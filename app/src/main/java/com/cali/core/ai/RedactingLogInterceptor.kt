package com.cali.core.ai

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Log HTTP tối giản cho bản debug (06 §5): dòng request/response và header, **che** `x-api-key` /
 * `Authorization`, không bao giờ log body (ảnh, ngữ cảnh người dùng). Chỉ gắn vào client khi `BuildConfig.DEBUG`.
 */
class RedactingLogInterceptor(private val log: (String) -> Unit) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        log("--> ${request.method} ${request.url}")
        logHeaders(request.headers)
        val started = System.nanoTime()
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            log("<-- HTTP FAILED: ${e.javaClass.simpleName}")
            throw e
        }
        val ms = (System.nanoTime() - started) / 1_000_000
        log("<-- ${response.code} ${request.url} (${ms} ms)")
        logHeaders(response.headers)
        return response
    }

    private fun logHeaders(headers: Headers) {
        for (i in 0 until headers.size) {
            val name = headers.name(i)
            log("$name: ${if (name.lowercase() in REDACTED) "██" else headers.value(i)}")
        }
    }

    companion object {
        val REDACTED = setOf("x-api-key", "authorization", "cookie", "set-cookie")
    }
}
