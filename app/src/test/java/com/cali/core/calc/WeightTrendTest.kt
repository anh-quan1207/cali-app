package com.cali.core.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * S31 (mockup f0) với lịch thật: ngày mockup lùi 1 ngày (mockup 13/11 = Thứ Năm, thật là 12/11).
 * 16 lần cân, tuần đầu 76,0, mục tiêu 68 kg, tốc độ kế hoạch 0,5 kg/tuần.
 */
class WeightTrendTest {

    private val calc = WeightTrendCalculator
    private val today = LocalDate.of(2026, 11, 12)
    private fun d(day: Int, month: Int) = LocalDate.of(2026, month, day)

    private val entries = listOf(
        WeightPoint(d(28, 9), 76.0), WeightPoint(d(30, 9), 75.9), WeightPoint(d(2, 10), 75.8),
        WeightPoint(d(5, 10), 75.9), WeightPoint(d(8, 10), 75.5),
        WeightPoint(d(12, 10), 75.6), WeightPoint(d(14, 10), 75.3), WeightPoint(d(17, 10), 75.3),
        WeightPoint(d(20, 10), 75.0), WeightPoint(d(24, 10), 75.0),
        WeightPoint(d(26, 10), 74.8), WeightPoint(d(29, 10), 74.5), WeightPoint(d(31, 10), 74.5),
        WeightPoint(d(3, 11), 74.3), WeightPoint(d(5, 11), 73.9), WeightPoint(d(7, 11), 74.1),
    )

    @Test fun `TB tung tuan, xep tu cu toi moi`() {
        val weeks = calc.weeklyAverages(entries, today)
        assertEquals(6, weeks.size)
        assertEquals(d(28, 9), weeks.first().weekStart)
        assertEquals(75.7, weeks[1].avgKg, 1e-9)
        assertEquals(75.4, weeks[2].avgKg, 1e-9)
        assertEquals(74.1, weeks.last().avgKg, 1e-9)
        assertEquals(3, weeks.last().count)
    }

    @Test fun `so lon la TB tuan gan nhat co du lan can, so voi tuan truoc -0,5`() {
        val h = calc.headline(entries, today)!!
        assertEquals(d(2, 11), h.week.weekStart)
        assertEquals(74.1, h.week.avgKg, 1e-9)
        assertEquals(74.6, h.previous!!.avgKg, 1e-9)
        assertEquals(-0.5, h.changeKg!!, 1e-9)
    }

    @Test fun `tuan nay chi thanh so TB khi co it nhat 2 lan can`() {
        val one = entries + WeightPoint(d(10, 11), 73.8)
        assertEquals(d(2, 11), calc.headline(one, today)!!.week.weekStart)
        val two = one + WeightPoint(d(12, 11), 73.6)
        val h = calc.headline(two, today)!!
        assertEquals(d(9, 11), h.week.weekStart)
        assertEquals(73.7, h.week.avgKg, 1e-9)
        // Tuần này chưa đủ 2 lần thì không có trong danh sách TB tuần.
        assertEquals(6, calc.weeklyAverages(one, today).size)
    }

    @Test fun `tien do - da giam 1,9, con 6,1, du kien 06-02-2027 tinh tu hom nay`() {
        val p = calc.progress(entries, targetKg = 68.0, weeklyRateKg = 0.5, today = today)!!
        assertEquals(76.0, p.startKg, 1e-9)
        assertEquals(74.1, p.latestKg, 1e-9)
        assertEquals(1.9, p.changedKg, 1e-9)
        assertEquals(6.1, p.remainingKg, 1e-9)
        assertEquals(LocalDate.of(2027, 2, 6), p.eta)
        assertEquals(12, p.etaWeeks)
    }

    @Test fun `30 ngay - TB tuan 75,4 toi 74,1, toc do -0,43 kg moi tuan`() {
        val r = calc.range(entries, today, days = 30)!!
        assertEquals(75.4, r.firstAvgKg, 1e-9)
        assertEquals(74.1, r.lastAvgKg, 1e-9)
        assertEquals(-1.3, r.changeKg, 1e-9)
        assertEquals(-0.4333, r.kgPerWeek!!, 1e-4)
        assertEquals(entries.count { !it.date.isBefore(today.minusDays(29)) }, r.points.size)
    }

    @Test fun `7 ngay chi mot tuan thi khong co toc do`() {
        val r = calc.range(entries, today, days = 7)!!
        assertNull(r.kgPerWeek)
    }

    @Test fun `tat ca - tu lan can dau`() {
        val r = calc.range(entries, today, days = null)!!
        assertEquals(16, r.points.size)
        assertEquals(75.9, r.firstAvgKg, 1e-9) // TB tuần 28/09: (76,0 + 75,9 + 75,8) / 3
    }

    @Test fun `chua co lan can nao`() {
        assertNull(calc.headline(emptyList(), today))
        assertNull(calc.progress(emptyList(), 68.0, 0.5, today))
        assertNull(calc.range(emptyList(), today, 30))
    }
}
