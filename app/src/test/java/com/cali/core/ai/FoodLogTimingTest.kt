package com.cali.core.ai

import com.cali.core.calc.MealTypeResolver
import com.cali.core.calc.model.MealType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

/** 05 §4.1 / 4.3: thời điểm ăn và bữa cho kết quả AI. */
class FoodLogTimingTest {

    private val timing = FoodLogTiming(MealTypeResolver())
    private val today = LocalDate.of(2026, 11, 13)
    private val sentAt = LocalDateTime.of(today, LocalTime.of(12, 35))

    private fun log(date: LocalDate? = null, time: LocalTime? = null, meal: MealType? = null) =
        ValidatedFoodLog(items = emptyList(), mealType = meal, eatenTime = time, eatenDate = date)

    @Test fun `không nói gì → giờ gửi, bữa theo giờ`() {
        val r = timing.resolve(log(), sentAt)
        assertEquals(sentAt, r.eatenAt)
        assertEquals(MealType.LUNCH, r.mealType)
    }

    @Test fun `tối qua ăn mì tôm → hôm qua, giữa khung Tối`() {
        val r = timing.resolve(log(date = today.minusDays(1), meal = MealType.DINNER), sentAt)
        assertEquals(LocalDateTime.of(2026, 11, 12, 19, 45), r.eatenAt)
        assertEquals(MealType.DINNER, r.mealType)
    }

    @Test fun `hôm qua trưa → hôm qua 12 giờ`() {
        val r = timing.resolve(log(date = today.minusDays(1), meal = MealType.LUNCH), sentAt)
        assertEquals(LocalDateTime.of(2026, 11, 12, 12, 0), r.eatenAt)
        assertEquals(MealType.LUNCH, r.mealType)
    }

    @Test fun `sáng nay 7h → hôm nay 07 00, bữa Sáng`() {
        val r = timing.resolve(log(time = LocalTime.of(7, 0)), sentAt)
        assertEquals(LocalDateTime.of(today, LocalTime.of(7, 0)), r.eatenAt)
        assertEquals(MealType.BREAKFAST, r.mealType)
    }

    @Test fun `AI nói rõ bữa thì thắng giờ`() {
        // "bữa trưa muộn" lúc 14:30 → vẫn Trưa
        val r = timing.resolve(log(time = LocalTime.of(14, 30), meal = MealType.LUNCH), sentAt)
        assertEquals(MealType.LUNCH, r.mealType)
        assertEquals(LocalDateTime.of(today, LocalTime.of(14, 30)), r.eatenAt)
    }

    @Test fun `ngày khác có giờ rõ → dùng giờ đó`() {
        val r = timing.resolve(log(date = today.minusDays(1), time = LocalTime.of(22, 30)), sentAt)
        assertEquals(LocalDateTime.of(2026, 11, 12, 22, 30), r.eatenAt)
        assertEquals(MealType.SNACK, r.mealType)
    }

    @Test fun `ngày khác không có bữa lẫn giờ → giờ gửi trên ngày đó`() {
        val r = timing.resolve(log(date = today.minusDays(2)), sentAt)
        assertEquals(LocalDateTime.of(2026, 11, 11, 12, 35), r.eatenAt)
        assertEquals(MealType.LUNCH, r.mealType)
    }

    @Test fun `ngày ghi trùng hôm nay coi như hôm nay`() {
        val r = timing.resolve(log(date = today, meal = MealType.DINNER), sentAt)
        // Hôm nay + không có giờ → giờ gửi, bữa vẫn theo AI.
        assertEquals(sentAt, r.eatenAt)
        assertEquals(MealType.DINNER, r.mealType)
    }

    @Test fun `ảnh thư viện mặc định giờ chụp EXIF`() {
        val shotAt = LocalDateTime.of(today, LocalTime.of(7, 10))
        val r = timing.resolve(log(), sentAt, defaultAt = shotAt)
        assertEquals(shotAt, r.eatenAt)
        assertEquals(MealType.BREAKFAST, r.mealType)
    }
}
