package com.cali.core.ai.anthropic

import com.cali.core.data.db.entity.AiMode

/**
 * Tham số gọi Messages API (06 §6, đối chiếu tài liệu API 11/2026).
 * Không gửi `temperature/top_p/top_k` (400 ở version 2026-10-21) và không gửi `thinking`
 * (mặc định adaptive trên Sonnet 5.5 / Haiku 5.5).
 */
data class AnthropicConfig(
    val baseUrl: String = "https://api.anthropic.com/",
    val apiVersion: String = "2026-10-21",
    /** Đủ chỗ cho thinking + tool call; vẫn non-streaming. */
    val maxTokens: Int = 8000,
    val effortPhoto: String = "medium",
    val effortChat: String = "low",
    /** Model được bật fallback phía server khi bị từ chối (Haiku 5.5 không hỗ trợ). */
    val fallbackModels: Set<String> = setOf("claude-sonnet-5-5"),
    val fallbackBeta: String = "server-side-fallback-2026-07-01",
    /** 03 §8: retry tối đa 2 lần cho 429 / 5xx / 529. */
    val retryDelaysMs: List<Long> = listOf(2_000, 5_000),
    /** 03 §5: JSON sai schema → gọi lại 1 lần. */
    val invalidResponseRetries: Int = 1,
    val connectTimeoutSeconds: Long = 15,
    val readTimeoutSeconds: Long = 60,
) {
    fun effortFor(mode: AiMode): String = if (mode == AiMode.PHOTO_LOG) effortPhoto else effortChat
}
