package com.cali.core.calc

import com.cali.core.calc.model.ActivityLevel
import com.cali.core.calc.model.BmiClass
import com.cali.core.calc.model.ClampReason
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.MacroPct
import com.cali.core.calc.model.Macros
import com.cali.core.calc.model.Profile
import com.cali.core.calc.model.Sex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class NutritionCalculatorTest {

    private val calc = NutritionCalculator

    /** 02 §10: Nam, 30 tuổi, 170 cm, 75 kg, vận động nhẹ, giảm 0,5 kg/tuần, mục tiêu 68 kg. */
    private val example = Profile(
        sex = Sex.MALE, age = 30, heightCm = 170.0, weightKg = 75.0,
        activityLevel = ActivityLevel.LIGHT, goalType = GoalType.LOSE,
        targetWeightKg = 68.0, weeklyRateKg = 0.5,
    )

    // ---------- 02 §10 ----------

    @Test fun `02-10 BMR 1667,5`() =
        assertEquals(1667.5, calc.bmr(Sex.MALE, 75.0, 170.0, 30), 1e-9)

    @Test fun `02-10 TDEE 2292,8`() =
        assertEquals(2292.8, calc.tdee(1667.5, ActivityLevel.LIGHT.factor), 0.05)

    @Test fun `02-10 raw target 1742,8`() =
        assertEquals(1742.8, calc.rawTarget(2292.8125, GoalType.LOSE, 0.5), 0.05)

    @Test fun `02-10 muc tieu 1740 khong bi kep`() {
        val r = calc.compute(example)
        assertEquals(1740, r.target.targetKcal)
        assertNull(r.target.clampReason)
    }

    @Test fun `02-10 BMI 26,0 beo phi do I`() {
        val r = calc.compute(example)
        assertEquals(26.0, r.bmi, 1e-9)
        assertEquals(BmiClass.OBESE_1, r.bmiClass)
    }

    @Test fun `02-10 macro 122 - 196 - 52 g`() =
        assertEquals(Macros(proteinG = 122, carbG = 196, fatG = 52), calc.compute(example).macros)

    @Test fun `02-10 nuoc 2600 ml`() = assertEquals(2600, calc.compute(example).waterMl)

    @Test fun `02-10 du kien 14 tuan`() {
        assertEquals(98, calc.etaDays(75.0, 68.0, 0.5))
        assertEquals(LocalDate.of(2027, 2, 22), calc.etaDate(LocalDate.of(2026, 11, 16), 75.0, 68.0, 0.5))
    }

    // ---------- Bộ số mockup (CLAUDE.md): 74,1 kg ----------

    private val mock = example.copy(weightKg = 74.1)

    @Test fun `mockup 74,1 kg BMR 1658,5 TDEE 2280`() {
        val r = calc.compute(mock)
        assertEquals(1658.5, r.bmr, 1e-9)
        assertEquals(2280.0, r.tdee, 0.5)
    }

    @Test fun `mockup 0,5 kg-tuan ra 1730`() {
        val r = calc.compute(mock)
        assertEquals(1730, r.target.targetKcal)
        assertNull(r.target.clampReason)
    }

    @Test fun `mockup 0,75 kg-tuan bi kep tran tham hut ve 1710`() {
        val r = calc.compute(mock.copy(weeklyRateKg = 0.75))
        assertEquals(1710, r.target.targetKcal)
        assertEquals(ClampReason.MAX_DEFICIT, r.target.clampReason)
        // tốc độ thực tế = |TDEE − target| × 7 / 7700
        assertEquals((2280.4375 - 1710) * 7 / 7700, r.target.actualWeeklyRateKg, 1e-6)
    }

    @Test fun `mockup ghi de 1400 van bi kep ve 1710`() {
        val r = calc.compute(mock, kcalOverride = 1400)
        assertEquals(1710, r.target.targetKcal)
        assertEquals(ClampReason.MAX_DEFICIT, r.target.clampReason)
    }

    @Test fun `mockup macro ghi de 30-40-30 ra 128 - 171 - 57`() {
        val m = calc.macros(1710, GoalType.LOSE, 74.1, 170.0, 68.0, MacroPct(protein = 30, carb = 40, fat = 30))
        assertEquals(Macros(proteinG = 128, carbG = 171, fatG = 57), m)
    }

    // ---------- Kẹp an toàn 02 §5 ----------

    @Test fun `nu san 1200 ap dung sau tran tham hut`() {
        // BMR 1189, TDEE 1426,8; raw 326,8 → trần 25% 1070,1 → sàn max(1200, 1189) = 1200
        val p = Profile(Sex.FEMALE, 30, 160.0, 50.0, ActivityLevel.SEDENTARY, GoalType.LOSE, 45.0, 1.0)
        val r = calc.compute(p)
        assertEquals(1200, r.target.targetKcal)
        assertEquals(ClampReason.FLOOR, r.target.clampReason)
    }

    @Test fun `san la BMR khi BMR cao hon san gioi tinh`() {
        // Nam BMR 2005 (100 kg, 180 cm, 25 tuổi), ít vận động: TDEE 2406; giảm 1 kg → raw 1306 → trần 1804,5 → sàn 2005
        val p = Profile(Sex.MALE, 25, 180.0, 100.0, ActivityLevel.SEDENTARY, GoalType.LOSE, 80.0, 1.0)
        val r = calc.compute(p)
        assertEquals(2010, r.target.targetKcal) // 2005 làm tròn 10 (nửa lên)
        assertEquals(ClampReason.FLOOR, r.target.clampReason)
    }

    @Test fun `tang can du toi da 500`() {
        // BMR 1705, TDEE 2642,75; +0,5 kg → raw 3192,75 → trần +500 = 3142,75 → 3140
        val p = Profile(Sex.MALE, 25, 180.0, 70.0, ActivityLevel.MODERATE, GoalType.GAIN, 75.0, 0.5)
        val r = calc.compute(p)
        assertEquals(3140, r.target.targetKcal)
        assertEquals(ClampReason.MAX_SURPLUS, r.target.clampReason)
    }

    @Test fun `giu can muc tieu bang TDEE va dam 1,4`() {
        val p = example.copy(goalType = GoalType.MAINTAIN, targetWeightKg = null, weeklyRateKg = 0.0, weightKg = 65.0)
        val r = calc.compute(p)
        // BMR 1567,5, TDEE 2155,3 → 2160
        assertEquals(2160, r.target.targetKcal)
        assertNull(r.target.clampReason)
        assertEquals(91, r.macros.proteinG) // 1,4 × 65 (BMI 22,5 < 25 → cân hiện tại)
        assertNull(calc.etaDays(65.0, null, 0.0))
    }

    // ---------- Macro 02 §6 ----------

    @Test fun `tinh bot duoi 20 phan tram thi ha dam xuong 1,6`() {
        // 1,8 × 90 = 162 g → carb (1200 − 648 − 324)/4 = 57 g < 20% → đạm 1,6 × 90 = 144 g
        val m = calc.macros(1200, GoalType.LOSE, 90.0, 192.0, 80.0)
        assertEquals(Macros(proteinG = 144, carbG = 75, fatG = 36), m)
    }

    @Test fun `tang can dam 1,6 beo 25 phan tram`() {
        val m = calc.macros(2600, GoalType.GAIN, 60.0, 175.0, 65.0)
        // đạm 96 g = 384 kcal; béo 650 kcal = 72,2 g; carb (2600 − 384 − 650)/4 = 391,5
        assertEquals(Macros(proteinG = 96, carbG = 392, fatG = 72), m)
    }

    // ---------- BMI 02 §7 ----------

    @Test fun `bien BMI chau A`() {
        assertEquals(BmiClass.UNDERWEIGHT, calc.bmiClass(18.4))
        assertEquals(BmiClass.NORMAL, calc.bmiClass(18.5))
        assertEquals(BmiClass.NORMAL, calc.bmiClass(22.9))
        assertEquals(BmiClass.OVERWEIGHT, calc.bmiClass(23.0))
        assertEquals(BmiClass.OVERWEIGHT, calc.bmiClass(24.9))
        assertEquals(BmiClass.OBESE_1, calc.bmiClass(25.0))
        assertEquals(BmiClass.OBESE_1, calc.bmiClass(29.9))
        assertEquals(BmiClass.OBESE_2, calc.bmiClass(30.0))
    }

    @Test fun `BMI lam tron 1 chu so`() = assertEquals(25.6, calc.bmi(74.1, 170.0), 1e-9)

    @Test fun `canh bao BMI thap`() {
        assertTrue(calc.lowBmiWarning(currentBmi = 21.0, goalType = GoalType.LOSE, targetBmi = 18.2))
        assertTrue(calc.lowBmiWarning(currentBmi = 18.0, goalType = GoalType.LOSE, targetBmi = 17.5))
        assertFalse(calc.lowBmiWarning(currentBmi = 18.0, goalType = GoalType.GAIN, targetBmi = 20.0))
        assertFalse(calc.lowBmiWarning(currentBmi = 26.0, goalType = GoalType.LOSE, targetBmi = 23.5))
    }

    // ---------- Nước 02 §8 ----------

    @Test fun `nuoc cong them theo van dong va kep 1500 - 4000`() {
        assertEquals(2900, calc.waterMl(75.0, ActivityLevel.ACTIVE.factor)) // 2625 + 300 = 2925
        assertEquals(1500, calc.waterMl(40.0, ActivityLevel.LIGHT.factor)) // 1400 → sàn
        assertEquals(4000, calc.waterMl(120.0, ActivityLevel.VERY_ACTIVE.factor)) // 4700 → trần
    }
}
