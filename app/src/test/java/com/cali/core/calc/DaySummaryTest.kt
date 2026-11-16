package com.cali.core.calc

import com.cali.core.calc.model.MealType
import com.cali.core.calc.model.MealType.BREAKFAST
import com.cali.core.calc.model.MealType.DINNER
import com.cali.core.calc.model.MealType.LUNCH
import com.cali.core.calc.model.MealType.SNACK
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DaySummaryTest {

    private var nextId = 1L
    private fun food(hhmm: Int, meal: MealType, kcal: Double, p: Double = 0.0, c: Double = 0.0, f: Double = 0.0) =
        DayFood(id = nextId++, eatenAtMinute = hhmm / 100 * 60 + hhmm % 100, meal = meal, kcal = kcal, proteinG = p, carbG = c, fatG = f)

    /** Dữ liệu mẫu CLAUDE.md: sáng 820 + trưa 490 = 1.310, mục tiêu 1.740 → còn 430. */
    @Test fun `ngay mau sang 820 trua 490 con 430`() {
        val s = DaySummaryCalculator.summarize(
            listOf(food(720, BREAKFAST, 280.0), food(900, BREAKFAST, 540.0), food(1235, LUNCH, 490.0, 33.0, 60.0, 12.0)),
            targetKcal = 1740,
        )
        assertEquals(1310.0, s.totalKcal, 0.0)
        assertEquals(430, s.remainingKcal)
        assertEquals(0, s.overKcal)
        assertEquals(RingStatus.WITHIN, s.status)
        assertEquals(820.0, s.meals.getValue(BREAKFAST).kcal, 0.0)
        assertEquals(2, s.meals.getValue(BREAKFAST).count)
        assertEquals(490.0, s.meals.getValue(LUNCH).kcal, 0.0)
        assertEquals(0, s.meals.getValue(DINNER).count)
        // Hai món Sáng liên tiếp gộp thành một đoạn vòng
        assertEquals(listOf(BREAKFAST to 820.0, LUNCH to 490.0), s.segments.map { it.meal to it.kcal })
    }

    @Test fun `doan vong theo gio an, Phu xuat hien hai lan`() {
        val s = DaySummaryCalculator.summarize(
            listOf(
                food(2230, SNACK, 100.0), // nhập sau nhưng giờ muộn nhất
                food(720, BREAKFAST, 300.0),
                food(1500, SNACK, 150.0),
                food(1230, LUNCH, 500.0),
                food(1900, DINNER, 600.0),
            ),
            targetKcal = 1740,
        )
        assertEquals(
            listOf(BREAKFAST to 300.0, LUNCH to 500.0, SNACK to 150.0, DINNER to 600.0, SNACK to 100.0),
            s.segments.map { it.meal to it.kcal },
        )
        assertEquals(250.0, s.meals.getValue(SNACK).kcal, 0.0)
    }

    @Test fun `vuot nhe 50 kcal la 3 phan tram ho phach`() {
        val s = DaySummaryCalculator.summarize(listOf(food(800, BREAKFAST, 1790.0)), targetKcal = 1740)
        assertEquals(RingStatus.OVER_LIGHT, s.status)
        assertEquals(50, s.overKcal)
        assertEquals(0, s.remainingKcal)
        assertEquals(3, s.overPercent)
    }

    @Test fun `vuot dung 10 phan tram van la nhe, hon la nhieu`() {
        assertEquals(RingStatus.OVER_LIGHT, DaySummaryCalculator.summarize(listOf(food(800, BREAKFAST, 1914.0)), 1740).status)
        assertEquals(RingStatus.OVER_MUCH, DaySummaryCalculator.summarize(listOf(food(800, BREAKFAST, 1915.0)), 1740).status)
    }

    @Test fun `bang muc tieu la trong muc tieu`() {
        val s = DaySummaryCalculator.summarize(listOf(food(800, BREAKFAST, 1740.0)), 1740)
        assertEquals(RingStatus.WITHIN, s.status)
        assertEquals(0, s.remainingKcal)
    }

    @Test fun `macro cong don`() {
        val s = DaySummaryCalculator.summarize(
            listOf(food(720, BREAKFAST, 280.0, 18.0, 35.0, 13.0), food(1235, LUNCH, 490.0, 33.0, 60.0, 12.0)),
            1740,
        )
        assertEquals(51.0, s.proteinG, 1e-9)
        assertEquals(95.0, s.carbG, 1e-9)
        assertEquals(25.0, s.fatG, 1e-9)
    }

    @Test fun `ngay trong`() {
        val s = DaySummaryCalculator.summarize(emptyList(), 1740)
        assertEquals(1740, s.remainingKcal)
        assertEquals(emptyList<RingPart>(), s.segments)
        assertEquals(RingStatus.EMPTY, s.status)
    }

    @Test fun `chua co muc tieu thi khong tinh con lai`() {
        val s = DaySummaryCalculator.summarize(listOf(food(800, BREAKFAST, 500.0)), null)
        assertEquals(RingStatus.NO_GOAL, s.status)
        assertEquals(0, s.remainingKcal)
        assertEquals(500.0, s.totalKcal, 0.0)
    }

    // --- Xem trước khi lưu / sửa (S12, S14): ngày 1.310, món Trưa 490 ---

    @Test fun `S14 x1 len 1 ruoi con 430 thanh 185`() {
        val p = Projection.of(dayKcal = 1310.0, oldKcal = 490.0, newKcal = 735.0, targetKcal = 1740)
        assertEquals(1555.0, p.afterKcal, 0.0)
        assertEquals(1310.0, p.baseKcal, 0.0)
        assertEquals(245.0, p.addedKcal, 0.0)
        assertEquals(0.0, p.removedKcal, 0.0)
        assertEquals(185, p.remainingAfter)
        assertEquals(RingStatus.WITHIN, p.statusAfter)
    }

    @Test fun `S14 x2 vuot nhe 60 kcal 3 phan tram`() {
        val p = Projection.of(1310.0, 490.0, 980.0, 1740)
        assertEquals(1800.0, p.afterKcal, 0.0)
        assertEquals(60, p.overAfter)
        assertEquals(3, p.overPercentAfter)
        assertEquals(RingStatus.OVER_LIGHT, p.statusAfter)
    }

    @Test fun `S14 giam con nua con 430 thanh 675`() {
        val p = Projection.of(1310.0, 490.0, 245.0, 1740)
        assertEquals(1065.0, p.afterKcal, 0.0)
        assertEquals(1065.0, p.baseKcal, 0.0)
        assertEquals(0.0, p.addedKcal, 0.0)
        assertEquals(245.0, p.removedKcal, 0.0)
        assertEquals(675, p.remainingAfter)
    }

    @Test fun `S12 them moi la old bang 0`() {
        // Ảnh s12 khung 1: hôm nay 280 kcal, thêm 490 → còn 970
        val p = Projection.of(280.0, 0.0, 490.0, 1740)
        assertEquals(970, p.remainingAfter)
        assertEquals(280.0, p.baseKcal, 0.0)
        assertEquals(490.0, p.addedKcal, 0.0)
    }
}
