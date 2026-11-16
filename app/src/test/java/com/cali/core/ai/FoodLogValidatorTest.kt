package com.cali.core.ai

import com.cali.core.ai.FoodLogValidator.Outcome
import com.cali.core.calc.AmberFlag
import com.cali.core.calc.model.MealType
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodLogValidatorTest {

    private val today = LocalDate.of(2026, 11, 13)

    private fun item(
        name: String = "Phở bò",
        grams: String = "500",
        kcal: String = "450",
        p: String = "25",
        c: String = "60",
        f: String = "12",
        confidence: String = "0.9",
        extra: String = "",
    ) = """{"name":"$name","portion_desc":"1 tô vừa","grams":$grams,"kcal":$kcal,
        "protein_g":$p,"carb_g":$c,"fat_g":$f,"confidence":$confidence$extra}"""

    private fun valid(json: String): ValidatedFoodLog {
        val out = FoodLogValidator.validate(json, today)
        assertTrue("phải hợp lệ: $out", out is Outcome.Valid)
        return (out as Outcome.Valid).log
    }

    private fun assertInvalid(json: String) {
        val out = FoodLogValidator.validate(json, today)
        assertTrue("phải không hợp lệ: $out", out is Outcome.Invalid)
    }

    @Test fun `món hợp lệ giữ nguyên số, không gắn cờ`() {
        val log = valid("""{"items":[${item(extra = ""","assumptions":"nước béo vừa"""")}]}""")
        val it = log.items.single()
        assertEquals("Phở bò", it.name)
        assertEquals("1 tô vừa", it.portionDesc)
        assertEquals(500.0, it.grams, 0.0)
        assertEquals(450.0, it.kcal, 0.0)
        assertEquals(25.0, it.proteinG, 0.0)
        assertEquals(0.9, it.confidence, 0.0)
        assertEquals("nước béo vừa", it.assumptions)
        assertTrue(it.flags.isEmpty())
        assertNull(log.mealType)
        assertNull(log.eatenDate)
        assertNull(log.eatenTime)
    }

    // --- JSON hỏng / sai schema → Invalid (provider gọi lại 1 lần) ---

    @Test fun `chuỗi không phải JSON`() = assertInvalid("""{"items":[{"name":"Phở""")

    @Test fun `không phải object`() = assertInvalid("""[1,2]""")

    @Test fun `thiếu items`() = assertInvalid("""{"comment":"x"}""")

    @Test fun `items không phải mảng`() = assertInvalid("""{"items":"phở"}""")

    @Test fun `thiếu trường bắt buộc trong món`() =
        assertInvalid("""{"items":[{"name":"Phở","portion_desc":"1 tô","grams":500,"kcal":450,"protein_g":25,"carb_g":60,"confidence":0.9}]}""")

    @Test fun `số là chuỗi chữ`() = assertInvalid("""{"items":[${item(kcal = "\"nhiều\"")}]}""")

    @Test fun `tên món rỗng`() = assertInvalid("""{"items":[${item(name = "  ")}]}""")

    @Test fun `null cho trường bắt buộc`() = assertInvalid("""{"items":[${item(kcal = "null")}]}""")

    // --- items rỗng ---

    @Test fun `items rỗng là hợp lệ, giữ câu hỏi và nhận xét`() {
        val log = valid("""{"items":[],"comment":"Ảnh không có đồ ăn","clarifying_question":"Bạn chụp lại được không?"}""")
        assertTrue(log.isEmpty)
        assertEquals("Ảnh không có đồ ăn", log.comment)
        assertEquals("Bạn chụp lại được không?", log.clarifyingQuestion)
    }

    // --- số âm / NaN ---

    @Test fun `số âm đặt về 0 và gắn cờ`() {
        val it = valid("""{"items":[${item(f = "-3", grams = "-10")}]}""").items.single()
        assertEquals(0.0, it.fatG, 0.0)
        assertEquals(0.0, it.grams, 0.0)
        assertTrue(AmberFlag.INVALID_NUMBER_FIXED in it.flags)
    }

    @Test fun `số âm chỉ ở grams cũng gắn cờ`() {
        val it = valid("""{"items":[${item(grams = "-1")}]}""").items.single()
        assertEquals(0.0, it.grams, 0.0)
        assertEquals(setOf(AmberFlag.INVALID_NUMBER_FIXED), it.flags)
    }

    // --- Amber Check ---

    @Test fun `lệch macro hơn 20 phần trăm gắn cờ nhưng giữ kcal của AI`() {
        // 4*10 + 4*20 + 9*5 = 165; |300-165|/300 = 45%
        val it = valid("""{"items":[${item(kcal = "300", p = "10", c = "20", f = "5")}]}""").items.single()
        assertEquals(300.0, it.kcal, 0.0)
        assertTrue(AmberFlag.MACRO_MISMATCH in it.flags)
    }

    @Test fun `lệch đúng 20 phần trăm chưa gắn cờ`() {
        // 4*20 + 4*20 + 9*0 = 160; |200-160|/200 = 20%
        val it = valid("""{"items":[${item(kcal = "200", p = "20", c = "20", f = "0")}]}""").items.single()
        assertTrue(it.flags.isEmpty())
    }

    @Test fun `món trên 2500 kcal gắn cờ cao`() {
        val it = valid("""{"items":[${item(kcal = "2600", p = "100", c = "300", f = "110")}]}""").items.single()
        assertTrue(AmberFlag.VERY_HIGH in it.flags)
    }

    @Test fun `đúng 2500 kcal chưa gắn cờ cao`() {
        val it = valid("""{"items":[${item(kcal = "2500", p = "100", c = "300", f = "100")}]}""").items.single()
        assertTrue(AmberFlag.VERY_HIGH !in it.flags)
    }

    // --- làm tròn ---

    @Test fun `kcal bội số 5, gram và macro một chữ số thập phân`() {
        val it = valid("""{"items":[${item(kcal = "452.4", grams = "123.456", p = "25.26", c = "60.04", f = "12.36")}]}""").items.single()
        assertEquals(450.0, it.kcal, 0.0)
        assertEquals(123.5, it.grams, 1e-9)
        assertEquals(25.3, it.proteinG, 1e-9)
        assertEquals(60.0, it.carbG, 1e-9)
        assertEquals(12.4, it.fatG, 1e-9)
    }

    @Test fun `kcal 453 làm tròn lên 455`() {
        val it = valid("""{"items":[${item(kcal = "453")}]}""").items.single()
        assertEquals(455.0, it.kcal, 0.0)
    }

    @Test fun `confidence ngoài 0-1 bị kẹp`() {
        val items = valid("""{"items":[${item(confidence = "1.4")},${item(confidence = "-0.2")}]}""").items
        assertEquals(1.0, items[0].confidence, 0.0)
        assertEquals(0.0, items[1].confidence, 0.0)
    }

    // --- eaten_date / eaten_time / meal_type ---

    @Test fun `đọc eaten_date eaten_time meal_type hợp lệ`() {
        val log = valid("""{"items":[${item()}],"eaten_date":"2026-11-12","eaten_time":"19:30","meal_type":"DINNER"}""")
        assertEquals(LocalDate.of(2026, 11, 12), log.eatenDate)
        assertEquals(LocalTime.of(19, 30), log.eatenTime)
        assertEquals(MealType.DINNER, log.mealType)
    }

    @Test fun `giờ một chữ số vẫn đọc được`() {
        val log = valid("""{"items":[${item()}],"eaten_time":"7:05"}""")
        assertEquals(LocalTime.of(7, 5), log.eatenTime)
    }

    @Test fun `eaten_date sai định dạng bị bỏ qua`() {
        assertNull(valid("""{"items":[${item()}],"eaten_date":"hôm qua"}""").eatenDate)
        assertNull(valid("""{"items":[${item()}],"eaten_date":"12/11/2026"}""").eatenDate)
    }

    @Test fun `eaten_date ở tương lai bị bỏ qua`() {
        assertNull(valid("""{"items":[${item()}],"eaten_date":"2026-11-14"}""").eatenDate)
    }

    @Test fun `eaten_date là hôm nay được giữ`() {
        assertEquals(today, valid("""{"items":[${item()}],"eaten_date":"2026-11-13"}""").eatenDate)
    }

    @Test fun `eaten_time sai bị bỏ qua`() {
        assertNull(valid("""{"items":[${item()}],"eaten_time":"25:00"}""").eatenTime)
        assertNull(valid("""{"items":[${item()}],"eaten_time":"tối"}""").eatenTime)
    }

    @Test fun `meal_type ngoài enum bị bỏ qua`() {
        assertNull(valid("""{"items":[${item()}],"meal_type":"BRUNCH"}""").mealType)
    }

    @Test fun `chuỗi rỗng ở trường tuỳ chọn coi như không có`() {
        val log = valid("""{"items":[${item(extra = ""","assumptions":"" """)}],"comment":"","clarifying_question":" "}""")
        assertNull(log.comment)
        assertNull(log.clarifyingQuestion)
        assertNull(log.items.single().assumptions)
    }

    @Test fun `trường lạ không làm hỏng`() {
        valid("""{"items":[${item(extra = ""","brand":"x"""")}],"note":1}""")
    }
}
