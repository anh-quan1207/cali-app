package com.cali.core.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * GĐ6 (09 §4 GĐ6 mục 1, 6): kỳ tuần / tháng, ngày trước khi dùng app, ngày tương lai, Same-Days Rule.
 * Lịch thật: Thứ Hai 09/11/2026; mockup S32 "tuần này" (hôm nay Thứ Năm) ứng với 12/11/2026.
 */
class PeriodStatsTest {

    private val calc = WeeklyStatsCalculator
    private val monday = LocalDate.of(2026, 11, 9)
    private val thursday = monday.plusDays(3)

    private fun day(date: LocalDate, kcal: Double, entries: Int = 2, water: Int = 2600, protein: Double = 122.0) = DayInput(
        date = date, entryCount = entries,
        kcal = kcal, proteinG = protein, carbG = 196.0, fatG = 52.0,
        waterMl = water,
        targetKcal = 1740, targetProteinG = 122, targetCarbG = 196, targetFatG = 52, targetWaterMl = 2600,
        tdee = 2293.0,
    )

    // S32 f0: tuần này 4/4 ngày ghi, T2 1.710 · T3 1.690 · T4 1.420 · T5 (hôm nay) 1.310.
    private val thisWeek = listOf(1710.0, 1690.0, 1420.0, 1310.0).mapIndexed { i, k -> day(monday.plusDays(i.toLong()), k) }

    @Test fun `S32 f0 tuan dang dien ra - TB 1533, 2 Dat 2 Thieu gom hom nay, con 3 ngay`() {
        val s = calc.compute(weekStart = monday, today = thursday, days = thisWeek)
        assertEquals(4, s.loggedDays)
        assertEquals(1532.5, s.avgKcal!!, 1e-9) // hiển thị 1.533
        assertEquals(6130.0, s.sumEatenKcal, 1e-9)
        assertEquals(2, s.onTargetDays)
        assertEquals(2, s.underDays)
        assertEquals(3, s.remainingDays)
        assertTrue(s.isPartial)
    }

    @Test fun `hom nay dang ghi duoi nua muc tieu chua bi coi la nghi ghi thieu`() {
        // S30 f0: hôm nay (đang ghi) không gắn cờ ⚠; ngày đã qua thì có.
        val days = thisWeek.take(3) + day(thursday, 600.0)
        val s = calc.compute(weekStart = monday, today = thursday, days = days)
        assertFalse(s.days[3].suspectedUnderLog)
        assertEquals(0, s.suspectedUnderLogDays)
        val done = calc.compute(weekStart = monday, today = thursday.plusDays(1), days = days)
        assertTrue(done.days[3].suspectedUnderLog)
    }

    @Test fun `ngay sau hom nay la FUTURE, khong phai chua ghi`() {
        val s = calc.compute(weekStart = monday, today = thursday, days = thisWeek)
        assertEquals(DayStatus.FUTURE, s.days[4].status)
        assertEquals(DayStatus.FUTURE, s.days[6].status)
        assertEquals(0, s.notLoggedDays)
    }

    @Test fun `S30 f4 nguoi moi - ngay truoc khi dung app khong tinh chua ghi, diem chia cho so ngay tu luc bat dau`() {
        // Bắt đầu dùng app 11/11 (Thứ Tư); ghi T4, T5 (Thiếu cả hai).
        val start = monday.plusDays(2)
        val days = listOf(day(start, 1420.0), day(thursday, 1310.0))
        val s = calc.compute(weekStart = monday, today = thursday, days = days, firstUseDate = start)
        assertEquals(DayStatus.BEFORE_START, s.days[0].status)
        assertEquals(DayStatus.BEFORE_START, s.days[1].status)
        assertEquals(DayStatus.UNDER, s.days[2].status)
        assertEquals(2, s.elapsedDays)
        assertEquals(0, s.notLoggedDays)
        assertEquals(20.0, s.score.logging, 1e-9) // 2/2 ngày
        assertFalse(s.averagesReady) // S32 f5: TB khi đủ 3 ngày ghi
    }

    @Test fun `nguoi cu thi TB co ngay ca khi moi ghi 1 ngay`() {
        val s = calc.compute(weekStart = monday, today = monday, days = thisWeek.take(1), firstUseDate = monday.minusDays(30))
        assertTrue(s.averagesReady)
    }

    @Test fun `thang 11 tinh toi hom nay, ngay truoc khi dung app bo qua`() {
        val nov = YearMonth.of(2026, 11)
        val days = (1..12).map { d -> day(nov.atDay(d), 1740.0) }
        val s = calc.computeMonth(nov, today = thursday, days = days, firstUseDate = nov.atDay(3))
        assertEquals(30, s.days.size)
        assertEquals(10, s.elapsedDays) // 03 → 12/11
        assertEquals(10, s.loggedDays)
        assertEquals(DayStatus.BEFORE_START, s.days[0].status)
        assertEquals(DayStatus.FUTURE, s.days[12].status)
        assertEquals(18, s.remainingDays)
        assertEquals(nov.atDay(1), s.weekStart)
    }

    @Test fun `thang da qua khong con ngay nao`() {
        val oct = YearMonth.of(2026, 10)
        val s = calc.computeMonth(oct, today = thursday, days = emptyList())
        assertFalse(s.isPartial)
        assertEquals(31, s.elapsedDays)
        assertEquals(31, s.notLoggedDays)
        assertEquals(0, s.remainingDays)
    }

    @Test fun `tuan chua bat dau dung app thi 0 ngay, khong chia cho 0`() {
        val prevMonday = monday.minusWeeks(1)
        val s = calc.compute(weekStart = prevMonday, today = thursday, days = emptyList(), firstUseDate = monday)
        assertEquals(0, s.elapsedDays)
        assertEquals(0, s.score.total)
        assertEquals(0.0, s.avgWaterMl, 1e-9)
        assertNull(s.avgKcal)
    }

    // ---------- Same-Days Rule (00-direction, S32 f4 / f7) ----------

    @Test fun `tuan dang dien ra so voi cung so ngay dau tuan truoc`() {
        val prevMonday = monday.minusWeeks(1)
        val prev = (0..6).map { i -> day(prevMonday.plusDays(i.toLong()), if (i < 4) 1740.0 else 4000.0) }
        val current = calc.compute(weekStart = monday, today = thursday, days = thisWeek)
        val cmp = calc.compareSameDays(current, prevMonday, 7, prev, today = thursday)
        assertTrue(cmp.enough)
        assertEquals(4, cmp.previous.elapsedDays)
        assertEquals(1740.0, cmp.previous.avgKcal!!, 1e-9)
        assertEquals(Trend.DOWN, cmp.trends!!.avgKcal)
    }

    @Test fun `tuan truoc duoi 3 ngay ghi o cac thu tuong ung thi khong so`() {
        val prevMonday = monday.minusWeeks(1)
        val prev = listOf(day(prevMonday, 1740.0), day(prevMonday.plusDays(1), 1740.0), day(prevMonday.plusDays(5), 1740.0))
        val current = calc.compute(weekStart = monday, today = thursday, days = thisWeek)
        val cmp = calc.compareSameDays(current, prevMonday, 7, prev, today = thursday)
        assertFalse(cmp.enough)
        assertNull(cmp.trends)
    }

    @Test fun `ky truoc chi tinh ngay sau khi bat dau dung app`() {
        val prevMonday = monday.minusWeeks(1)
        val prev = (0..3).map { i -> day(prevMonday.plusDays(i.toLong()), 1740.0) }
        val current = calc.compute(weekStart = monday, today = thursday, days = thisWeek)
        val cmp = calc.compareSameDays(current, prevMonday, 7, prev, today = thursday, firstUseDate = prevMonday.plusDays(2))
        assertEquals(2, cmp.previous.loggedDays)
        assertFalse(cmp.enough)
    }

    @Test fun `thang dang dien ra so voi cung so ngay dau thang truoc, can 7 ngay ghi`() {
        val nov = YearMonth.of(2026, 11)
        val oct = YearMonth.of(2026, 10)
        val current = calc.computeMonth(nov, today = thursday, days = (1..12).map { day(nov.atDay(it), 1700.0) })
        val octDays = (1..31).map { day(oct.atDay(it), if (it <= 12) 1800.0 else 3000.0) }
        val cmp = calc.compareSameDays(current, oct.atDay(1), 31, octDays, today = thursday, minLoggedDays = 7)
        assertTrue(cmp.enough)
        assertEquals(12, cmp.previous.elapsedDays)
        assertEquals(1800.0, cmp.previous.avgKcal!!, 1e-9)
        assertEquals(Trend.DOWN, cmp.trends!!.avgKcal)

        val few = octDays.take(6)
        assertFalse(calc.compareSameDays(current, oct.atDay(1), 31, few, today = thursday, minLoggedDays = 7).enough)
    }

    @Test fun `thang 31 ngay so voi thang truoc ngan hon thi cat o cuoi thang truoc`() {
        val dec = YearMonth.of(2026, 12)
        val nov = YearMonth.of(2026, 11)
        val dec31 = dec.atDay(31)
        val current = calc.computeMonth(dec, today = dec31, days = emptyList())
        val cmp = calc.compareSameDays(current, nov.atDay(1), 30, (1..30).map { day(nov.atDay(it), 1740.0) }, today = dec31, minLoggedDays = 7)
        assertEquals(30, cmp.previous.elapsedDays)
        assertNotNull(cmp.trends)
    }
}
