package com.cali.core.calc.model

enum class Sex { MALE, FEMALE }

/** 02 §3. */
enum class ActivityLevel(val factor: Double) {
    SEDENTARY(1.2),
    LIGHT(1.375),
    MODERATE(1.55),
    ACTIVE(1.725),
    VERY_ACTIVE(1.9),
}

enum class GoalType { LOSE, MAINTAIN, GAIN }

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }

/** Lý do mục tiêu bị kẹp (02 §5). */
enum class ClampReason { MAX_DEFICIT, FLOOR, MAX_SURPLUS }

/** 02 §7, ngưỡng châu Á. */
enum class BmiClass { UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE_1, OBESE_2 }

data class Profile(
    val sex: Sex,
    val age: Int,
    val heightCm: Double,
    val weightKg: Double,
    val activityLevel: ActivityLevel,
    val goalType: GoalType,
    val targetWeightKg: Double?,
    val weeklyRateKg: Double,
)

/** Tỉ lệ năng lượng ghi đè, tổng = 100. */
data class MacroPct(val protein: Int, val carb: Int, val fat: Int) {
    init {
        require(protein + carb + fat == 100) { "Tổng tỉ lệ macro phải bằng 100" }
    }
}

data class Macros(val proteinG: Int, val carbG: Int, val fatG: Int)

data class TargetResult(
    val rawTarget: Double,
    val targetKcal: Int,
    val clampReason: ClampReason?,
    /** Tốc độ thực tế (kg/tuần); khác tốc độ chọn khi bị kẹp. */
    val actualWeeklyRateKg: Double,
)

data class GoalResult(
    val bmr: Double,
    val tdee: Double,
    val target: TargetResult,
    val macros: Macros,
    val waterMl: Int,
    val bmi: Double,
    val bmiClass: BmiClass,
)
