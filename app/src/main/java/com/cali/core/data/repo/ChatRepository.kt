package com.cali.core.data.repo

import com.cali.core.ai.AiErrorType
import com.cali.core.ai.AiFoodLogJson
import com.cali.core.ai.AiLogService
import com.cali.core.ai.AiOutcome
import com.cali.core.ai.ChatRole as TurnRole
import com.cali.core.ai.ChatTurn
import com.cali.core.ai.ResolvedTiming
import com.cali.core.calc.model.MealType
import com.cali.core.data.db.AppDatabase
import com.cali.core.data.db.entity.ChatMessageEntity
import com.cali.core.data.db.entity.ChatRole
import com.cali.core.data.db.entity.FoodLogStatus
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

sealed interface ChatSendResult {
    /** [movedTo] ≠ null khi AI ghi món cho ngày khác → cả cặp tin đã sang luồng ngày đó. */
    data class Sent(val movedTo: LocalDate?) : ChatSendResult
    data class Failed(val type: AiErrorType, val message: String, val userMessageId: Long) : ChatSendResult
}

/**
 * Chat AI (S20): mỗi ngày một luồng (05 S20). Mọi kết quả AI là tin nhắn; thẻ ghi món lưu ở `food_log_json`
 * với trạng thái PENDING → SAVED / DISCARDED. Tin người dùng không có trả lời = "Chưa gửi" (thử lại được).
 */
@Singleton
class ChatRepository @Inject constructor(
    db: AppDatabase,
    private val ai: AiLogService,
    private val clock: Clock,
) {
    private val dao = db.chatMessageDao()

    fun observe(date: LocalDate): Flow<List<ChatMessageEntity>> = dao.observeByDate(date)

    suspend fun send(date: LocalDate, text: String?, jpeg: ByteArray?, thumbnailPath: String?): ChatSendResult {
        val id = dao.insert(
            ChatMessageEntity(date = date, role = ChatRole.USER, text = text?.trim()?.ifEmpty { null }, thumbnailPath = thumbnailPath, createdAt = clock.millis()),
        )
        return answer(id, jpeg)
    }

    /** Gửi lại tin chưa có trả lời; [jpeg] là ảnh còn giữ trong bộ nhớ (null nếu đã mất). */
    suspend fun retry(userMessageId: Long, jpeg: ByteArray?): ChatSendResult = answer(userMessageId, jpeg)

    private suspend fun answer(userId: Long, jpeg: ByteArray?): ChatSendResult {
        val user = dao.byId(userId) ?: return ChatSendResult.Failed(AiErrorType.UNKNOWN, "Không có tin $userId", userId)
        val history = toTurns(dao.observeByDate(user.date).first().filter { it.id < userId })
        val now = LocalDateTime.now(clock)
        val sentAt = if (user.date == now.toLocalDate()) now else LocalDateTime.of(user.date, now.toLocalTime())

        return when (val r = ai.chat(user.text, jpeg, history, sentAt.withSecond(0).withNano(0))) {
            is AiOutcome.Failed -> ChatSendResult.Failed(r.type, r.message, userId)
            is AiOutcome.Text -> {
                dao.insert(ChatMessageEntity(date = user.date, role = ChatRole.ASSISTANT, text = r.text, aiCallId = r.callId, createdAt = clock.millis()))
                ChatSendResult.Sent(null)
            }
            is AiOutcome.FoodLog -> {
                val target = r.timing.eatenAt.toLocalDate()
                if (target != user.date) dao.update(user.copy(date = target))
                dao.insert(
                    ChatMessageEntity(
                        date = target, role = ChatRole.ASSISTANT, text = r.log.comment,
                        foodLogJson = AiFoodLogJson.encode(r.log, r.timing), foodLogStatus = FoodLogStatus.PENDING,
                        aiCallId = r.callId, createdAt = clock.millis(),
                    ),
                )
                ChatSendResult.Sent(target.takeIf { it != user.date })
            }
        }
    }

    suspend fun byId(id: Long): ChatMessageEntity? = dao.byId(id)

    /** Đã lưu từ S12: ghi lại giờ / bữa người dùng chốt để thẻ hiện "Đã lưu vào Trưa". */
    suspend fun markSaved(id: Long, eatenAt: LocalDateTime, meal: MealType, mealManual: Boolean): Result<Unit> = runCatching {
        val m = requireNotNull(dao.byId(id))
        val d = requireNotNull(AiFoodLogJson.decode(m.foodLogJson))
        dao.update(
            m.copy(
                foodLogJson = AiFoodLogJson.encode(d.log, ResolvedTiming(eatenAt, meal), mealManual),
                foodLogStatus = FoodLogStatus.SAVED,
            ),
        )
    }

    suspend fun discard(id: Long): Result<Unit> = setStatus(id, FoodLogStatus.DISCARDED)
    suspend fun undoDiscard(id: Long): Result<Unit> = setStatus(id, FoodLogStatus.PENDING)

    private suspend fun setStatus(id: Long, s: FoodLogStatus) = runCatching {
        val m = requireNotNull(dao.byId(id))
        dao.update(m.copy(foodLogStatus = s))
    }

    companion object {
        /** Tin cuối là của người dùng → chưa có trả lời (lỗi hoặc app bị tắt giữa chừng). */
        fun unansweredId(messages: List<ChatMessageEntity>): Long? =
            messages.lastOrNull()?.takeIf { it.role == ChatRole.USER }?.id

        /** Lịch sử gửi AI (03 §9): ảnh cũ thành "[ảnh: …]", thẻ món thành "[đã ghi: …]". */
        fun toTurns(messages: List<ChatMessageEntity>): List<ChatTurn> = messages.mapIndexed { i, m ->
            val names = AiFoodLogJson.decode(m.foodLogJson)?.log?.items?.map { it.name }.orEmpty()
            if (m.role == ChatRole.USER) {
                val next = messages.getOrNull(i + 1)?.takeIf { it.role == ChatRole.ASSISTANT }
                val nextNames = AiFoodLogJson.decode(next?.foodLogJson)?.log?.items?.map { it.name }.orEmpty()
                ChatTurn(TurnRole.USER, m.text, hadImage = m.thumbnailPath != null, loggedItemNames = if (m.thumbnailPath != null) nextNames else emptyList())
            } else {
                ChatTurn(TurnRole.ASSISTANT, m.text, loggedItemNames = names)
            }
        }
    }
}
