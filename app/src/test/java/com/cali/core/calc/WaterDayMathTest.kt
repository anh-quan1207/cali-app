package com.cali.core.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Số của mockup s15: 1.250 / 2.600 (48%, còn 1.350, ~6 cốc) và 2.850 (đạt ở lần 21:10, +250). */
class WaterDayMathTest {
    private val today = listOf(250, 500, 250, 250)
    private val evening = today + listOf(250, 250, 500, 250, 350)

    @Test fun `48 phan tram con 1350 khoang 6 coc 250`() {
        val m = WaterDayMath.of(today, 2600, 250)
        assertEquals(1250, m.total)
        assertEquals(48, m.percent)
        assertEquals(1350, m.leftMl)
        assertEquals(6, m.cupsLeft)
        assertEquals(0, m.overMl)
        assertNull(m.reachedIndex)
    }

    @Test fun `vuot muc tieu 250 dat o lan uong cuoi`() {
        val m = WaterDayMath.of(evening, 2600, 250)
        assertEquals(2850, m.total)
        assertEquals(110, m.percent)
        assertEquals(0, m.leftMl)
        assertEquals(250, m.overMl)
        assertEquals(8, m.reachedIndex) // hàng 21:10
        assertEquals(2850, m.cumulative[8])
    }

    @Test fun `doan lan cat tai muc tieu`() {
        val m = WaterDayMath.of(listOf(2000, 1000), 2600, 250)
        assertEquals(listOf(0 to 2000, 2000 to 2600), m.laneSegments)
        assertEquals(1, m.reachedIndex)
        assertEquals(400, m.overMl)
    }

    @Test fun `ngay 12-11 mockup 2700 dat luc lan cuoi`() {
        val m = WaterDayMath.of(listOf(250, 500, 250, 250, 500, 250, 250, 450), 2600, 250)
        assertEquals(2700, m.total)
        assertEquals(7, m.reachedIndex)
        assertEquals(100, m.overMl)
    }

    @Test fun `muc tieu 0 khong chia cho 0`() {
        val m = WaterDayMath.of(listOf(250), 0, 250)
        assertEquals(0, m.percent)
        assertNull(m.reachedIndex)
    }
}
