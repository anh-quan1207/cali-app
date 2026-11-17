package com.cali.core.ai

import com.cali.core.calc.DayResult
import com.cali.core.calc.DayStatus
import com.cali.core.calc.Trend
import com.cali.core.calc.WeekComparison
import com.cali.core.calc.WeeklyStats
import com.cali.core.calc.model.GoalType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class ReviewTone { CELEBRATE, ENCOURAGE, WARN }

/** Đầu ra của tool `submit_weekly_review` (08 §5.2) sau khi kiểm tra; lưu ở `weekly_review.review_json`. */
@Serializable
data class WeeklyReview(
    val headline: String,
    val highlights: List<String>,
    val issues: List<String>,
    val patterns: List<String>,
    val suggestions: List<String>,
    val tone: ReviewTone,
) {
    fun encode(): String = JSON.encodeToString(serializer(), this)

    companion object {
        private val JSON = Json { ignoreUnknownKeys = true }

        fun decode(raw: String): WeeklyReview? = runCatching { JSON.decodeFromString(serializer(), raw) }.getOrNull()
    }
}

/** Tool 08 §5.2, dạng OpenAI function (giống `record_food_items`). */
object WeeklyReviewTool {
    const val NAME = "submit_weekly_review"

    const val DESCRIPTION = "Gửi nhận xét tuần có cấu trúc dựa trên <weekly_stats>. Luôn gọi đúng một lần."

    val inputSchema: JsonObject = Json.parseToJsonElement(
        """
        {
          "type": "object",
          "properties": {
            "headline":    { "type": "string", "description": "1 câu tóm tắt tuần, tối đa 20 từ" },
            "highlights":  { "type": "array", "items": { "type": "string" }, "description": "1–3 điểm làm tốt, có số liệu" },
            "issues":      { "type": "array", "items": { "type": "string" }, "description": "1–3 vấn đề chính, có số liệu" },
            "patterns":    { "type": "array", "items": { "type": "string" }, "description": "0–2 quy luật phát hiện, vd 'cuối tuần ăn vượt', 'bữa tối chiếm 50% calo'" },
            "suggestions": { "type": "array", "items": { "type": "string" }, "description": "đúng 3 gợi ý cụ thể, làm được ngay tuần sau" },
            "tone":        { "type": "string", "enum": ["CELEBRATE", "ENCOURAGE", "WARN"] }
          },
          "required": ["headline", "highlights", "issues", "suggestions", "tone"]
        }
        """,
    ).jsonObject
}

/** Kiểm tra đầu ra tool tuần; null = không dùng được (UI hiện lỗi + Thử lại). */
object WeeklyReviewValidator {
    fun validate(input: JsonElement): WeeklyReview? {
        val o = input as? JsonObject ?: return null
        val headline = o.text("headline") ?: return null
        val suggestions = o.list("suggestions", 3)
        if (suggestions.isEmpty()) return null
        val tone = o.text("tone")?.let { t -> ReviewTone.entries.firstOrNull { it.name == t } } ?: ReviewTone.ENCOURAGE
        return WeeklyReview(
            headline = headline,
            highlights = o.list("highlights", 3),
            issues = o.list("issues", 3),
            patterns = o.list("patterns", 2),
            suggestions = suggestions,
            tone = tone,
        )
    }

    private fun JsonObject.text(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content?.trim()?.takeIf { it.isNotEmpty() }

    private fun JsonObject.list(key: String, max: Int): List<String> =
        (this[key] as? JsonArray).orEmpty()
            .mapNotNull { (it as? JsonPrimitive)?.takeIf { p -> p.isString }?.content?.trim()?.takeIf { s -> s.isNotEmpty() } }
            .take(max)
}

/** Khối `<weekly_stats>` (08 §5.4): chỉ số liệu đã tổng hợp + món rút gọn theo ngày, không ảnh. */
object WeeklyStatsPrompt {
    fun build(
        stats: WeeklyStats,
        goalType: GoalType,
        weeklyRateKg: Double,
        comparison: WeekComparison?,
        dayFoods: Map<LocalDate, List<Pair<String, Double>>>,
    ): String = buildString {
        val s = stats
        val end = s.weekStart.plusDays(6)
        val state = if (s.isPartial) "đang diễn ra, đã qua ${s.elapsedDays}/7 ngày" else "đã kết thúc"
        appendLine("<weekly_stats>")
        appendLine("Tuần: ${s.weekStart.format(DMY)} – ${end.format(DMY)} ($state)")
        val goal = goalText(goalType)
        appendLine(
            "Mục tiêu: $goal, ${num1(weeklyRateKg)} kg/tuần, mục tiêu ngày ${targetKcal(s)} kcal, " +
                "đạm ${s.avgTargetProteinG?.let { Math.round(it) } ?: "?"} g, nước ${waterTarget(s)} ml",
        )
        val sc = s.score
        appendLine(
            "Điểm tuần: ${sc.total}/100 (Calo ${Math.round(sc.calorie)}/40, Đạm ${Math.round(sc.protein)}/20, " +
                "Nước ${Math.round(sc.water)}/20, Ghi chép ${Math.round(sc.logging)}/20)",
        )
        val suspected = s.days.filter { it.suspectedUnderLog }
        val suspectedText = if (suspected.isEmpty()) "" else
            " (${suspected.size} ngày nghi ghi thiếu: " + suspected.joinToString(", ") { d ->
                "${dow(d.date)} ${Math.round(kcalOf(s, d))} kcal"
            } + ")"
        appendLine("Calo: đã ghi ${s.loggedDays}/${s.elapsedDays} ngày$suspectedText")
        val over = s.days.filter { it.status == DayStatus.OVER }
        val overText = if (over.isEmpty()) "" else " (" + over.joinToString(", ") { "${dow(it.date)} ${signed(it.delta!!)}" } + ")"
        appendLine("  Đạt ${s.onTargetDays}, Vượt ${s.overDays}$overText, Thiếu ${s.underDays}, Chưa ghi ${s.notLoggedDays}")
        appendLine(
            "  Tổng chênh lệch so với mục tiêu: ${signed(s.sumDeltaKcal)} kcal; so với TDEE: ${signed(s.tdeeBalanceKcal)} kcal " +
                "(≈ ${signedKg(s.expectedWeightChangeKg)} kg dự kiến)",
        )
        if (s.avgProteinG != null) {
            appendLine(
                "Macro TB: đạm ${Math.round(s.avgProteinG)} g, tinh bột ${Math.round(s.avgCarbG ?: 0.0)} g, béo ${Math.round(s.avgFatG ?: 0.0)} g; " +
                    "đạt đạm ${s.proteinMetDays}/${s.elapsedDays} ngày",
            )
        }
        appendLine("Nước: ${liters(s.waterTotalMl)}/${liters(s.waterTargetTotalMl)} L, đạt ${s.waterMetDays}/${s.elapsedDays} ngày")
        if (s.avgWeightKg != null) {
            val change = s.weightChangeKg
            val prev = if (change != null) " (tuần trước ${num1(s.avgWeightKg - change)} → ${signedKg(change)} kg)" else ""
            appendLine("Cân: TB tuần ${num1(s.avgWeightKg)} kg$prev")
        } else {
            appendLine("Cân: tuần này chưa cân")
        }
        if (s.topFoods.isNotEmpty()) {
            appendLine("Top món theo calo: " + s.topFoods.joinToString(", ") { "${it.name} ×${it.count} (${Math.round(it.kcal)} kcal)" })
        }
        appendLine(
            "Theo ngày: " + s.days.filter { it.status != DayStatus.FUTURE && it.status != DayStatus.BEFORE_START }.joinToString(" | ") { d ->
                if (d.status == DayStatus.NOT_LOGGED) "${dow(d.date)} chưa ghi"
                else "${dow(d.date)} ${Math.round(kcalOf(s, d))}" + if (d.suspectedUnderLog) "⚠" else ""
            },
        )
        if (comparison != null) {
            appendLine(
                "So với tuần trước (cùng số ngày): kcal TB ${arrow(comparison.avgKcal)}, ngày đạt ${arrow(comparison.onTargetDays)}, " +
                    "nước TB ${arrow(comparison.avgWaterMl)}, đạm TB ${arrow(comparison.avgProteinG)}, điểm ${arrow(comparison.score)}",
            )
        }
        val foods = dayFoods.filterKeys { it in s.weekStart..end }.toSortedMap()
        if (foods.isNotEmpty()) {
            appendLine("Món theo ngày:")
            foods.forEach { (date, items) ->
                appendLine("  ${dow(date)}: " + items.joinToString(", ") { (n, k) -> "$n ${Math.round(k)}" })
            }
        }
        append("</weekly_stats>")
    }

    private val DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /** kcal của ngày = mục tiêu + chênh lệch (WeeklyStats chỉ giữ chênh lệch). */
    private fun kcalOf(s: WeeklyStats, d: DayResult): Double = targetKcal(s) + (d.delta ?: 0.0)

    private fun targetKcal(s: WeeklyStats): Int =
        if (s.loggedDays > 0) Math.round(s.sumTargetKcal / s.loggedDays).toInt() else 0

    private fun waterTarget(s: WeeklyStats): Int =
        if (s.elapsedDays > 0) Math.round(s.waterTargetTotalMl.toDouble() / s.elapsedDays).toInt() else 0

    private fun goalText(g: GoalType) = when (g) {
        GoalType.LOSE -> "giảm cân"
        GoalType.MAINTAIN -> "giữ cân"
        GoalType.GAIN -> "tăng cân"
    }

    private fun dow(d: LocalDate) = when (d.dayOfWeek) {
        DayOfWeek.MONDAY -> "T2"
        DayOfWeek.TUESDAY -> "T3"
        DayOfWeek.WEDNESDAY -> "T4"
        DayOfWeek.THURSDAY -> "T5"
        DayOfWeek.FRIDAY -> "T6"
        DayOfWeek.SATURDAY -> "T7"
        else -> "CN"
    }

    private fun signed(x: Double): String {
        val r = Math.round(x)
        return if (r >= 0) "+$r" else "−${-r}"
    }

    private fun signedKg(x: Double): String = if (x >= 0) "+" + num2(x) else "−" + num2(-x)

    private fun num1(x: Double) = String.format(Locale.ROOT, "%.1f", x).replace('.', ',')

    private fun num2(x: Double): String {
        val s = String.format(Locale.ROOT, "%.2f", x).trimEnd('0').trimEnd('.')
        return s.replace('.', ',')
    }

    private fun liters(ml: Int) = num1(ml / 1000.0)

    private fun arrow(t: Trend?) = when (t) {
        Trend.UP -> "↑"
        Trend.DOWN -> "↓"
        Trend.SAME -> "="
        null -> "?"
    }
}
