package com.cali.core.calc

import com.cali.core.calc.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

/** S44 (09 §4 GĐ6 mục 5): sửa giờ bắt đầu bữa, kể cả trùng giờ. */
class MealTimesRulesTest {
    private fun t(h: Int, m: Int = 0) = LocalTime.of(h, m)
    private val d = MealTypeResolver.DEFAULT

    @Test fun `mac dinh khong co khung nao bi che`() {
        assertTrue(MealTimesRules.shadowed(d).isEmpty())
    }

    @Test fun `doi gio giu nguyen thu tu danh sach`() {
        val r = MealTimesRules.withStart(d, 1, t(11))
        assertEquals(t(11), r[1].start)
        assertEquals(MealType.LUNCH, r[1].meal)
        assertEquals(d.map { it.meal }, r.map { it.meal })
    }

    @Test fun `trung gio - bua dung sau trong danh sach thang, bua kia bi che`() {
        // Trưa đặt 05:00 trùng Sáng: khung Sáng dài 0 phút.
        val r = MealTimesRules.withStart(d, 1, t(5))
        assertEquals(setOf(0), MealTimesRules.shadowed(r))
        assertEquals(MealType.LUNCH, MealTypeResolver(r).fromTime(t(5)))
        assertEquals(MealType.LUNCH, MealTypeResolver(r).fromTime(t(9, 59)))
        assertEquals(1, MealTimesRules.winnerOf(r, 0))
    }

    @Test fun `khung ket thuc o moc ke tiep theo gio`() {
        assertEquals(t(10), MealTimesRules.endOf(d, 0))
        assertEquals(t(5), MealTimesRules.endOf(d, 4)) // Phụ đêm 22:00 → 05:00
    }
}
