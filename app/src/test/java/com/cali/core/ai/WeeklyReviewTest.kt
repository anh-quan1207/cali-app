package com.cali.core.ai

import com.cali.core.calc.DayInput
import com.cali.core.calc.FoodContribution
import com.cali.core.calc.WeeklyStatsCalculator
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.MealType
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** 08 §5: tool `submit_weekly_review`, kiểm tra đầu ra, khối `<weekly_stats>`. */
class WeeklyReviewTest {

    private fun parse(s: String) = Json.parseToJsonElement(s)

    @Test fun `tool bat buoc headline, highlights, issues, suggestions, tone`() {
        val schema = WeeklyReviewTool.inputSchema.toString()
        assertEquals("submit_weekly_review", WeeklyReviewTool.NAME)
        listOf("headline", "highlights", "issues", "patterns", "suggestions", "CELEBRATE", "ENCOURAGE", "WARN")
            .forEach { assertTrue(it, schema.contains(it)) }
    }

    @Test fun `hop le - giu nguyen noi dung`() {
        val r = WeeklyReviewValidator.validate(
            parse(
                """{"headline":"Ngày thường đều tay","highlights":["4/7 ngày đạt"],"issues":["T7, CN vượt"],
                "patterns":["cuối tuần ăn vượt"],"suggestions":["a","b","c"],"tone":"ENCOURAGE"}""",
            ),
        )!!
        assertEquals("Ngày thường đều tay", r.headline)
        assertEquals(listOf("a", "b", "c"), r.suggestions)
        assertEquals(ReviewTone.ENCOURAGE, r.tone)
        assertEquals(listOf("cuối tuần ăn vượt"), r.patterns)
    }

    @Test fun `cat bot qua gioi han, bo dong rong, thieu patterns thi rong`() {
        val r = WeeklyReviewValidator.validate(
            parse(
                """{"headline":"  X  ","highlights":["1","2","3","4"],"issues":[" ","i"],
                "suggestions":["a","b","c","d"],"tone":"WARN"}""",
            ),
        )!!
        assertEquals("X", r.headline)
        assertEquals(3, r.highlights.size)
        assertEquals(listOf("i"), r.issues)
        assertEquals(emptyList<String>(), r.patterns)
        assertEquals(3, r.suggestions.size)
    }

    @Test fun `tone la thi coi la ENCOURAGE`() {
        val r = WeeklyReviewValidator.validate(parse("""{"headline":"X","highlights":[],"issues":[],"suggestions":["a"],"tone":"HAPPY"}"""))!!
        assertEquals(ReviewTone.ENCOURAGE, r.tone)
    }

    @Test fun `thieu headline hoac khong co goi y nao thi khong hop le`() {
        assertNull(WeeklyReviewValidator.validate(parse("""{"highlights":[],"issues":[],"suggestions":["a"],"tone":"WARN"}""")))
        assertNull(WeeklyReviewValidator.validate(parse("""{"headline":"X","highlights":[],"issues":[],"suggestions":[],"tone":"WARN"}""")))
        assertNull(WeeklyReviewValidator.validate(parse("""[1,2]""")))
    }

    @Test fun `luu va doc lai JSON`() {
        val r = WeeklyReview("H", listOf("a"), listOf("b"), listOf("c"), listOf("1", "2", "3"), ReviewTone.CELEBRATE)
        assertEquals(r, WeeklyReview.decode(r.encode()))
        assertNull(WeeklyReview.decode("rác"))
    }

    // ---------- 08 §5.4 ----------

    private val monday = LocalDate.of(2026, 11, 9)
    private val kcal = listOf(1710.0, 1760.0, 690.0, 1800.0, 1650.0, 2360.0, 2630.0)
    private val water = listOf(2600, 2600, 1100, 2600, 2600, 1100, 2600)
    private val protein = listOf(110.0, 105.0, 40.0, 115.0, 118.0, 120.0, 120.0)
    private val week = (0..6).map { i ->
        DayInput(
            monday.plusDays(i.toLong()), 3, kcal[i], protein[i], 231.0, 55.0, water[i],
            1740, 122, 196, 52, 2600, 16050.0 / 7,
        )
    }

    @Test fun `khoi weekly_stats co cac dong chinh theo 08 5-4`() {
        val stats = WeeklyStatsCalculator.compute(
            monday, monday.plusDays(7), week,
            foods = listOf(
                FoodContribution("Trà sữa trân châu", 400.0, MealType.SNACK, monday),
                FoodContribution("Trà sữa trân châu", 400.0, MealType.SNACK, monday.plusDays(5)),
            ),
            weights = listOf(74.3, 74.0, 74.0), previousWeekAvgWeightKg = 74.6,
        )
        val text = WeeklyStatsPrompt.build(
            stats, GoalType.LOSE, weeklyRateKg = 0.5, comparison = null,
            dayFoods = mapOf(monday to listOf("Phở bò" to 450.0)),
        )
        listOf(
            "Tuần: 09/11/2026 – 15/11/2026 (đã kết thúc)",
            "giảm cân, 0,5 kg/tuần, mục tiêu ngày 1740 kcal, đạm 122 g, nước 2600 ml",
            "Điểm tuần: 71/100 (Calo 23/40, Đạm 17/20, Nước 14/20, Ghi chép 17/20)",
            "đã ghi 7/7 ngày (1 ngày nghi ghi thiếu: T4 690 kcal)",
            "Đạt 4, Vượt 2 (T7 +620, CN +890), Thiếu 1",
            "+420 kcal",
            "−3450 kcal",
            "Nước: 15,2/18,2 L, đạt 5/7 ngày",
            "Cân: TB tuần 74,1 kg (tuần trước 74,6 → −0,5 kg)",
            "Trà sữa trân châu ×2 (800 kcal)",
            "T2 1710 | T3 1760 | T4 690⚠ | T5 1800 | T6 1650 | T7 2360 | CN 2630",
            "T2: Phở bò 450",
        ).forEach { assertTrue("thiếu: $it\n$text", text.contains(it)) }
    }

    @Test fun `tuan dang dien ra ghi tam tinh`() {
        val stats = WeeklyStatsCalculator.compute(monday, monday.plusDays(3), week)
        val text = WeeklyStatsPrompt.build(stats, GoalType.LOSE, 0.5, null, emptyMap())
        assertTrue(text, text.contains("(đang diễn ra, đã qua 4/7 ngày)"))
    }
}
