package com.cali.core.data.repo

import androidx.room.withTransaction
import com.cali.core.calc.GoalPlanner
import com.cali.core.calc.NutritionCalculator
import com.cali.core.calc.model.ActivityLevel
import com.cali.core.calc.model.GoalResult
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.MacroPct
import com.cali.core.calc.model.Profile
import com.cali.core.calc.model.Sex
import com.cali.core.data.db.AppDatabase
import com.cali.core.data.db.entity.GoalSnapshotEntity
import com.cali.core.data.db.entity.UserProfileEntity
import com.cali.core.data.db.entity.WeightEntryEntity
import com.cali.core.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Các trường hồ sơ người dùng nhập (S01 / S41). Cân nặng chỉ dùng khi onboarding. */
data class ProfileInput(
    val sex: Sex,
    val birthYear: Int,
    val heightCm: Double,
    val activityLevel: ActivityLevel,
    val goalType: GoalType,
    val targetWeightKg: Double?,
    val weeklyRateKg: Double,
)

/** Kết quả tính thử, chưa lưu. */
data class GoalPreview(
    val profile: Profile,
    val result: GoalResult,
    val etaDate: LocalDate?,
    val weeks: Int?,
    val kcalOverridden: Boolean,
    val macroOverridden: Boolean,
)

/** Hồ sơ + mục tiêu (02 §11, 04 §2). UI không gọi DAO trực tiếp. */
@Singleton
class ProfileRepository @Inject constructor(
    private val db: AppDatabase,
    private val settings: SettingsRepository,
    private val clock: Clock,
) {
    private val profileDao = db.userProfileDao()
    private val weightDao = db.weightEntryDao()
    private val goalDao = db.goalSnapshotDao()

    val profile: Flow<UserProfileEntity?> = profileDao.observe()
    val latestWeight: Flow<WeightEntryEntity?> = weightDao.observeLatest()

    fun today(): LocalDate = LocalDate.now(clock)

    fun observeGoal(date: LocalDate = today()): Flow<GoalSnapshotEntity?> = goalDao.observeForDate(date)

    fun preview(
        input: ProfileInput,
        weightKg: Double,
        kcalOverride: Int? = null,
        macroOverride: MacroPct? = null,
    ): GoalPreview {
        val p = toCalc(input, weightKg)
        val r = NutritionCalculator.compute(p, kcalOverride, macroOverride)
        val target = if (input.goalType == GoalType.MAINTAIN) null else input.targetWeightKg
        return GoalPreview(
            profile = p,
            result = r,
            etaDate = NutritionCalculator.etaDate(today(), weightKg, target, r.target.actualWeeklyRateKg),
            weeks = GoalPlanner.weeksRounded(weightKg, target, r.target.actualWeeklyRateKg),
            kcalOverridden = kcalOverride != null,
            macroOverridden = macroOverride != null,
        )
    }

    /** "Bắt đầu" ở S02: lưu hồ sơ, cân hôm nay, mục tiêu từ hôm nay; đánh dấu đã onboarding. */
    suspend fun completeOnboarding(input: ProfileInput, weightKg: Double): Result<GoalSnapshotEntity> = runCatching {
        val now = clock.millis()
        val day = today()
        val snapshot = snapshotOf(preview(input, weightKg), day, now)
        db.withTransaction {
            profileDao.upsert(entityOf(input, createdAt = now, updatedAt = now, autoRecalc = false))
            weightDao.put(WeightEntryEntity(date = day, weightKg = weightKg, createdAt = now))
            goalDao.put(snapshot)
        }
        settings.update { it.copy(onboardingDone = true, kcalOverride = null, macroPctOverride = null) }.getOrThrow()
        snapshot
    }

    /**
     * "Áp dụng" ở S41: lưu hồ sơ + ghi đè, tạo mục tiêu mới từ hôm nay theo lần cân mới nhất
     * (tính lại trong cùng ngày thì ghi đè snapshot của ngày đó).
     */
    suspend fun apply(
        input: ProfileInput,
        autoRecalcOnWeighIn: Boolean,
        kcalOverride: Int?,
        macroOverride: MacroPct?,
    ): Result<GoalSnapshotEntity> = runCatching {
        val weight = requireNotNull(weightDao.observeLatest().first()) { "Chưa có cân nặng" }.weightKg
        val existing = profileDao.observe().first()
        val now = clock.millis()
        val snapshot = snapshotOf(preview(input, weight, kcalOverride, macroOverride), today(), now)
        db.withTransaction {
            profileDao.upsert(entityOf(input, existing?.createdAt ?: now, now, autoRecalcOnWeighIn))
            goalDao.put(snapshot)
        }
        settings.update { it.copy(kcalOverride = kcalOverride, macroPctOverride = macroOverride) }.getOrThrow()
        snapshot
    }

    /** Chỉ đổi công tắc "Tự tính lại khi ghi cân", không tạo mục tiêu mới. */
    suspend fun setAutoRecalc(enabled: Boolean): Result<Unit> = runCatching {
        val p = requireNotNull(profileDao.observe().first())
        profileDao.upsert(p.copy(autoRecalcOnWeighIn = enabled, updatedAt = clock.millis()))
    }

    fun toCalc(input: ProfileInput, weightKg: Double) = Profile(
        sex = input.sex,
        age = today().year - input.birthYear,
        heightCm = input.heightCm,
        weightKg = weightKg,
        activityLevel = input.activityLevel,
        goalType = input.goalType,
        targetWeightKg = if (input.goalType == GoalType.MAINTAIN) null else input.targetWeightKg,
        weeklyRateKg = if (input.goalType == GoalType.MAINTAIN) 0.0 else input.weeklyRateKg,
    )

    private fun entityOf(input: ProfileInput, createdAt: Long, updatedAt: Long, autoRecalc: Boolean) = UserProfileEntity(
        sex = input.sex,
        birthYear = input.birthYear,
        heightCm = input.heightCm,
        activityLevel = input.activityLevel,
        goalType = input.goalType,
        targetWeightKg = if (input.goalType == GoalType.MAINTAIN) null else input.targetWeightKg,
        weeklyRateKg = if (input.goalType == GoalType.MAINTAIN) 0.0 else input.weeklyRateKg,
        autoRecalcOnWeighIn = autoRecalc,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun snapshotOf(p: GoalPreview, day: LocalDate, now: Long) = GoalSnapshotEntity(
        effectiveFrom = day,
        weightKgUsed = p.profile.weightKg,
        bmr = p.result.bmr,
        tdee = p.result.tdee,
        targetKcal = p.result.target.targetKcal,
        proteinG = p.result.macros.proteinG,
        carbG = p.result.macros.carbG,
        fatG = p.result.macros.fatG,
        waterMl = p.result.waterMl,
        isKcalOverridden = p.kcalOverridden,
        isMacroOverridden = p.macroOverridden,
        clampReason = p.result.target.clampReason,
        createdAt = now,
    )
}

fun UserProfileEntity.toInput() = ProfileInput(
    sex = sex,
    birthYear = birthYear,
    heightCm = heightCm,
    activityLevel = activityLevel,
    goalType = goalType,
    targetWeightKg = targetWeightKg,
    weeklyRateKg = weeklyRateKg,
)
