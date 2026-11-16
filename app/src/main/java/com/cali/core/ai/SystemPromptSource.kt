package com.cali.core.ai

import android.content.Context
import com.cali.core.data.prefs.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * System prompt (03 §6): bản mặc định trong `assets/prompts/system_prompt_vi.md`, bản người dùng sửa ở
 * `Settings.systemPromptOverride`. [DEFAULT_VERSION] tăng khi bản mặc định đổi, so với
 * `Settings.systemPromptDefaultVersion` để S42 báo "có bản mặc định mới".
 */
/** Prompt đang dùng cho mỗi lần gọi; tách interface để test không cần assets. */
fun interface SystemPromptProvider {
    suspend fun current(): String
}

@Singleton
class SystemPromptSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
) : SystemPromptProvider {
    @Volatile private var cachedDefault: String? = null

    suspend fun defaultPrompt(): String = cachedDefault ?: withContext(Dispatchers.IO) {
        context.assets.open(ASSET_PATH).bufferedReader(Charsets.UTF_8).use { it.readText() }.trim()
    }.also { cachedDefault = it }

    /** Prompt đang dùng: bản sửa nếu có (không rỗng), không thì bản mặc định. */
    override suspend fun current(): String =
        settings.current().systemPromptOverride?.takeIf { it.isNotBlank() } ?: defaultPrompt()

    companion object {
        const val ASSET_PATH = "prompts/system_prompt_vi.md"
        const val DEFAULT_VERSION = 1
    }
}
