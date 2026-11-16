package com.cali.core.calc

import com.cali.core.calc.model.ActivityLevel
import com.cali.core.calc.model.BmiClass
import com.cali.core.calc.model.ClampReason
import com.cali.core.calc.model.GoalResult
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.MacroPct
import com.cali.core.calc.model.Macros
import com.cali.core.calc.model.Profile
import com.cali.core.calc.model.Sex
import com.cali.core.calc.model.TargetResult
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/** Công thức ở 02. Thuần Kotlin, không phụ thuộc Android. */
object NutritionCalculator {
    const val KCAL_PER_KG = 7700.0
    private const val MAX_DEFICIT_RATIO = 0.25
    private const val MAX_SURPLUS_KCAL = 500.0
    private const val FLOOR_FEMALE = 1200.0
    private const val FLOOR_MALE = 1500.0
    private const val CARB_MIN_RATIO = 0.20

    fun bmr(sex: Sex, weightKg: Double, heightCm: Double, age: Int): Double {
        val base = 10 * weightKg + 6.25 * heightCm - 5 * age
        return if (sex == Sex.MALE) base + 5 else base - 161
    }

    fun tdee(bmr: Double, factor: Double): Double = bmr * factor

    fun rawTarget(tdee: Double, goalType: GoalType, weeklyRateKg: Double): Double {
        val dailyDelta = weeklyRateKg * KCAL_PER_KG / 7
        return when (goalType) {
            GoalType.LOSE -> tdee - dailyDelta
            GoalType.MAINTAIN -> tdee
            GoalType.GAIN -> tdee + dailyDelta
        }
    }

    fun clampTarget(raw: Double, tdee: Double, bmr: Double, sex: Sex, goalType: GoalType, weeklyRateKg: Double): TargetResult {
        var target = raw
        var reason: ClampReason? = null

        // 1. Trần thâm hụt. Áp cho mọi giá trị dưới TDEE (kể cả ghi đè), không chỉ khi chọn Giảm.
        val minByDeficit = tdee * (1 - MAX_DEFICIT_RATIO)
        if (target < minByDeficit) {
            target = minByDeficit
            reason = ClampReason.MAX_DEFICIT
        }
        // 2. Sàn calo.
        val floor = max(if (sex == Sex.FEMALE) FLOOR_FEMALE else FLOOR_MALE, bmr)
        if (target < floor) {
            target = floor
            reason = ClampReason.FLOOR
        }
        // 3. Dư thừa tối đa.
        if (target - tdee > MAX_SURPLUS_KCAL) {
            target = tdee + MAX_SURPLUS_KCAL
            reason = ClampReason.MAX_SURPLUS
        }
        // 4. Làm tròn 10.
        val rounded = roundTo(target, 10)

        val actualRate = if (reason == null) {
            if (goalType == GoalType.MAINTAIN) 0.0 else weeklyRateKg
        } else {
            abs(tdee - rounded) * 7 / KCAL_PER_KG
        }
        return TargetResult(rawTarget = raw, targetKcal = rounded, clampReason = reason, actualWeeklyRateKg = actualRate)
    }

    fun macros(
        targetKcal: Int,
        goalType: GoalType,
        weightKg: Double,
        heightCm: Double,
        targetWeightKg: Double?,
        override: MacroPct? = null,
    ): Macros {
        val target = targetKcal.toDouble()
        if (override != null) {
            return Macros(
                proteinG = (target * override.protein / 100 / 4).roundToInt(),
                carbG = (target * override.carb / 100 / 4).roundToInt(),
                fatG = (target * override.fat / 100 / 9).roundToInt(),
            )
        }
        val refWeight = if (bmi(weightKg, heightCm) >= 25 && targetWeightKg != null) targetWeightKg else weightKg
        val (proteinPerKg, fatPct) = when (goalType) {
            GoalType.LOSE -> 1.8 to 0.27
            GoalType.MAINTAIN -> 1.4 to 0.28
            GoalType.GAIN -> 1.6 to 0.25
        }
        val fatKcal = target * fatPct
        fun carbKcalFor(proteinG: Int) = target - proteinG * 4 - fatKcal

        var proteinG = (proteinPerKg * refWeight).roundToInt()
        if (carbKcalFor(proteinG) < CARB_MIN_RATIO * target) {
            proteinG = (1.6 * refWeight).roundToInt()
        }
        val carbG = max(0.0, carbKcalFor(proteinG) / 4).roundToInt()
        return Macros(proteinG = proteinG, carbG = carbG, fatG = (fatKcal / 9).roundToInt())
    }

    fun bmi(weightKg: Double, heightCm: Double): Double {
        val m = heightCm / 100
        return (weightKg / (m * m) * 10).roundToInt() / 10.0
    }

    fun bmiClass(bmi: Double): BmiClass = when {
        bmi < 18.5 -> BmiClass.UNDERWEIGHT
        bmi < 23.0 -> BmiClass.NORMAL
        bmi < 25.0 -> BmiClass.OVERWEIGHT
        bmi < 30.0 -> BmiClass.OBESE_1
        else -> BmiClass.OBESE_2
    }

    fun lowBmiWarning(currentBmi: Double, goalType: GoalType, targetBmi: Double?): Boolean =
        (targetBmi != null && targetBmi < 18.5) || (goalType == GoalType.LOSE && currentBmi < 18.5)

    fun waterMl(weightKg: Double, activityFactor: Double): Int {
        val bonus = when (activityFactor) {
            ActivityLevel.ACTIVE.factor -> 300
            ActivityLevel.VERY_ACTIVE.factor -> 500
            else -> 0
        }
        return roundTo(weightKg * 35 + bonus, 100).coerceIn(1500, 4000)
    }

    fun etaDays(weightKg: Double, targetWeightKg: Double?, actualWeeklyRateKg: Double): Int? {
        if (targetWeightKg == null || actualWeeklyRateKg <= 0.0) return null
        val weeks = abs(weightKg - targetWeightKg) / actualWeeklyRateKg
        // Trừ sai số dấu phẩy động trước khi làm tròn lên (14,000000001 tuần không thành 99 ngày).
        return ceil(weeks * 7 - 1e-9).toInt()
    }

    fun etaDate(today: LocalDate, weightKg: Double, targetWeightKg: Double?, actualWeeklyRateKg: Double): LocalDate? =
        etaDays(weightKg, targetWeightKg, actualWeeklyRateKg)?.let { today.plusDays(it.toLong()) }

    fun compute(profile: Profile, kcalOverride: Int? = null, macroOverride: MacroPct? = null): GoalResult {
        val bmr = bmr(profile.sex, profile.weightKg, profile.heightCm, profile.age)
        val tdee = tdee(bmr, profile.activityLevel.factor)
        val rate = if (profile.goalType == GoalType.MAINTAIN) 0.0 else profile.weeklyRateKg
        val raw = kcalOverride?.toDouble() ?: rawTarget(tdee, profile.goalType, rate)
        val target = clampTarget(raw, tdee, bmr, profile.sex, profile.goalType, rate).let {
            // Ghi đè không bị kẹp: tốc độ thực tế suy từ chính số ghi đè.
            if (kcalOverride != null && it.clampReason == null) it.copy(actualWeeklyRateKg = abs(tdee - it.targetKcal) * 7 / KCAL_PER_KG) else it
        }
        val bmi = bmi(profile.weightKg, profile.heightCm)
        return GoalResult(
            bmr = bmr,
            tdee = tdee,
            target = target,
            macros = macros(target.targetKcal, profile.goalType, profile.weightKg, profile.heightCm, profile.targetWeightKg, macroOverride),
            waterMl = waterMl(profile.weightKg, profile.activityLevel.factor),
            bmi = bmi,
            bmiClass = bmiClass(bmi),
        )
    }

    /** Làm tròn nửa lên tới bội số của [step]. */
    internal fun roundTo(value: Double, step: Int): Int = Math.round(value / step).toInt() * step
}
