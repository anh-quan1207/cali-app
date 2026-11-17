package com.cali.core.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** 08 §1, §6: thông báo sáng Thứ Hai, thẻ S10 Thứ Hai–Thứ Ba, tuần mặc định của S35. */
class WeeklySummaryPlannerTest {

    private val p = WeeklySummaryPlanner
    private val eight = LocalTime.of(8, 0)
    private val monday = LocalDate.of(2026, 11, 16)

    @Test fun `lan bao tiep theo la Thu Hai gan nhat luc 8 gio`() {
        // Thứ Năm 12/11 → Thứ Hai 16/11 08:00
        assertEquals(monday.atTime(eight), p.nextFire(LocalDateTime.of(2026, 11, 12, 20, 15), eight))
    }

    @Test fun `Thu Hai truoc gio bao thi bao ngay hom do, sau gio thi tuan sau`() {
        assertEquals(monday.atTime(eight), p.nextFire(monday.atTime(7, 59), eight))
        assertEquals(monday.plusWeeks(1).atTime(eight), p.nextFire(monday.atTime(8, 0), eight))
        assertEquals(monday.plusWeeks(1).atTime(eight), p.nextFire(monday.atTime(9, 0), eight))
    }

    @Test fun `gio bao do nguoi dung chon`() {
        assertEquals(monday.atTime(21, 30), p.nextFire(monday.atTime(9, 0), LocalTime.of(21, 30)))
    }

    @Test fun `thong bao Thu Hai noi ve tuan vua ket thuc`() {
        assertEquals(LocalDate.of(2026, 11, 9), p.reviewedWeek(monday))
    }

    @Test fun `the tuan truoc tren S10 chi hien Thu Hai va Thu Ba`() {
        assertTrue(p.showLastWeekCard(monday))
        assertTrue(p.showLastWeekCard(monday.plusDays(1)))
        assertFalse(p.showLastWeekCard(monday.plusDays(2)))
        assertFalse(p.showLastWeekCard(monday.minusDays(1)))
    }

    @Test fun `S35 mac dinh tuan truoc neu Thu Hai - Thu Ba, nguoc lai tuan nay`() {
        assertEquals(LocalDate.of(2026, 11, 9), p.defaultWeek(monday.plusDays(1)))
        assertEquals(monday, p.defaultWeek(monday.plusDays(2)))
        assertEquals(monday, p.defaultWeek(monday.plusDays(6)))
    }

    @Test fun `dau tuan cua mot ngay`() {
        assertEquals(LocalDate.of(2026, 11, 9), p.weekStart(LocalDate.of(2026, 11, 15)))
        assertEquals(monday, p.weekStart(monday))
    }
}
