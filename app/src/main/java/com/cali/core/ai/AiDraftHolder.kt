package com.cali.core.ai

import com.cali.core.data.db.entity.FoodSource
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** Kết quả AI đang chờ người dùng xác nhận trên S12 (chỉ trong bộ nhớ, không ghi đĩa). */
class AiDraft(
    val log: ValidatedFoodLog,
    val timing: ResolvedTiming,
    val mealManual: Boolean,
    val source: FoodSource,
    val callId: Long?,
    /** Thumbnail 256 px để hiện trên thẻ; chỉ ghi đĩa khi lưu và công tắc "Lưu ảnh thu nhỏ" bật. */
    val thumbnailJpeg: ByteArray?,
    /** Ảnh đã gửi AI (để trả lời câu hỏi làm rõ không cần gửi lại ảnh, chỉ cần biết là có ảnh). */
    val hadImage: Boolean,
    /** Giờ mặc định của lần ghi (lúc chụp / giờ EXIF / lúc gửi), dùng lại khi trả lời câu hỏi làm rõ. */
    val defaultAt: LocalDateTime,
    /** Mở từ thẻ chat → sau khi lưu đánh dấu tin này SAVED. */
    val chatMessageId: Long? = null,
)

/** Chuyền kết quả từ S11 sang S12 qua điều hướng (09 §8 G4-19). */
@Singleton
class AiDraftHolder @Inject constructor() {
    @Volatile private var draft: AiDraft? = null

    fun put(d: AiDraft) {
        draft = d
    }

    /** Lấy ra một lần. */
    fun take(): AiDraft? = draft.also { draft = null }
}
