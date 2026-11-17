package com.cali.core.ai.neuralwatt

import com.cali.core.ai.AiCallRecorder
import com.cali.core.ai.AiErrorType
import com.cali.core.ai.AiException
import com.cali.core.ai.AiModelSource
import com.cali.core.ai.WeeklyReviewTool
import com.cali.core.ai.WeeklyReviewValidator
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
import com.cali.core.ai.RedactingLogInterceptor
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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
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
 * Gọi Neuralwatt `/v1/chat/completions` (OpenAI-compatible) bằng key của người dùng (03, 06 §6, 09 §8).
 *
 * - PHOTO_LOG ép gọi `record_food_items` (`tool_choice` theo tên); CHAT để `auto`. Thiếu tool call ở PHOTO_LOG,
 *   `arguments` không phải JSON, sai schema, hoặc hết `max_tokens` → coi là phản hồi hỏng, gọi lại 1 lần.
 * - 429 / 5xx: retry tối đa 2 lần, chờ 2 s rồi 5 s (trừ khi body ghi `retryable: false`).
 *   401/403, 402, 4xx khác, timeout, mất mạng: báo ngay.
 * - Mỗi request HTTP ghi một dòng `ai_call_log` (kể cả lỗi), có `duration_ms`. Chưa có key thì không gọi mạng.
 * - Không ném exception ra ngoài trừ CancellationException; huỷ coroutine huỷ luôn request OkHttp.
 */
class NeuralwattProvider(
    private val client: OkHttpClient,
    private val keySource: ApiKeySource,
    private val models: AiModelSource,
    private val recorder: AiCallRecorder,
    private val config: NeuralwattConfig = NeuralwattConfig(),
    private val promptBuilder: PromptBuilder = PromptBuilder(),
    private val clock: Clock = Clock.systemDefaultZone(),
    private val sleep: suspend (Long) -> Unit = { kotlinx.coroutines.delay(it) },
) : AiProvider {

    private val baseUrl: HttpUrl = config.baseUrl.toHttpUrl()

    override suspend fun send(request: AiRequest): AiResult {
        val key = currentKey() ?: return AiResult.Error(AiErrorType.NO_KEY, "Chưa có API key")
        val model = models.modelFor(request.mode)
        val body = json.encodeToString(
            ChatCompletionRequest.serializer(),
            buildRequest(model, request.mode, promptBuilder.build(request)),
        )
        val today = LocalDate.now(clock)

        var retryIndex = 0
        var invalidLeft = config.invalidResponseRetries
        while (true) {
            val started = System.nanoTime()
            val raw = execute(completionRequest(key, body))
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
                        val error = parseError(raw.body)
                        val id = record(request.mode, model, durationMs, null, type)
                        if (isRetryable(raw.code, error) && retryIndex < config.retryDelaysMs.size) {
                            sleep(config.retryDelaysMs[retryIndex++])
                            continue
                        }
                        return AiResult.Error(type, errorMessage(raw.code, error), id)
                    }
                }
            }
        }
    }

    /**
     * `GET /v1/models` không tốn token. Không có key hợp lệ thì API vẫn trả 200 với `scope: "public"`,
     * nên chỉ coi là đạt khi `scope` khác "public".
     */
    override suspend fun testConnection(): Result<Unit> {
        val key = currentKey() ?: return Result.failure(AiException(AiErrorType.NO_KEY, "Chưa có API key"))
        val url = baseUrl.newBuilder().addPathSegments("v1/models").build()
        val req = Request.Builder().url(url).auth(key).get().build()
        return when (val raw = execute(req)) {
            is Raw.IoFailure -> Result.failure(AiException(raw.type, raw.message))
            is Raw.Http -> when {
                raw.code !in 200..299 ->
                    Result.failure(AiException(errorTypeFor(raw.code), errorMessage(raw.code, parseError(raw.body))))
                runCatching { json.decodeFromString(ModelsResponse.serializer(), raw.body).scope }.getOrNull() == "public" ->
                    Result.failure(AiException(AiErrorType.INVALID_KEY, "Key không được nhận (danh mục công khai)"))
                else -> Result.success(Unit)
            }
        }
    }

    // --- Request ---

    private suspend fun currentKey(): String? = keySource.apiKey()?.trim()?.takeIf { it.isNotEmpty() }

    private fun buildRequest(model: String, mode: AiMode, prompt: BuiltPrompt): ChatCompletionRequest {
        // Một system message, phần tĩnh đứng đầu để prefix cache phía server dùng lại được.
        val system = ChatMessage("system", JsonPrimitive(prompt.staticSystem + "\n\n" + prompt.userContext))
        val messages = prompt.messages.map { m ->
            if (m.role == ChatRole.USER) {
                ChatMessage("user", userContent(m.parts))
            } else {
                ChatMessage("assistant", JsonPrimitive(m.parts.filterIsInstance<PromptPart.Text>().joinToString("\n\n") { it.text }))
            }
        }
        // PHOTO_LOG và WEEKLY_REVIEW ép gọi tool theo tên; CHAT để AI tự chọn.
        val forced = when (mode) {
            AiMode.PHOTO_LOG -> RecordFoodItemsTool.NAME
            AiMode.WEEKLY_REVIEW -> WeeklyReviewTool.NAME
            AiMode.CHAT -> null
        }
        val toolChoice: JsonElement = if (forced != null) {
            buildJsonObject {
                put("type", "function")
                putJsonObject("function") { put("name", forced) }
            }
        } else {
            JsonPrimitive("auto")
        }
        val tool = if (mode == AiMode.WEEKLY_REVIEW) {
            ToolParam(FunctionDef(WeeklyReviewTool.NAME, WeeklyReviewTool.DESCRIPTION, WeeklyReviewTool.inputSchema))
        } else {
            ToolParam(FunctionDef(RecordFoodItemsTool.NAME, RecordFoodItemsTool.DESCRIPTION, RecordFoodItemsTool.inputSchema))
        }
        return ChatCompletionRequest(
            model = model,
            maxTokens = config.maxTokens,
            messages = listOf(system) + messages,
            tools = listOf(tool),
            toolChoice = toolChoice,
            reasoningEffort = config.effortFor(mode),
        )
    }

    private fun userContent(parts: List<PromptPart>): JsonArray = buildJsonArray {
        for (p in parts) {
            add(
                when (p) {
                    is PromptPart.Text -> buildJsonObject {
                        put("type", "text")
                        put("text", p.text)
                    }
                    is PromptPart.Image -> buildJsonObject {
                        put("type", "image_url")
                        putJsonObject("image_url") {
                            put("url", "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(p.jpeg))
                        }
                    }
                },
            )
        }
    }

    private fun completionRequest(key: String, body: String): Request =
        Request.Builder()
            .url(baseUrl.newBuilder().addPathSegments("v1/chat/completions").build())
            .auth(key)
            .post(body.toRequestBody(JSON_MEDIA))
            .build()

    private fun Request.Builder.auth(key: String) = this
        .header("Authorization", "Bearer $key")
        .header("content-type", "application/json")

    // --- Response ---

    private sealed interface Interpreted {
        class Ok(val usage: TokenUsage, val result: (Long?) -> AiResult) : Interpreted
        class Invalid(val usage: TokenUsage?, val reason: String) : Interpreted
        class Refused(val usage: TokenUsage?) : Interpreted
    }

    private fun interpret(body: String, mode: AiMode, today: LocalDate): Interpreted {
        val resp = try {
            json.decodeFromString(ChatCompletionResponse.serializer(), body)
        } catch (e: Exception) {
            return Interpreted.Invalid(null, "Phản hồi không đọc được")
        }
        val usage = resp.usage?.let {
            TokenUsage(it.promptTokens, it.completionTokens, it.promptTokensDetails?.cachedTokens ?: 0, 0)
        } ?: TokenUsage()
        val choice = resp.choices.firstOrNull() ?: return Interpreted.Invalid(usage, "Phản hồi không có lựa chọn nào")
        val message = choice.message

        if (choice.finishReason == "content_filter" || !message?.refusal.isNullOrBlank()) return Interpreted.Refused(usage)

        if (mode == AiMode.WEEKLY_REVIEW) {
            val review = message?.toolCalls?.firstOrNull { it.function?.name == WeeklyReviewTool.NAME }
                ?.function?.arguments
                ?.let { runCatching { json.parseToJsonElement(it) }.getOrNull() }
                ?.let(WeeklyReviewValidator::validate)
                ?: return Interpreted.Invalid(usage, "AI không trả nhận xét tuần đúng định dạng")
            return Interpreted.Ok(usage) { id -> AiResult.Review(review, usage, id) }
        }

        val call = message?.toolCalls?.firstOrNull { it.function?.name == RecordFoodItemsTool.NAME }
        if (call != null) {
            val args = call.function?.arguments
                ?.let { runCatching { json.parseToJsonElement(it) }.getOrNull() }
                ?: return Interpreted.Invalid(usage, "Kết quả ghi món không phải JSON")
            return when (val v = FoodLogValidator.validate(args, today)) {
                is FoodLogValidator.Outcome.Valid -> Interpreted.Ok(usage) { id -> AiResult.FoodLog(v.log, usage, id) }
                is FoodLogValidator.Outcome.Invalid -> Interpreted.Invalid(usage, "Kết quả ghi món sai định dạng: ${v.reason}")
            }
        }
        if (choice.finishReason == "length") return Interpreted.Invalid(usage, "AI hết lượt token trước khi trả lời")
        if (mode == AiMode.PHOTO_LOG) return Interpreted.Invalid(usage, "AI không trả kết quả ghi món")

        val text = message?.content?.trim().orEmpty()
        if (text.isEmpty()) return Interpreted.Invalid(usage, "AI không trả lời")
        return Interpreted.Ok(usage) { id -> AiResult.Text(text, usage, id) }
    }

    private fun errorTypeFor(code: Int): AiErrorType = when (code) {
        401, 403 -> AiErrorType.INVALID_KEY
        402 -> AiErrorType.QUOTA
        429 -> AiErrorType.RATE_LIMITED
        in 500..599 -> AiErrorType.OVERLOADED
        else -> AiErrorType.UNKNOWN
    }

    private fun isRetryable(code: Int, error: ErrorBody?) =
        (code == 429 || code in 500..599) && error?.retryable != false

    private fun parseError(body: String): ErrorBody? =
        runCatching { json.decodeFromString(ErrorResponse.serializer(), body).error }.getOrNull()

    /** Thông điệp kỹ thuật cho log/debug; UI chọn câu chữ theo [AiErrorType]. */
    private fun errorMessage(code: Int, error: ErrorBody?): String =
        "HTTP $code" + listOfNotNull(error?.code, error?.message).joinToString(": ").let { if (it.isEmpty()) "" else " $it" }

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
        const val PROVIDER = "neuralwatt"
        private val JSON_MEDIA = "application/json".toMediaType()

        internal val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
        }

        /** Client riêng cho AI: kết nối 15 s, đọc 60 s (03 §8). [logger] chỉ truyền ở bản debug. */
        fun defaultClient(config: NeuralwattConfig, logger: ((String) -> Unit)?): OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
                .apply { if (logger != null) addInterceptor(RedactingLogInterceptor(logger)) }
                .build()
    }
}
