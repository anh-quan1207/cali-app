package com.cali.core.ai

import com.cali.core.data.db.dao.AiCallLogDao
import com.cali.core.data.db.entity.AiCallLogEntity
import com.cali.core.data.db.entity.AiMode
import com.cali.core.data.prefs.SecureKeyStore
import com.cali.core.data.prefs.SettingsRepository
import javax.inject.Inject

/** Lấy API key ngay trước khi gọi; không giữ trong biến lâu dài (06 §5). */
fun interface ApiKeySource {
    suspend fun apiKey(): String?
}

/** Model theo mode, đọc từ Cài đặt (đổi được không cần build lại, 03 §2). */
fun interface AiModelSource {
    suspend fun modelFor(mode: AiMode): String
}

/** Ghi `ai_call_log` (F16); trả id dòng mới, null nếu ghi lỗi. */
fun interface AiCallRecorder {
    suspend fun record(log: AiCallLogEntity): Long?
}

class SecureApiKeySource @Inject constructor(private val store: SecureKeyStore) : ApiKeySource {
    override suspend fun apiKey(): String? = store.read().getOrNull()
}

class SettingsModelSource @Inject constructor(private val settings: SettingsRepository) : AiModelSource {
    override suspend fun modelFor(mode: AiMode): String {
        val s = settings.current()
        return if (mode == AiMode.PHOTO_LOG) s.aiModelVision else s.aiModelChat
    }
}

class RoomAiCallRecorder @Inject constructor(private val dao: AiCallLogDao) : AiCallRecorder {
    override suspend fun record(log: AiCallLogEntity): Long? = runCatching { dao.insert(log) }.getOrNull()
}
