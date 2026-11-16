package com.cali.core.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PortionScalerTest {
    private val bunBo = FoodValues(kcal = 490.0, proteinG = 33.0, carbG = 60.0, fatG = 12.0)

    private fun FoodValues.rounded() = listOf(kcal, proteinG, carbG, fatG).map { Math.round(it).toInt() }

    @Test fun `mot ruoi 490 ra 735 va 50-90-18`() {
        assertEquals(listOf(735, 50, 90, 18), PortionScaler.scale(bunBo, 1.5).rounded())
    }

    @Test fun `nua ra 245 va 17-30-6`() {
        assertEquals(listOf(245, 17, 30, 6), PortionScaler.scale(bunBo, 0.5).rounded())
    }

    @Test fun `gap doi ra 980 va 66-120-24`() {
        assertEquals(listOf(980, 66, 120, 24), PortionScaler.scale(bunBo, 2.0).rounded())
    }

    @Test fun `theo gram`() {
        assertEquals(1.2, PortionScaler.factorForGrams(baseGrams = 550.0, grams = 660.0)!!, 1e-9)
        assertNull(PortionScaler.factorForGrams(baseGrams = null, grams = 660.0))
        assertNull(PortionScaler.factorForGrams(baseGrams = 550.0, grams = 0.0))
    }

    @Test fun `nhan he so`() {
        assertEquals("½", PortionScaler.label(0.5))
        assertEquals("×1", PortionScaler.label(1.0))
        assertEquals("1½", PortionScaler.label(1.5))
        assertEquals("2", PortionScaler.label(2.0))
    }
}

class WeightInputTest {
    @Test fun `doc dau phay va dau cham`() {
        assertEquals(74.1, WeightInput.parse("74,1")!!, 1e-9)
        assertEquals(74.1, WeightInput.parse("74.1")!!, 1e-9)
        assertEquals(74.0, WeightInput.parse(" 74 ")!!, 1e-9)
        assertNull(WeightInput.parse(""))
        assertNull(WeightInput.parse("abc"))
    }

    @Test fun `trong khoang 30-250`() {
        assertEquals(true, WeightInput.inRange(30.0))
        assertEquals(true, WeightInput.inRange(250.0))
        assertEquals(false, WeightInput.inRange(29.9))
        assertEquals(false, WeightInput.inRange(741.0))
    }

    @Test fun `go thieu dau phay goi y 74,1`() {
        assertEquals(74.1, WeightInput.suggest(741.0)!!, 1e-9)
        assertEquals(74.1, WeightInput.suggest(7410.0)!!, 1e-9)
        assertNull(WeightInput.suggest(74.1)) // đã hợp lệ thì không gợi ý
        assertNull(WeightInput.suggest(5.0))
    }
}
