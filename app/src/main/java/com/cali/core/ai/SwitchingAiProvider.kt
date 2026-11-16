package com.cali.core.ai

/**
 * Chỉ dùng ở bản debug (AiModule kiểm `BuildConfig.DEBUG`): khi có cờ [useFake] thì trả lời bằng [fake]
 * để chụp ảnh so mockup / thử luồng mà không cần key thật. Bản release luôn gọi thẳng [real].
 */
class SwitchingAiProvider(
    private val real: AiProvider,
    private val fake: AiProvider,
    private val useFake: () -> Boolean,
) : AiProvider {
    override suspend fun send(request: AiRequest): AiResult = (if (useFake()) fake else real).send(request)
    override suspend fun testConnection(): Result<Unit> = (if (useFake()) fake else real).testConnection()

    companion object {
        /** Tên file cờ trong `filesDir`, do `SeedActivity --ez fakeAi true|false` tạo / xoá. */
        const val DEBUG_FLAG_FILE = "debug_fake_ai"
    }
}
