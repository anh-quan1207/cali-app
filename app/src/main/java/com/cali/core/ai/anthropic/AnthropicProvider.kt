package com.cali.core.ai.anthropic

import com.cali.core.ai.AiCallRecorder
import com.cali.core.ai.AiErrorType
import com.cali.core.ai.AiException
import com.cali.core.ai.AiModelSource
import com.cali.core.ai.AiProvider
import com.cali.core.ai.AiRequest
import com.cali.core.ai.AiResult
import com.cali.core.ai.ApiKeySource
import com.cali.core.ai.BuiltPrompt
import com.cali.core.ai.ChatRole
import com.cali.core.ai.FoodLogValidator
import com.cali.core.ai.PromptBuilder
import com.cali.core.ai.PromptPart
import com.cali.core.ai.RecordFoodItemsTool
import com.cali.core.ai.TokenUsage
import com.cali.core.data.db.entity.AiCallLogEntity
import com.cali.core.data.db.entity.AiMode
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.time.Clock
import java.time.LocalDate
import java.util.Base64
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 * Gọi Claude Messages API bằng key của người dùng (03, 06 §6).
 *
 * - Mọi mode dùng `tool_choice: auto` + `strict: true`: Sonnet 5.5 trả 400 nếu ép tool. PHOTO_LOG thiếu tool_use
 *   bị coi là phản hồi hỏng và gọi lại 1 lần (09 §8).
 * - 429 / 5xx / 529: retry tối đa 2 lần, chờ 2 s rồi 5 s. 401/403, timeout, mất mạng: báo ngay.
 * - Mỗi request HTTP ghi một dòng `ai_call_log` (kể cả lỗi). Chưa có key thì không gọi mạng, không ghi log.
 * - Không ném exception ra ngoài trừ CancellationException; huỷ coroutine huỷ luôn request OkHttp.
 */
class AnthropicProvider(
    private val client: OkHttpClient,
    private val keySource: ApiKeySource,
    private val models: AiModelSource,
    private val recorder: AiCallRecorder,
    private val config: AnthropicConfig = AnthropicConfig(),
    private val promptBuilder: PromptBuilder = PromptBuilder(),
    private val clock: Clock = Clock.systemDefaultZone(),
    private val sleep: suspend (Long) -> Unit = { kotlinx.coroutines.delay(it) },
) : AiProvider {

    private val baseUrl: HttpUrl = config.baseUrl.toHttpUrl()

    override suspend fun send(request: AiRequest): AiResult {
        val key = currentKey() ?: return AiResult.Error(AiErrorType.NO_KEY, "Chưa có API key")
        if (request.mode == AiMode.WEEKLY_REVIEW) {
            return AiResult.Error(AiErrorType.UNKNOWN, "Đánh giá tuần chưa được hỗ trợ")
        }
        val model = models.modelFor(request.mode)
        val body = json.encodeToString(MessagesRequest.serializer(), buildRequest(model, request.mode, promptBuilder.build(request)))
        val today = LocalDate.now(clock)
        val withFallback = model in config.fallbackModels

        var retryIndex = 0
        var invalidLeft = config.invalidResponseRetries
        while (true) {
            val started = System.nanoTime()
            val raw = execute(messagesRequest(key, body, withFallback))
            val durationMs = (System.nanoTime() - started) / 1_000_000

            when (raw) {
                is Raw.IoFailure -> {
                    val id = record(request.mode, model, durationMs, null, raw.type)
                    return AiResult.Error(raw.type, raw.message, id)
                }
                is Raw.Http -> when {
                    raw.code in 200..299 -> when (val r = interpret(raw.body, request.mode, today)) {
                        is Interpreted.Ok -> {
                            val id = record(request.mode, model, durationMs, r.usage, null)
                            return r.result(id)
                        }
                        is Interpreted.Refused -> {
                            val id = record(request.mode, model, durationMs, r.usage, AiErrorType.REFUSED)
                            return AiResult.Error(AiErrorType.REFUSED, "AI từ chối yêu cầu", id)
                        }
                        is Interpreted.Invalid -> {
                            val id = record(request.mode, model, durationMs, r.usage, AiErrorType.INVALID_RESPONSE)
                            if (invalidLeft-- > 0) continue
                            return AiResult.Error(AiErrorType.INVALID_RESPONSE, r.reason, id)
                        }
                    }
                    else -> {
                        val type = errorTypeFor(raw.code)
                        val id = record(request.mode, model, durationMs, null, type)
                        if (isRetryable(raw.code) && retryIndex < config.retryDelaysMs.size) {
                            sleep(config.retryDelaysMs[retryIndex++])
                            continue
                        }
                        return AiResult.Error(type, errorMessage(raw), id)
                    }
                }
            }
        }
    }

    override suspend fun testConnection(): Result<Unit> {
        val key = currentKey() ?: return Result.failure(AiException(AiErrorType.NO_KEY, "Chưa có API key"))
        val url = baseUrl.newBuilder().addPathSegments("v1/models").addQueryParameter("limit", "1").build()
        val req = Request.Builder().url(url).headers(key).get().build()
        return when (val raw = execute(req)) {
            is Raw.IoFailure -> Result.failure(AiException(raw.type, raw.message))
            is Raw.Http ->
                if (raw.code in 200..299) {
                    Result.success(Unit)
                } else {
                    Result.failure(AiException(errorTypeFor(raw.code), errorMessage(raw)))
                }
        }
    }

    // --- Request ---

    private suspend fun currentKey(): String? = keySource.apiKey()?.trim()?.takeIf { it.isNotEmpty() }

    private fun buildRequest(model: String, mode: AiMode, prompt: BuiltPrompt) = MessagesRequest(
        model = model,
        maxTokens = config.maxTokens,
        system = listOf(
            SystemBlock(prompt.staticSystem, cacheControl = CacheControl()),
            SystemBlock(prompt.userContext),
        ),
        messages = prompt.messages.map { m ->
            MessageParam(
                role = if (m.role == ChatRole.USER) "user" else "assistant",
                content = m.parts.map { p ->
                    when (p) {
                        is PromptPart.Text -> ContentParam.Text(p.text)
                        is PromptPart.Image -> ContentParam.Image(ImageSource(Base64.getEncoder().encodeToString(p.jpeg)))
                    }
                },
            )
        },
        tools = listOf(ToolParam(RecordFoodItemsTool.NAME, RecordFoodItemsTool.DESCRIPTION, RecordFoodItemsTool.inputSchema)),
        toolChoice = ToolChoice("auto"),
        outputConfig = OutputConfig(config.effortFor(mode)),
        fallbacks = if (model in config.fallbackModels) "default" else null,
    )

    private fun messagesRequest(key: String, body: String, withFallback: Boolean): Request =
        Request.Builder()
            .url(baseUrl.newBuilder().addPathSegments("v1/messages").build())
            .headers(key)
            .apply { if (withFallback) header("anthropic-beta", config.fallbackBeta) }
            .post(body.toRequestBody(JSON_MEDIA))
            .build()

    private fun Request.Builder.headers(key: String) = this
        .header("x-api-key", key)
        .header("anthropic-version", config.apiVersion)
        .header("content-type", "application/json")

    // --- Response ---

    private sealed interface Interpreted {
        class Ok(val usage: TokenUsage, val result: (Long?) -> AiResult) : Interpreted
        class Invalid(val usage: TokenUsage?, val reason: String) : Interpreted
        class Refused(val usage: TokenUsage?) : Interpreted
    }

    private fun interpret(body: String, mode: AiMode, today: LocalDate): Interpreted {
        val resp = try {
            json.decodeFromString(MessagesResponse.serializer(), body)
        } catch (e: Exception) {
            return Interpreted.Invalid(null, "Phản hồi không đọc được")
        }
        val usage = resp.usage?.let {
            TokenUsage(it.inputTokens, it.outputTokens, it.cacheReadInputTokens ?: 0, it.cacheCreationInputTokens ?: 0)
        } ?: TokenUsage()

        if (resp.stopReason == "refusal") return Interpreted.Refused(usage)

        val toolUse = resp.content.firstOrNull {
            it.str("type") == "tool_use" && it.str("name") == RecordFoodItemsTool.NAME
        }
        if (toolUse != null) {
            return when (val v = FoodLogValidator.validate(toolUse["input"], today)) {
                is FoodLogValidator.Outcome.Valid -> Interpreted.Ok(usage) { id -> AiResult.FoodLog(v.log, usage, id) }
                is FoodLogValidator.Outcome.Invalid -> Interpreted.Invalid(usage, "Kết quả ghi món sai định dạng: ${v.reason}")
            }
        }
        if (mode == AiMode.PHOTO_LOG) return Interpreted.Invalid(usage, "AI không trả kết quả ghi món")

        val text = resp.content.filter { it.str("type") == "text" }
            .mapNotNull { it.str("text") }
            .joinToString("\n\n")
            .trim()
        if (text.isEmpty()) return Interpreted.Invalid(usage, "AI không trả lời")
        return Interpreted.Ok(usage) { id -> AiResult.Text(text, usage, id) }
    }

    private fun JsonObject.str(key: String): String? = (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

    private fun errorTypeFor(code: Int): AiErrorType = when (code) {
        401, 403 -> AiErrorType.INVALID_KEY
        429 -> AiErrorType.RATE_LIMITED
        in 500..599 -> AiErrorType.OVERLOADED
        else -> AiErrorType.UNKNOWN
    }

    private fun isRetryable(code: Int) = code == 429 || code in 500..599

    private fun errorMessage(raw: Raw.Http): String {
        val apiMessage = runCatching { json.decodeFromString(ErrorResponse.serializer(), raw.body).error?.message }.getOrNull()
        return "HTTP ${raw.code}" + (apiMessage?.let { ": $it" } ?: "")
    }

    private suspend fun record(mode: AiMode, model: String, durationMs: Long, usage: TokenUsage?, error: AiErrorType?): Long? =
        try {
            recorder.record(
                AiCallLogEntity(
                    mode = mode,
                    provider = PROVIDER,
                    model = model,
                    inputTokens = usage?.inputTokens,
                    outputTokens = usage?.outputTokens,
                    cacheReadTokens = usage?.cacheReadTokens,
                    durationMs = durationMs,
                    success = error == null,
                    errorType = error?.name,
                    createdAt = clock.millis(),
                ),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    // --- HTTP ---

    private sealed interface Raw {
        class Http(val code: Int, val body: String) : Raw
        class IoFailure(val type: AiErrorType, val message: String) : Raw
    }

    private suspend fun execute(request: Request): Raw = try {
        client.newCall(request).await()
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Raw.IoFailure(ioErrorType(e), e.javaClass.simpleName)
    }

    private fun ioErrorType(e: IOException): AiErrorType = when {
        e is SocketTimeoutException -> AiErrorType.TIMEOUT
        e is InterruptedIOException && e.message == "timeout" -> AiErrorType.TIMEOUT
        else -> AiErrorType.NO_NETWORK
    }

    private suspend fun Call.await(): Raw.Http = suspendCancellableCoroutine { cont ->
        cont.invokeOnCancellation { cancel() }
        enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val result = response.use { Raw.Http(it.code, it.body.string()) }
                        cont.resume(result)
                    } catch (e: IOException) {
                        cont.resumeWithException(e)
                    }
                }
            },
        )
    }

    companion object {
        const val PROVIDER = "anthropic"
        private val JSON_MEDIA = "application/json".toMediaType()

        internal val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
        }

        /** Client riêng cho AI: kết nối 15 s, đọc 60 s (03 §8). [logger] chỉ truyền ở bản debug. */
        fun defaultClient(config: AnthropicConfig, logger: ((String) -> Unit)?): OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
                .apply { if (logger != null) addInterceptor(RedactingLogInterceptor(logger)) }
                .build()
    }
}
