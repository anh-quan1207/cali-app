package com.cali.core.calc

import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.Profile
import kotlin.math.abs
import kotlin.math.roundToInt

/** Nhãn tốc độ ở 02 §4. */
enum class RateLabel { GENTLE, RECOMMENDED, FAST, VERY_FAST }

/** Một hàng chọn tốc độ (S01 bước 5, sheet Tốc độ ở S41). */
data class RateOption(
    val rateKg: Double,
    val label: RateLabel,
    val targetKcal: Int,
    /** Bị kẹp an toàn → hiện hổ phách kèm tốc độ thực. */
    val clamped: Boolean,
    val actualRateKg: Double,
    /** Số tuần làm tròn để hiển thị "~14 tuần"; null nếu không có đích. */
    val weeks: Int?,
)

/** Tuỳ chọn mục tiêu cho onboarding / hồ sơ. Thuần Kotlin. */
object GoalPlanner {
    fun rates(goal: GoalType): List<Pair<Double, RateLabel>> = when (goal) {
        GoalType.LOSE -> listOf(
            0.25 to RateLabel.GENTLE,
            0.5 to RateLabel.RECOMMENDED,
            0.75 to RateLabel.FAST,
            1.0 to RateLabel.VERY_FAST,
        )
        GoalType.GAIN -> listOf(0.25 to RateLabel.RECOMMENDED, 0.5 to RateLabel.FAST)
        GoalType.MAINTAIN -> emptyList()
    }

    fun defaultRate(goal: GoalType): Double =
        rates(goal).firstOrNull { it.second == RateLabel.RECOMMENDED }?.first ?: 0.0

    fun rateOptions(profile: Profile): List<RateOption> = rates(profile.goalType).map { (rate, label) ->
        val t = NutritionCalculator.compute(profile.copy(weeklyRateKg = rate)).target
        RateOption(
            rateKg = rate,
            label = label,
            targetKcal = t.targetKcal,
            clamped = t.clampReason != null,
            actualRateKg = t.actualWeeklyRateKg,
            weeks = weeksRounded(profile.weightKg, profile.targetWeightKg, t.actualWeeklyRateKg),
        )
    }

    fun weeksRounded(weightKg: Double, targetWeightKg: Double?, actualRateKg: Double): Int? {
        if (targetWeightKg == null || actualRateKg <= 0.0) return null
        return (abs(weightKg - targetWeightKg) / actualRateKg).roundToInt()
    }
}

/** Giới hạn ô nhập (02 §1, mockup S01/S41). */
object InputLimits {
    val HEIGHT_CM = 120.0..220.0
    val WEIGHT_KG = 30.0..250.0
    val KCAL_OVERRIDE = 1000..5000
    const val MIN_AGE = 15
    const val MAX_AGE = 100

    fun heightValid(v: Double?): Boolean = v != null && v in HEIGHT_CM
    fun weightValid(v: Double?): Boolean = v != null && v in WEIGHT_KG
    fun kcalOverrideValid(v: Int?): Boolean = v != null && v in KCAL_OVERRIDE

    fun macroPctValid(p: Int?, c: Int?, f: Int?): Boolean {
        if (p == null || c == null || f == null) return false
        if (listOf(p, c, f).any { it !in 0..100 }) return false
        return p + c + f == 100
    }

    fun birthYears(currentYear: Int): IntRange = (currentYear - MAX_AGE)..(currentYear - MIN_AGE)

    private val decimal = Regex("""^\d+([.,]\d+)?$""")

    fun parseDecimal(text: String): Double? {
        val t = text.trim()
        if (!decimal.matches(t)) return null
        return t.replace(',', '.').toDoubleOrNull()
    }
}
