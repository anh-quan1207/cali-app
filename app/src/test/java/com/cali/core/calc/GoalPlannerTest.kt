package com.cali.core.calc

import com.cali.core.calc.model.ActivityLevel
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.Profile
import com.cali.core.calc.model.Sex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalPlannerTest {

    private val p75 = Profile(Sex.MALE, 30, 170.0, 75.0, ActivityLevel.LIGHT, GoalType.LOSE, 68.0, 0.5)

    @Test fun `bon toc do giam, hai toc do tang, giu can khong co`() {
        assertEquals(listOf(0.25, 0.5, 0.75, 1.0), GoalPlanner.rates(GoalType.LOSE).map { it.first })
        assertEquals(RateLabel.RECOMMENDED, GoalPlanner.rates(GoalType.LOSE)[1].second)
        assertEquals(listOf(0.25, 0.5), GoalPlanner.rates(GoalType.GAIN).map { it.first })
        assertEquals(RateLabel.RECOMMENDED, GoalPlanner.rates(GoalType.GAIN)[0].second)
        assertTrue(GoalPlanner.rates(GoalType.MAINTAIN).isEmpty())
        assertEquals(0.5, GoalPlanner.defaultRate(GoalType.LOSE), 0.0)
        assertEquals(0.25, GoalPlanner.defaultRate(GoalType.GAIN), 0.0)
        assertEquals(0.0, GoalPlanner.defaultRate(GoalType.MAINTAIN), 0.0)
    }

    /** Mockup S01 bước 5 (75 kg → 68 kg). */
    @Test fun `buoc 5 o 75 kg`() {
        val o = GoalPlanner.rateOptions(p75)
        assertEquals(listOf(2020, 1740, 1720, 1720), o.map { it.targetKcal })
        assertEquals(listOf(28, 14, 13, 13), o.map { it.weeks })
        assertEquals(listOf(false, false, true, true), o.map { it.clamped })
        assertEquals(0.52, o[2].actualRateKg, 0.005)
    }

    /** Mockup S41 sheet Tốc độ (74,1 kg). */
    @Test fun `sheet toc do o 74,1 kg`() {
        val o = GoalPlanner.rateOptions(p75.copy(weightKg = 74.1))
        assertEquals(listOf(2010, 1730, 1710, 1710), o.map { it.targetKcal })
        assertEquals(listOf(24, 12, 12, 12), o.map { it.weeks })
        assertFalse(o[1].clamped)
        assertTrue(o[3].clamped)
    }

    /** Mockup S01 bước 5 BMI thấp: đích 50 kg. */
    @Test fun `dich 50 kg ra 100 va 50 tuan`() {
        val o = GoalPlanner.rateOptions(p75.copy(targetWeightKg = 50.0))
        assertEquals(listOf(100, 50, 48, 48), o.map { it.weeks })
    }

    @Test fun `khong co dich thi khong co so tuan`() {
        assertNull(GoalPlanner.weeksRounded(75.0, null, 0.5))
        assertNull(GoalPlanner.weeksRounded(75.0, 68.0, 0.0))
    }

    // ---------- InputLimits ----------

    @Test fun `gioi han o nhap`() {
        assertTrue(InputLimits.heightValid(120.0))
        assertTrue(InputLimits.heightValid(220.0))
        assertFalse(InputLimits.heightValid(119.9))
        assertFalse(InputLimits.heightValid(null))
        assertTrue(InputLimits.weightValid(30.0))
        assertFalse(InputLimits.weightValid(7.0))
        assertFalse(InputLimits.weightValid(250.1))
        assertTrue(InputLimits.kcalOverrideValid(1400))
        assertFalse(InputLimits.kcalOverrideValid(999))
        assertFalse(InputLimits.kcalOverrideValid(5001))
        assertTrue(InputLimits.macroPctValid(30, 40, 30))
        assertFalse(InputLimits.macroPctValid(30, 40, 31))
        assertFalse(InputLimits.macroPctValid(30, null, 70))
        assertFalse(InputLimits.macroPctValid(-10, 80, 30))
        assertEquals(1926..2011, InputLimits.birthYears(2026))
    }

    @Test fun `doc so kieu Viet`() {
        assertEquals(74.1, InputLimits.parseDecimal("74,1")!!, 1e-9)
        assertEquals(74.1, InputLimits.parseDecimal("74.1")!!, 1e-9)
        assertEquals(170.0, InputLimits.parseDecimal(" 170 ")!!, 1e-9)
        assertNull(InputLimits.parseDecimal(""))
        assertNull(InputLimits.parseDecimal("7a"))
        assertNull(InputLimits.parseDecimal("1,2,3"))
    }
}
