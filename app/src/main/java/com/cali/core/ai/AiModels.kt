package com.cali.core.ai

import com.cali.core.calc.model.MealType
import com.cali.core.calc.model.Sex
import com.cali.core.data.db.entity.AiMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Một lần gọi AI (03 §2). */
data class AiRequest(
    val mode: AiMode,
    /** Prompt gốc (bản mặc định trong assets hoặc bản người dùng sửa). */
    val systemPrompt: String,
    val context: UserContext,
    /** Lịch sử chat trong ngày; PromptBuilder tự cắt còn 10 tin nhắn cuối. */
    val history: List<ChatTurn> = emptyList(),
    val userText: String? = null,
    val imageJpeg: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is AiRequest && mode == other.mode && systemPrompt == other.systemPrompt &&
            context == other.context && history == other.history && userText == other.userText &&
            (imageJpeg?.contentEquals(other.imageJpeg) ?: (other.imageJpeg == null))

    override fun hashCode(): Int =
        listOf(mode, systemPrompt, context, history, userText, imageJpeg?.contentHashCode()).hashCode()
}

sealed interface AiResult {
    /** [callId] = id dòng `ai_call_log` để gắn vào món / tin nhắn (null nếu ghi log lỗi). */
    data class FoodLog(val payload: ValidatedFoodLog, val usage: TokenUsage, val callId: Long? = null) : AiResult
    data class Text(val text: String, val usage: TokenUsage, val callId: Long? = null) : AiResult
    data class Error(val type: AiErrorType, val message: String, val callId: Long? = null) : AiResult
}

/** Loại lỗi theo bảng 03 §8; UI chọn thông báo theo loại, không theo [AiResult.Error.message]. */
enum class AiErrorType {
    NO_KEY,
    NO_NETWORK,
    INVALID_KEY,
    RATE_LIMITED,
    OVERLOADED,
    TIMEOUT,
    INVALID_RESPONSE,
    REFUSED,
    UNKNOWN,
}

data class TokenUsage(
    val inputTokens: Int = 0,
    val outputTokens: Int = 0,
    val cacheReadTokens: Int = 0,
    val cacheCreationTokens: Int = 0,
)

enum class ChatRole { USER, ASSISTANT }

/**
 * Một tin nhắn trong lịch sử chat.
 * [hadImage] = tin có đính ảnh; ảnh cũ không gửi lại mà thay bằng "[ảnh: <tên món>]" từ [loggedItemNames].
 * Tin của AI đã ghi món thì [loggedItemNames] là các món trong thẻ.
 */
data class ChatTurn(
    val role: ChatRole,
    val text: String? = null,
    val hadImage: Boolean = false,
    val loggedItemNames: List<String> = emptyList(),
)

/** Ngữ cảnh gửi kèm mỗi lần gọi (03 §4). Số là giá trị thật, PromptBuilder lo định dạng. */
data class UserContext(
    val now: LocalDateTime,
    val sex: Sex,
    val age: Int,
    val heightCm: Double,
    val weightKg: Double,
    /** null khi giữ cân. */
    val targetWeightKg: Double?,
    val goalKcal: Int,
    val goalProteinG: Int,
    val goalCarbG: Int,
    val goalFatG: Int,
    val eatenKcal: Int,
    val eatenProteinG: Int,
    val eatenCarbG: Int,
    val eatenFatG: Int,
    val todayEntries: List<ContextEntry> = emptyList(),
    /** Giờ dùng cho lần ghi này (giờ chụp / giờ gửi); null = [now]. */
    val logTime: LocalTime? = null,
    val personalNotes: String = "",
    /** Tối đa 20 món Món của tôi có useCount cao nhất, đã định dạng sẵn: "Cơm nhà (1 bát 180 g)". */
    val frequentFoods: List<String> = emptyList(),
)

data class ContextEntry(val meal: MealType, val time: LocalTime, val name: String, val kcal: Int)

/** Kết quả `record_food_items` sau khi qua FoodLogValidator. */
data class ValidatedFoodLog(
    val items: List<ValidatedFoodItem>,
    val mealType: MealType? = null,
    val eatenTime: LocalTime? = null,
    val eatenDate: LocalDate? = null,
    val clarifyingQuestion: String? = null,
    val comment: String? = null,
) {
    /** "Không nhận ra món ăn nào" (03 §5). */
    val isEmpty: Boolean get() = items.isEmpty()
}

data class ValidatedFoodItem(
    val name: String,
    val portionDesc: String,
    val grams: Double,
    val kcal: Double,
    val proteinG: Double,
    val carbG: Double,
    val fatG: Double,
    val confidence: Double,
    val assumptions: String? = null,
    val flags: Set<com.cali.core.calc.AmberFlag> = emptySet(),
)
