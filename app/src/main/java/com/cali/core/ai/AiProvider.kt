package com.cali.core.ai

/** Cổng gọi AI, không phụ thuộc hãng (03 §1.3). Không ném exception, trừ CancellationException. */
interface AiProvider {
    suspend fun send(request: AiRequest): AiResult

    /** Kiểm tra key/mạng cho S42; lỗi trả về dạng [AiException]. */
    suspend fun testConnection(): Result<Unit>
}

class AiException(val type: AiErrorType, message: String) : Exception(message)
