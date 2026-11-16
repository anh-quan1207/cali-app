package com.cali.core.ai

import com.cali.core.calc.AmberFlag
import com.cali.core.calc.model.MealType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiDraftTest {

    private fun item(name: String, conf: Double, flags: Set<AmberFlag> = emptySet(), assumptions: String? = null) =
        ValidatedFoodItem(name, "1 phần", 100.0, 200.0, 10.0, 20.0, 5.0, conf, assumptions, flags)

    private val log = ValidatedFoodLog(
        items = listOf(
            item("Cơm tấm sườn", 0.85),
            item("Mỡ hành + nước mắm", 0.4, assumptions = "~2 thìa mỡ hành"),
            item("Canh chua", 0.7, setOf(AmberFlag.MACRO_MISMATCH)),
        ),
        mealType = MealType.DINNER,
        eatenTime = LocalTime.of(19, 5),
        eatenDate = LocalDate.of(2026, 11, 15),
        clarifyingQuestion = "Bạn có chan hết chén nước mắm không?",
        comment = "Bữa tối khá đậm.",
    )
    private val timing = ResolvedTiming(LocalDateTime.of(2026, 11, 15, 19, 5), MealType.DINNER)

    @Test fun `JSON thẻ món - ghi rồi đọc lại giống hệt, kèm giờ và bữa đã tính`() {
        val json = AiFoodLogJson.encode(log, timing)
        val back = AiFoodLogJson.decode(json)!!
        assertEquals(log, back.log)
        assertEquals(timing, back.timing)
        assertFalse(back.mealManual)
    }

    @Test fun `JSON thẻ món - trường rỗng và bữa người dùng chọn`() {
        val bare = ValidatedFoodLog(items = listOf(item("Phở bò", 0.9)))
        val t = ResolvedTiming(LocalDateTime.of(2026, 11, 16, 12, 35), MealType.LUNCH)
        val back = AiFoodLogJson.decode(AiFoodLogJson.encode(bare, t, mealManual = true))!!
        assertEquals(bare, back.log)
        assertNull(back.log.mealType)
        assertTrue(back.mealManual)
    }

    @Test fun `JSON hỏng thì trả null, không ném`() {
        assertNull(AiFoodLogJson.decode("{khong phai json"))
        assertNull(AiFoodLogJson.decode(null))
    }

    @Test fun `Cần kiểm tra khi độ tin cậy dưới 0,5 (03 §6)`() {
        assertTrue(AiReview.needsCheck(item("a", 0.49)))
        assertFalse(AiReview.needsCheck(item("a", 0.5)))
        assertFalse(AiReview.needsCheck(item("a", 0.9, setOf(AmberFlag.VERY_HIGH))))
    }

    @Test fun `món cần kiểm tra hoặc có cờ lên đầu, còn lại giữ thứ tự`() {
        val order = AiReview.reviewOrder(log.items + item("Trà đá", 0.95)).map { it.name }
        assertEquals(listOf("Mỡ hành + nước mắm", "Canh chua", "Cơm tấm sườn", "Trà đá"), order)
    }
}
