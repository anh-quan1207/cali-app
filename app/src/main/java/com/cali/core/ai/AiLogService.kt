package com.cali.core.ai

import com.cali.core.calc.MealTypeResolver
import com.cali.core.data.db.entity.AiMode
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/** Kết quả một lần gọi AI cho UI; không bao giờ ném exception (trừ huỷ). */
sealed interface AiOutcome {
    data class FoodLog(val log: ValidatedFoodLog, val timing: ResolvedTiming, val callId: Long?) : AiOutcome
    data class Text(val text: String, val callId: Long?) : AiOutcome
    data class Failed(val type: AiErrorType, val message: String) : AiOutcome
}

/**
 * Điểm vào duy nhất của UI tới AI (03 §3): gom ngữ cảnh, prompt đang dùng, gọi provider, tính giờ / bữa mặc định
 * cho kết quả ghi món (05 §4). Mọi kết quả vẫn phải qua bước người dùng xác nhận trước khi lưu.
 */
class AiLogService @Inject constructor(
    private val provider: AiProvider,
    private val contexts: UserContextBuilder,
    private val prompts: SystemPromptProvider,
    private val clock: Clock,
) {
    /** Khung giờ bữa của người dùng (S44); ViewModel cập nhật từ Settings. */
    var resolver: MealTypeResolver = MealTypeResolver()

    /** PHOTO_LOG. [defaultAt] = lúc bấm chụp hoặc giờ EXIF của ảnh thư viện (05 §4.1). */
    suspend fun analyzePhoto(jpeg: ByteArray, note: String?, defaultAt: LocalDateTime): AiOutcome =
        call(AiMode.PHOTO_LOG, defaultAt, note, jpeg, emptyList())

    /**
     * Trả lời câu hỏi làm rõ trên Thẻ xác nhận (03 §3.4): một lượt CHAT có kèm kết quả trước đó.
     * AI trả lời chữ (không gọi tool) thì coi như không đổi gì: UI giữ thẻ cũ và hiện câu trả lời.
     */
    suspend fun clarify(previous: ValidatedFoodLog, question: String, answer: String, hadImage: Boolean, defaultAt: LocalDateTime): AiOutcome {
        val names = previous.items.map { it.name }
        val history = listOf(
            ChatTurn(ChatRole.USER, text = null, hadImage = hadImage, loggedItemNames = names),
            ChatTurn(ChatRole.ASSISTANT, text = question, loggedItemNames = names),
        )
        return call(AiMode.CHAT, defaultAt, answer, null, history)
    }

    /** CHAT với lịch sử trong ngày (PromptBuilder tự cắt 10 tin cuối). */
    suspend fun chat(text: String?, jpeg: ByteArray?, history: List<ChatTurn>, sentAt: LocalDateTime): AiOutcome =
        call(AiMode.CHAT, sentAt, text, jpeg, history)

    private suspend fun call(mode: AiMode, at: LocalDateTime, text: String?, jpeg: ByteArray?, history: List<ChatTurn>): AiOutcome {
        return try {
            val ctx = contexts.build(logTime = at.toLocalTime()).getOrElse {
                return AiOutcome.Failed(AiErrorType.UNKNOWN, it.message ?: "Thiếu hồ sơ")
            }
            val request = AiRequest(mode, prompts.current(), ctx, history, text?.trim()?.ifEmpty { null }, jpeg)
            when (val r = provider.send(request)) {
                is AiResult.FoodLog -> AiOutcome.FoodLog(
                    r.payload,
                    FoodLogTiming(resolver).resolve(r.payload, LocalDateTime.now(clock), defaultAt = at),
                    r.callId,
                )
                is AiResult.Text -> AiOutcome.Text(r.text, r.callId)
                is AiResult.Review -> AiOutcome.Failed(AiErrorType.INVALID_RESPONSE, "Kết quả không phải ghi món")
                is AiResult.Error -> AiOutcome.Failed(r.type, r.message)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AiOutcome.Failed(AiErrorType.UNKNOWN, e.javaClass.simpleName)
        }
    }
}
