package com.cali.core.data.repo

import com.cali.core.data.db.AppDatabase
import com.cali.core.data.db.entity.GoalSnapshotEntity
import com.cali.core.data.db.entity.WeightEntryEntity
import com.cali.core.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Đề xuất tính lại mục tiêu sau khi cân (sheet Đang dùng / Mới, F09). */
data class RecalcProposal(val current: GoalSnapshotEntity, val preview: GoalPreview)

data class WeighOutcome(
    val entry: WeightEntryEntity,
    /** Ngày đó đã có số → "Thay bản ghi". */
    val replaced: Boolean,
    /** Công tắc "Tự tính lại khi ghi cân" bật → đã áp dụng. */
    val applied: GoalSnapshotEntity?,
    /** Công tắc tắt và mục tiêu sẽ đổi → hỏi người dùng. */
    val proposal: RecalcProposal?,
)

/** Cân nặng (04 §2 weight_entry): 1 bản ghi mỗi ngày, ghi lại là thay. */
@Singleton
class WeightRepository @Inject constructor(
    db: AppDatabase,
    private val profileRepo: ProfileRepository,
    private val settings: SettingsRepository,
    private val clock: Clock,
) {
    private val dao = db.weightEntryDao()

    fun observeAll(): Flow<List<WeightEntryEntity>> = dao.observeAll()
    fun observeLatest(): Flow<WeightEntryEntity?> = dao.observeLatest()

    suspend fun hasEntry(date: LocalDate): Boolean = dao.byDate(date) != null
    suspend fun byDate(date: LocalDate): WeightEntryEntity? = dao.byDate(date)

    /** Lần cân gần nhất trước [date], cho dòng "−0,3 kg so với lần cân 08/11". */
    suspend fun previousBefore(date: LocalDate): WeightEntryEntity? = dao.latestBefore(date)

    suspend fun save(date: LocalDate, kg: Double, note: String? = null): Result<WeighOutcome> = runCatching {
        val existing = dao.byDate(date)
        val entry = WeightEntryEntity(id = existing?.id ?: 0, date = date, weightKg = kg, note = note, createdAt = clock.millis())
        val id = dao.put(entry)
        val saved = entry.copy(id = id)

        // Chỉ tính lại khi đây là lần cân mới nhất (mục tiêu dùng cân mới nhất, 02 §11).
        val latest = dao.observeLatest().first()
        val profile = profileRepo.profile.first()
        val current = profileRepo.observeGoal(profileRepo.today()).first()
        var applied: GoalSnapshotEntity? = null
        var proposal: RecalcProposal? = null
        if (latest?.date == date && profile != null && current != null) {
            val s = settings.current()
            val input = profile.toInput()
            if (profile.autoRecalcOnWeighIn) {
                applied = profileRepo.apply(input, true, s.kcalOverride, s.macroPctOverride).getOrThrow()
            } else {
                val preview = profileRepo.preview(input, kg, s.kcalOverride, s.macroPctOverride)
                val m = preview.result.macros
                val changed = preview.result.target.targetKcal != current.targetKcal ||
                    m.proteinG != current.proteinG || m.carbG != current.carbG || m.fatG != current.fatG
                if (changed) proposal = RecalcProposal(current, preview)
            }
        }
        WeighOutcome(saved, existing != null, applied, proposal)
    }

    /** "Áp dụng" trên sheet Đang dùng / Mới sau khi cân. */
    suspend fun applyProposal(): Result<GoalSnapshotEntity> {
        val profile = profileRepo.profile.first() ?: return Result.failure(IllegalStateException("Chưa có hồ sơ"))
        val s = settings.current()
        return profileRepo.apply(profile.toInput(), profile.autoRecalcOnWeighIn, s.kcalOverride, s.macroPctOverride)
    }

    suspend fun delete(entry: WeightEntryEntity): Result<Unit> = runCatching { dao.delete(entry) }

    suspend fun restore(entry: WeightEntryEntity): Result<Unit> = runCatching { dao.insert(entry) }
}
