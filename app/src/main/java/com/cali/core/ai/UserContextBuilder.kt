package com.cali.core.ai

import com.cali.core.data.db.AppDatabase
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.DiaryRepository
import com.cali.core.ui.format.VnFormat
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first

/**
 * Gom ngữ cảnh gửi kèm mỗi lần gọi (03 §4) từ dữ liệu trên máy: hồ sơ, cân gần nhất, mục tiêu của hôm nay,
 * các món đã ghi hôm nay, 20 món hay ăn trong Món của tôi, ghi chú cá nhân.
 */
class UserContextBuilder @Inject constructor(
    private val db: AppDatabase,
    private val diary: DiaryRepository,
    private val settings: SettingsRepository,
    private val clock: Clock,
) {
    suspend fun build(logTime: LocalTime? = null): Result<UserContext> = runCatching {
        val now = LocalDateTime.now(clock).withSecond(0).withNano(0)
        val today: LocalDate = now.toLocalDate()
        val profile = checkNotNull(db.userProfileDao().observe().first()) { "Chưa có hồ sơ" }
        val day = diary.observeDay(today).first()
        val goal = checkNotNull(day.goal) { "Chưa có mục tiêu" }
        val weight = db.weightEntryDao().observeLatest().first()?.weightKg ?: goal.weightKgUsed
        val s = day.summary

        UserContext(
            now = now,
            sex = profile.sex,
            age = today.year - profile.birthYear,
            heightCm = profile.heightCm,
            weightKg = weight,
            targetWeightKg = profile.targetWeightKg,
            goalKcal = goal.targetKcal,
            goalProteinG = goal.proteinG,
            goalCarbG = goal.carbG,
            goalFatG = goal.fatG,
            eatenKcal = s.totalKcal.roundToInt(),
            eatenProteinG = s.proteinG.roundToInt(),
            eatenCarbG = s.carbG.roundToInt(),
            eatenFatG = s.fatG.roundToInt(),
            todayEntries = day.entries.map {
                ContextEntry(it.mealType, diary.toLocal(it.eatenAt).toLocalTime(), it.name, it.kcal.roundToInt())
            },
            logTime = logTime,
            personalNotes = settings.current().personalNotes,
            frequentFoods = db.myFoodDao().top(MAX_FREQUENT).map { f ->
                val portion = listOfNotNull(f.portionDesc?.trim()?.takeIf { it.isNotEmpty() }, f.grams?.let { "${VnFormat.int(it)} g" })
                if (portion.isEmpty()) f.name else "${f.name} (${portion.joinToString(" ")})"
            },
        )
    }

    companion object {
        const val MAX_FREQUENT = 20
    }
}
