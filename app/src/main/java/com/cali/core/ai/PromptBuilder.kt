package com.cali.core.ai

import com.cali.core.calc.MealTypeResolver
import com.cali.core.calc.model.MealType
import com.cali.core.calc.model.Sex
import com.cali.core.data.db.entity.AiMode
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

sealed interface PromptPart {
    data class Text(val text: String) : PromptPart

    class Image(val jpeg: ByteArray) : PromptPart {
        override fun toString() = "Image(${jpeg.size} B)"
    }
}

data class PromptMessage(val role: ChatRole, val parts: List<PromptPart>)

/**
 * Prompt trung lập hãng.
 * [staticSystem] không đổi giữa các lần gọi cùng mode → đặt `cache_control`; [userContext] đổi mỗi lần.
 */
data class BuiltPrompt(val staticSystem: String, val userContext: String, val messages: List<PromptMessage>)

/** Ghép system prompt + `<user_context>` (03 §4) + lịch sử chat (03 §9). Thuần Kotlin. */
class PromptBuilder(private val resolver: MealTypeResolver = MealTypeResolver()) {

    fun build(request: AiRequest): BuiltPrompt {
        val static = when (request.mode) {
            AiMode.PHOTO_LOG -> request.systemPrompt.trimEnd() + "\n\n" + PHOTO_LOG_RULE
            else -> request.systemPrompt.trimEnd()
        }
        return BuiltPrompt(static, userContextBlock(request.context), messages(request))
    }

    fun userContextBlock(ctx: UserContext): String = buildString {
        appendLine("<user_context>")
        appendLine("Ngày: ${ctx.now.toLocalDate()} (${dayName(ctx.now.dayOfWeek)}), giờ hiện tại: ${hhmm(ctx.now.toLocalTime())}")
        appendLine(
            "Hồ sơ: ${if (ctx.sex == Sex.MALE) "Nam" else "Nữ"}, ${ctx.age} tuổi, ${num(ctx.heightCm)} cm, " +
                "${num(ctx.weightKg)} kg, ${goalText(ctx)}",
        )
        appendLine("Mục tiêu ngày: ${ctx.goalKcal} kcal | Đạm ${ctx.goalProteinG} g | Tinh bột ${ctx.goalCarbG} g | Béo ${ctx.goalFatG} g")
        appendLine("Đã ăn hôm nay: ${ctx.eatenKcal} kcal | Đạm ${ctx.eatenProteinG} g | Tinh bột ${ctx.eatenCarbG} g | Béo ${ctx.eatenFatG} g")
        val remaining = ctx.goalKcal - ctx.eatenKcal
        appendLine(if (remaining >= 0) "Còn lại: $remaining kcal" else "Còn lại: 0 kcal (đã vượt ${-remaining} kcal)")
        appendLine("Các bữa đã ghi: ${mealsText(ctx.todayEntries)}")
        val logTime = ctx.logTime ?: ctx.now.toLocalTime()
        appendLine("Thời điểm của lần ghi này: ${hhmm(logTime)} → bữa ${mealName(resolver.fromTime(logTime))} (tự gán theo giờ)")
        ctx.personalNotes.trim().takeIf { it.isNotEmpty() }?.let { appendLine("Ghi chú cá nhân: $it") }
        ctx.frequentFoods.take(MAX_FREQUENT).takeIf { it.isNotEmpty() }?.let { appendLine("Món hay ăn: ${it.joinToString("; ")}") }
        append("</user_context>")
    }

    private fun messages(request: AiRequest): List<PromptMessage> {
        val history = request.history.takeLast(MAX_HISTORY)
            .mapNotNull { turn -> historyText(turn)?.let { PromptMessage(turn.role, listOf(PromptPart.Text(it))) } }
            .dropWhile { it.role == ChatRole.ASSISTANT }

        val current = buildList {
            request.imageJpeg?.let { add(PromptPart.Image(it)) }
            request.userText?.trim()?.takeIf { it.isNotEmpty() }?.let { add(PromptPart.Text(it)) }
        }.ifEmpty { listOf(PromptPart.Text("(không có ghi chú)")) }

        // Gộp các tin liền nhau cùng vai (API yêu cầu xen kẽ user/assistant).
        val merged = mutableListOf<PromptMessage>()
        for (m in history + PromptMessage(ChatRole.USER, current)) {
            val last = merged.lastOrNull()
            if (last != null && last.role == m.role) {
                merged[merged.lastIndex] = last.copy(parts = last.parts + m.parts)
            } else {
                merged += m
            }
        }
        return merged
    }

    /** Ảnh cũ không gửi lại: "[ảnh: <tên các món đã ghi>]". Món AI đã ghi: "[đã ghi: …]". */
    private fun historyText(turn: ChatTurn): String? {
        val lines = buildList {
            if (turn.role == ChatRole.USER && turn.hadImage) {
                add(if (turn.loggedItemNames.isEmpty()) "[ảnh]" else "[ảnh: ${turn.loggedItemNames.joinToString(", ")}]")
            }
            turn.text?.trim()?.takeIf { it.isNotEmpty() }?.let(::add)
            if (turn.role == ChatRole.ASSISTANT && turn.loggedItemNames.isNotEmpty()) {
                add("[đã ghi: ${turn.loggedItemNames.joinToString(", ")}]")
            }
        }
        return lines.takeIf { it.isNotEmpty() }?.joinToString("\n")
    }

    private fun goalText(ctx: UserContext): String {
        val target = ctx.targetWeightKg ?: return "mục tiêu giữ cân"
        return when {
            target < ctx.weightKg -> "mục tiêu giảm còn ${num(target)} kg"
            target > ctx.weightKg -> "mục tiêu tăng lên ${num(target)} kg"
            else -> "mục tiêu giữ cân"
        }
    }

    private fun mealsText(entries: List<ContextEntry>): String {
        if (entries.isEmpty()) return "chưa có"
        val sorted = entries.sortedBy { it.time }
        val order = sorted.map { it.meal }.distinct()
        return order.joinToString("; ") { meal ->
            mealName(meal) + ": " + sorted.filter { it.meal == meal }
                .joinToString(", ") { "${hhmm(it.time)} ${it.name} (${it.kcal} kcal)" }
        }
    }

    companion object {
        const val MAX_HISTORY = 10
        const val MAX_FREQUENT = 20

        /** Thay cho `tool_choice` ép tool: Sonnet 5.5 trả 400 nếu ép (09 §8). */
        const val PHOTO_LOG_RULE =
            "## Chế độ hiện tại: ghi món từ ảnh\n" +
                "Người dùng vừa gửi ảnh món ăn. Bắt buộc trả kết quả bằng cách gọi tool `record_food_items` " +
                "đúng một lần, không trả lời bằng văn bản thay cho tool. Nếu ảnh không có đồ ăn/uống, " +
                "vẫn gọi tool với `items` rỗng và giải thích trong `comment`."

        private val HHMM = DateTimeFormatter.ofPattern("HH:mm")

        private fun hhmm(t: java.time.LocalTime) = t.format(HHMM)

        /** 74.2 → "74.2", 170.0 → "170" (định dạng gửi AI, không phải hiển thị). */
        private fun num(x: Double): String {
            val r = (x * 10).roundToLong()
            return if (r % 10 == 0L) (r / 10).toString() else "${r / 10}.${kotlin.math.abs(r % 10)}"
        }

        fun mealName(m: MealType) = when (m) {
            MealType.BREAKFAST -> "Sáng"
            MealType.LUNCH -> "Trưa"
            MealType.DINNER -> "Tối"
            MealType.SNACK -> "Phụ"
        }

        private fun dayName(d: DayOfWeek) = when (d) {
            DayOfWeek.MONDAY -> "Thứ Hai"
            DayOfWeek.TUESDAY -> "Thứ Ba"
            DayOfWeek.WEDNESDAY -> "Thứ Tư"
            DayOfWeek.THURSDAY -> "Thứ Năm"
            DayOfWeek.FRIDAY -> "Thứ Sáu"
            DayOfWeek.SATURDAY -> "Thứ Bảy"
            DayOfWeek.SUNDAY -> "Chủ Nhật"
        }
    }
}
