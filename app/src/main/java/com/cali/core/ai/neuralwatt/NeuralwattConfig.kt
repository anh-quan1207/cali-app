package com.cali.core.ai.neuralwatt

import com.cali.core.data.db.entity.AiMode

/**
 * Tham số gọi Neuralwatt `/v1/chat/completions` (OpenAI-compatible, docs.neuralwatt.com, đối chiếu 16/11/2026).
 *
 * - `reasoning_effort`: kimi-k3.1-pro mặc định "max" (chậm, đắt), deepseek-v4.1-flash mặc định "high".
 *   Hạ xuống "low" cho cả hai chế độ để ghi một bữa < 30 s (01 G1). qwen3.6-35b chỉ có high/none: "low" → high.
 * - Không gửi `temperature`, `stream`, `thinking_token_budget`.
 */
data class NeuralwattConfig(
    val baseUrl: String = "https://api.neuralwatt.com/",
    /** Gồm cả token suy nghĩ; hết thì `finish_reason: length`, `content: null`. */
    val maxTokens: Int = 8000,
    val effortPhoto: String = "low",
    val effortChat: String = "low",
    /** 03 §8: retry tối đa 2 lần cho 429 / 5xx. */
    val retryDelaysMs: List<Long> = listOf(2_000, 5_000),
    /** 03 §5: JSON sai schema / thiếu tool call → gọi lại 1 lần. */
    val invalidResponseRetries: Int = 1,
    val connectTimeoutSeconds: Long = 15,
    val readTimeoutSeconds: Long = 60,
) {
    fun effortFor(mode: AiMode): String = if (mode == AiMode.PHOTO_LOG) effortPhoto else effortChat
}
