package com.cali.core.calc

import com.cali.core.calc.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Ví dụ 08 §5.4. Đặc tả ghi "10/11 – 16/11" theo lịch giả định của mockup (13/11 = Thứ Năm);
 * test dùng lịch thật: tuần Thứ Hai 09/11/2026 → Chủ Nhật 15/11/2026, cùng số liệu.
 */
class WeeklyStatsCalculatorTest {

    private val calc = WeeklyStatsCalculator
    private val monday = LocalDate.of(2026, 11, 9)
    private val sunday = monday.plusDays(6)

    /** TDEE ngày sao cho Σ(kcal − TDEE) = 12.600 − 7 × TDEE = −3.450. */
    private val tdee = 16050.0 / 7

    private val kcal = listOf(1710.0, 1760.0, 690.0, 1800.0, 1650.0, 2360.0, 2630.0)
    private val protein = listOf(110.0, 105.0, 40.0, 115.0, 118.0, 120.0, 120.0) // Σ 728 → TB 104
    private val carb = listOf(220.0, 230.0, 90.0, 240.0, 210.0, 300.0, 327.0) // Σ 1617 → TB 231
    private val fat = listOf(50.0, 55.0, 25.0, 60.0, 55.0, 65.0, 75.0) // Σ 385 → TB 55
    private val water = listOf(2600, 2600, 1100, 2600, 2600, 1100, 2600) // Σ 15.200, đạt 5 ngày

    private fun day(i: Int, entries: Int = 3) = DayInput(
        date = monday.plusDays(i.toLong()),
        entryCount = entries,
        kcal = kcal[i], proteinG = protein[i], carbG = carb[i], fatG = fat[i],
        waterMl = water[i],
        targetKcal = 1740, targetProteinG = 122, targetCarbG = 196, targetFatG = 52, targetWaterMl = 2600,
        tdee = tdee,
    )

    private val week = (0..6).map { day(it) }

    private val foods = listOf(
        FoodContribution("Trà sữa trân châu", 400.0, MealType.SNACK, monday),
        FoodContribution("Trà sữa trân châu", 400.0, MealType.SNACK, monday.plusDays(5)),
        FoodContribution("Trà sữa trân châu", 400.0, MealType.SNACK, monday.plusDays(6)),
        FoodContribution("Cơm tấm sườn", 650.0, MealType.LUNCH, monday.plusDays(1)),
        FoodContribution("Cơm tấm sườn", 650.0, MealType.LUNCH, monday.plusDays(6)),
        FoodContribution("Phở bò", 450.0, MealType.BREAKFAST, monday.plusDays(2)),
    )

    private fun full() = calc.compute(
        weekStart = monday, today = sunday.plusDays(1), days = week, foods = foods,
        weights = listOf(74.3, 74.0, 74.0), previousWeekAvgWeightKg = 74.6,
    )

    // ---------- 08 §2 ----------

    @Test fun `bien Dat - Vuot - Thieu 10 phan tram`() {
        assertEquals(DayStatus.ON_TARGET, calc.dayStatus(2, 1914.0, 1740))
        assertEquals(DayStatus.OVER, calc.dayStatus(2, 1915.0, 1740))
        assertEquals(DayStatus.ON_TARGET, calc.dayStatus(2, 1566.0, 1740))
        assertEquals(DayStatus.UNDER, calc.dayStatus(2, 1565.0, 1740))
        assertEquals(DayStatus.NOT_LOGGED, calc.dayStatus(0, 0.0, 1740))
    }

    // ---------- 08 §5.4 ----------

    @Test fun `5-4 Dat 4 Vuot 2 Thieu 1, da ghi 7 ngay`() {
        val s = full()
        assertEquals(7, s.loggedDays)
        assertEquals(0, s.notLoggedDays)
        assertEquals(4, s.onTargetDays)
        assertEquals(2, s.overDays)
        assertEquals(1, s.underDays)
        assertFalse(s.isPartial)
    }

    @Test fun `5-4 T4 690 kcal nghi ghi thieu`() {
        val s = full()
        assertEquals(1, s.suspectedUnderLogDays)
        assertTrue(s.days[2].suspectedUnderLog)
        assertEquals(DayStatus.UNDER, s.days[2].status)
    }

    @Test fun `5-4 chenh lech T7 +620, CN +890, tong +420`() {
        val s = full()
        assertEquals(620.0, s.days[5].delta!!, 1e-9)
        assertEquals(890.0, s.days[6].delta!!, 1e-9)
        assertEquals(420.0, s.sumDeltaKcal, 1e-9)
        assertEquals(12180.0, s.sumTargetKcal, 1e-9)
        assertEquals(12600.0, s.sumEatenKcal, 1e-9)
        assertEquals(1800.0, s.avgKcal!!, 1e-9)
        assertEquals(sunday, s.maxDay!!.date)
        assertEquals(monday.plusDays(2), s.minDay!!.date)
    }

    @Test fun `5-4 so voi TDEE -3450 kcal tuong duong -0,45 kg`() {
        val s = full()
        assertEquals(-3450.0, s.tdeeBalanceKcal, 1e-6)
        assertEquals(-0.45, s.expectedWeightChangeKg, 0.005)
    }

    @Test fun `5-4 macro TB 104 - 231 - 55`() {
        val s = full()
        assertEquals(104.0, s.avgProteinG!!, 1e-9)
        assertEquals(231.0, s.avgCarbG!!, 1e-9)
        assertEquals(55.0, s.avgFatG!!, 1e-9)
        assertEquals(122.0, s.avgTargetProteinG!!, 1e-9)
    }

    @Test fun `5-4 nuoc 15,2 tren 18,2 L, dat 5 ngay`() {
        val s = full()
        assertEquals(15200, s.waterTotalMl)
        assertEquals(18200, s.waterTargetTotalMl)
        assertEquals(5, s.waterMetDays)
        assertEquals(3000, s.waterShortfallMl)
    }

    @Test fun `5-4 can TB tuan 74,1, tuan truoc 74,6 nen -0,5`() {
        val s = full()
        assertEquals(74.1, s.avgWeightKg!!, 1e-9)
        assertEquals(-0.5, s.weightChangeKg!!, 1e-9)
    }

    @Test fun `5-4 diem 71 = Calo 23 + Dam 17 + Nuoc 14 + Ghi chep 17, xep loai Tot`() {
        val s = full()
        assertEquals(23, Math.round(s.score.calorie).toInt())
        assertEquals(17, Math.round(s.score.protein).toInt())
        assertEquals(14, Math.round(s.score.water).toInt())
        assertEquals(17, Math.round(s.score.logging).toInt())
        assertEquals(71, s.score.total)
        assertEquals(WeekGrade.GOOD, s.grade)
    }

    @Test fun `5-4 top mon gop theo ten, xep theo kcal`() {
        val s = full()
        assertEquals(TopFood("Cơm tấm sườn", 2, 1300.0), s.topFoods[0])
        assertEquals(TopFood("Trà sữa trân châu", 3, 1200.0), s.topFoods[1])
        // Bữa có kcal TB cao nhất (TB trên các ngày có bữa đó): trưa 650 > sáng 450 > phụ 400.
        assertEquals(MealType.LUNCH, s.topMeal)
    }

    // ---------- 08 §4 ----------

    @Test fun `xep loai theo nguong`() {
        assertEquals(WeekGrade.EXCELLENT, calc.grade(85))
        assertEquals(WeekGrade.GOOD, calc.grade(84))
        assertEquals(WeekGrade.GOOD, calc.grade(70))
        assertEquals(WeekGrade.NEEDS_WORK, calc.grade(69))
        assertEquals(WeekGrade.NEEDS_WORK, calc.grade(50))
        assertEquals(WeekGrade.POOR, calc.grade(49))
    }

    @Test fun `tuan dang dien ra chia cho so ngay da qua`() {
        // Hôm nay Thứ Năm: 4 ngày đã qua; ngày Thứ Sáu trở đi bị bỏ qua dù có dữ liệu.
        val thursday = monday.plusDays(3)
        val s = calc.compute(weekStart = monday, today = thursday, days = week)
        assertTrue(s.isPartial)
        assertEquals(4, s.elapsedDays)
        assertEquals(4, s.loggedDays)
        // Đạt 3/4 (T2, T3, T5), nước 3/4, ghi chép đều 3/4, đạm TB (110+105+40+115)/4 = 92,5
        assertEquals(40.0 * 3 / 4, s.score.calorie, 1e-9)
        assertEquals(20.0 * 3 / 4, s.score.water, 1e-9)
        assertEquals(20.0 * 3 / 4, s.score.logging, 1e-9)
        assertEquals(20.0 * 92.5 / 122, s.score.protein, 1e-9)
    }

    @Test fun `ngay chua ghi khong tinh vao tong nhung duoc dem`() {
        val days = week.mapIndexed { i, d -> if (i == 4) d.copy(entryCount = 0, kcal = 0.0, proteinG = 0.0, carbG = 0.0, fatG = 0.0) else d }
        val s = calc.compute(weekStart = monday, today = sunday, days = days)
        assertEquals(6, s.loggedDays)
        assertEquals(1, s.notLoggedDays)
        assertEquals(DayStatus.NOT_LOGGED, s.days[4].status)
        assertNull(s.days[4].delta)
        assertEquals(12600.0 - 1650.0, s.sumEatenKcal, 1e-9)
        assertEquals(1740.0 * 6, s.sumTargetKcal, 1e-9)
    }

    @Test fun `ngay thieu trong danh sach coi nhu chua ghi`() {
        val s = calc.compute(weekStart = monday, today = sunday, days = week.filter { it.date != monday })
        assertEquals(7, s.days.size)
        assertEquals(DayStatus.NOT_LOGGED, s.days[0].status)
        assertEquals(6, s.loggedDays)
    }

    @Test fun `khong co ban ghi can thi can TB null`() {
        val s = calc.compute(weekStart = monday, today = sunday, days = week)
        assertNull(s.avgWeightKg)
        assertNull(s.weightChangeKg)
    }

    // ---------- 08 §3.6 + Same-Days Rule ----------

    @Test fun `so tuan truoc chi dung cung so ngay`() {
        val thursday = monday.plusDays(3)
        val current = calc.compute(weekStart = monday, today = thursday, days = week)
        val prevMonday = monday.minusWeeks(1)
        // Tuần này 4 ngày đầu TB 1.490 kcal. Tuần trước 4 ngày đầu ăn đúng 1.740;
        // 3 ngày cuối 4.000 kcal phải bị bỏ qua theo Same-Days Rule (nếu tính thì TB tuần trước ~2.700).
        val prev = (0..6).map { i ->
            day(i).copy(
                date = prevMonday.plusDays(i.toLong()),
                kcal = if (i < 4) 1740.0 else 4000.0,
                proteinG = 122.0,
                waterMl = 2600,
            )
        }
        val cmp = calc.compare(current, prevMonday, prev)
        assertEquals(Trend.DOWN, cmp.avgKcal) // 1490 < 1740
        assertEquals(Trend.DOWN, cmp.onTargetDays) // 3 < 4
        assertEquals(Trend.DOWN, cmp.avgWaterMl)
        assertEquals(Trend.DOWN, cmp.avgProteinG)
        assertEquals(Trend.DOWN, cmp.score)
    }
}
