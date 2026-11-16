package com.cali.core.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmberCheckTest {

    private fun v(k: Double, p: Double, c: Double, f: Double) = FoodValues(k, p, c, f)

    @Test fun `so lieu khop thi khong co co`() {
        // 4×20 + 4×50 + 9×10 = 370
        val r = AmberCheck.check(v(370.0, 20.0, 50.0, 10.0))
        assertTrue(r.flags.isEmpty())
        assertEquals(v(370.0, 20.0, 50.0, 10.0), r.values)
    }

    @Test fun `lech dung 20 phan tram chua gan co, tren 20 thi gan`() {
        // macro ra 4×25 + 4×25 = 200 kcal; kcal 250 → lệch 50/250 = 20% → chưa vượt
        assertTrue(AmberCheck.check(v(250.0, 25.0, 25.0, 0.0)).flags.isEmpty())
        // kcal 255 → lệch 55/255 ≈ 21,6%
        val r = AmberCheck.check(v(255.0, 25.0, 25.0, 0.0))
        assertEquals(setOf(AmberFlag.MACRO_MISMATCH), r.flags)
        assertEquals(255.0, r.values.kcal, 1e-9) // giữ kcal của AI
    }

    @Test fun `so am va NaN ve 0 kem co`() {
        val r = AmberCheck.check(v(Double.NaN, -3.0, 10.0, 2.0))
        assertTrue(AmberFlag.INVALID_NUMBER_FIXED in r.flags)
        assertEquals(0.0, r.values.kcal, 1e-9)
        assertEquals(0.0, r.values.proteinG, 1e-9)
    }

    @Test fun `mon tren 2500 kcal co ve cao`() {
        // 4×100 + 4×400 + 9×60 = 2540
        val r = AmberCheck.check(v(2540.0, 100.0, 400.0, 60.0))
        assertEquals(setOf(AmberFlag.VERY_HIGH), r.flags)
        assertTrue(AmberCheck.check(v(2500.0, 100.0, 400.0, 400.0 / 9 + 20.0 / 9)).flags.isEmpty())
    }

    @Test fun `lam tron kcal boi 5, gram mot chu so`() {
        val r = AmberCheck.check(v(371.4, 20.04, 49.96, 10.06))
        assertEquals(370.0, r.values.kcal, 1e-9)
        assertEquals(20.0, r.values.proteinG, 1e-9)
        assertEquals(50.0, r.values.carbG, 1e-9)
        assertEquals(10.1, r.values.fatG, 1e-9)
        assertEquals(375.0, AmberCheck.check(v(372.5, 20.0, 50.0, 10.3)).values.kcal, 1e-9)
    }
}
