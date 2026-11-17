package com.cali.core.ai.neuralwatt

import com.cali.core.ai.AiCallRecorder
import com.cali.core.ai.AiErrorType
import com.cali.core.ai.AiException
import com.cali.core.ai.AiModelSource
import com.cali.core.ai.AiRequest
import com.cali.core.ai.AiResult
import com.cali.core.ai.ApiKeySource
import com.cali.core.ai.ChatRole
import com.cali.core.ai.ChatTurn
import com.cali.core.ai.RedactingLogInterceptor
import com.cali.core.ai.UserContext
import com.cali.core.calc.model.Sex
import com.cali.core.data.db.entity.AiCallLogEntity
import com.cali.core.data.db.entity.AiMode
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Base64
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/** Neuralwatt, OpenAI-compatible `/v1/chat/completions` (tài liệu docs.neuralwatt.com, 16/11/2026). */
class NeuralwattProviderTest {

    private val key = "sk-nw-test-0123456789-SECRET"
    private lateinit var server: MockWebServer
    private val logs = mutableListOf<AiCallLogEntity>()
    private val sleeps = mutableListOf<Long>()
    private val httpLog = mutableListOf<String>()
    private var apiKey: String? = key
    private val clock = Clock.fixed(Instant.parse("2026-11-13T05:35:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"))

    private val ctx = UserContext(
        now = LocalDateTime.of(2026, 11, 13, 12, 35), sex = Sex.MALE, age = 30, heightCm = 170.0,
        weightKg = 74.1, targetWeightKg = 68.0, goalKcal = 1740, goalProteinG = 122, goalCarbG = 196,
        goalFatG = 52, eatenKcal = 820, eatenProteinG = 45, eatenCarbG = 98, eatenFatG = 30,
    )
    private val jpeg = byteArrayOf(-1, -40, -1, 1, 2, 3)
    private val photo = AiRequest(AiMode.PHOTO_LOG, "PROMPT", ctx, userText = "ăn một nửa", imageJpeg = jpeg)
    private val chat = AiRequest(AiMode.CHAT, "PROMPT", ctx, userText = "tối qua ăn thêm 1 gói mì tôm")

    @Before fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After fun tearDown() {
        server.close()
    }

    private fun provider(
        client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(RedactingLogInterceptor { httpLog += it })
            .build(),
        baseUrl: String = server.url("/").toString(),
    ) = NeuralwattProvider(
        client = client,
        keySource = ApiKeySource { apiKey },
        models = AiModelSource { mode -> if (mode == AiMode.PHOTO_LOG) "kimi-k3.1-pro" else "deepseek-v4.1-flash" },
        recorder = AiCallRecorder { logs += it; logs.size.toLong() },
        config = NeuralwattConfig(baseUrl = baseUrl),
        clock = clock,
        sleep = { sleeps += it },
    )

    private fun ok(body: String) = MockResponse.Builder().code(200)
        .addHeader("content-type", "application/json").body(body).build()

    private fun err(code: Int, type: String = "error", errCode: String = "x", msg: String = "lỗi", extra: String = "") =
        MockResponse.Builder().code(code)
            .addHeader("content-type", "application/json")
            .body("""{"error":{"message":"$msg","type":"$type","code":"$errCode"$extra}}""").build()

    private val goodInput = """{"items":[{"name":"Phở bò","portion_desc":"1 tô vừa","grams":500,"kcal":452,
        "protein_g":25,"carb_g":60,"fat_g":12,"confidence":0.9}],"meal_type":"LUNCH"}"""

    /** `arguments` là chuỗi JSON (OpenAI). */
    private fun toolResponse(input: String = goodInput, model: String = "kimi-k3.1-pro") = ok(
        """{"id":"chatcmpl-1","object":"chat.completion","model":"$model",
        "choices":[{"index":0,"finish_reason":"tool_calls","message":{"role":"assistant","content":"Mình ghi nhé.",
          "reasoning":"nghĩ một chút",
          "tool_calls":[{"id":"call_1","type":"function","function":{"name":"record_food_items",
            "arguments":${JsonPrimitive(input.replace("\n", ""))}}}]}}],
        "usage":{"prompt_tokens":1200,"completion_tokens":300,"total_tokens":1500,
          "prompt_tokens_details":{"cached_tokens":1000},"completion_tokens_details":{"reasoning_tokens":120}},
        "energy":{"energy_joules":12.5,"measurement_available":true}}""",
    )

    private fun textResponse(text: String = "Bạn còn 920 kcal.", finish: String = "stop") = ok(
        """{"id":"chatcmpl-2","object":"chat.completion","model":"deepseek-v4.1-flash",
        "choices":[{"index":0,"finish_reason":"$finish","message":{"role":"assistant","content":"$text"}}],
        "usage":{"prompt_tokens":900,"completion_tokens":40,"total_tokens":940}}""",
    )

    private fun RecordedRequest.json(): JsonObject = Json.parseToJsonElement(body!!.utf8()).jsonObject

    // --- Request ---

    @Test fun `PHOTO_LOG gửi đúng header và body`() = runBlocking {
        server.enqueue(toolResponse())
        provider().send(photo)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/v1/chat/completions", req.url.encodedPath)
        assertEquals("Bearer $key", req.headers["Authorization"])
        assertNull(req.headers["x-api-key"])
        assertNull(req.headers["anthropic-version"])
        assertTrue(req.headers["content-type"]!!.startsWith("application/json"))

        val b = req.json()
        assertEquals("kimi-k3.1-pro", b["model"]!!.jsonPrimitive.content)
        assertEquals(8000, b["max_tokens"]!!.jsonPrimitive.int)
        // Kimi mặc định suy nghĩ "max": hạ xuống để ghi một bữa < 30 s (01 G1).
        assertEquals("low", b["reasoning_effort"]!!.jsonPrimitive.content)
        // Ép gọi đúng tool ở chế độ ảnh.
        val choice = b["tool_choice"]!!.jsonObject
        assertEquals("function", choice["type"]!!.jsonPrimitive.content)
        assertEquals("record_food_items", choice["function"]!!.jsonObject["name"]!!.jsonPrimitive.content)
        val tool = b["tools"]!!.jsonArray.single().jsonObject
        assertEquals("function", tool["type"]!!.jsonPrimitive.content)
        val fn = tool["function"]!!.jsonObject
        assertEquals("record_food_items", fn["name"]!!.jsonPrimitive.content)
        assertEquals("object", fn["parameters"]!!.jsonObject["type"]!!.jsonPrimitive.content)

        val messages = b["messages"]!!.jsonArray
        assertEquals(2, messages.size)
        // Một system message: phần tĩnh đứng đầu (prefix cache tự động), rồi <user_context>.
        val system = messages[0].jsonObject
        assertEquals("system", system["role"]!!.jsonPrimitive.content)
        val sysText = system["content"]!!.jsonPrimitive.content
        assertTrue(sysText.startsWith("PROMPT"))
        assertTrue("<user_context>" in sysText)
        assertTrue(sysText.indexOf("PROMPT") < sysText.indexOf("<user_context>"))

        val user = messages[1].jsonObject
        assertEquals("user", user["role"]!!.jsonPrimitive.content)
        val content = user["content"]!!.jsonArray
        val image = content[0].jsonObject
        assertEquals("image_url", image["type"]!!.jsonPrimitive.content)
        assertEquals(
            "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(jpeg),
            image["image_url"]!!.jsonObject["url"]!!.jsonPrimitive.content,
        )
        assertEquals("text", content[1].jsonObject["type"]!!.jsonPrimitive.content)
        assertEquals("ăn một nửa", content[1].jsonObject["text"]!!.jsonPrimitive.content)

        for (k in listOf("stream", "cache_control", "output_config", "fallbacks", "system", "temperature")) assertFalse(k, k in b)
    }

    @Test fun `CHAT dùng model chat, tool_choice auto, suy nghĩ thấp`() = runBlocking {
        server.enqueue(textResponse())
        provider().send(chat)
        val b = server.takeRequest().json()
        assertEquals("deepseek-v4.1-flash", b["model"]!!.jsonPrimitive.content)
        assertEquals("auto", b["tool_choice"]!!.jsonPrimitive.content)
        assertEquals("low", b["reasoning_effort"]!!.jsonPrimitive.content)
    }

    @Test fun `lịch sử chat - assistant gửi chữ thường, không có ảnh`() = runBlocking {
        server.enqueue(textResponse())
        val history = listOf(
            ChatTurn(ChatRole.USER, "trưa ăn phở"),
            ChatTurn(ChatRole.ASSISTANT, "Đã ghi phở bò 450 kcal."),
        )
        provider().send(chat.copy(history = history))
        val messages = server.takeRequest().json()["messages"]!!.jsonArray
        assertEquals(listOf("system", "user", "assistant", "user"), messages.map { it.jsonObject["role"]!!.jsonPrimitive.content })
        assertEquals("Đã ghi phở bò 450 kcal.", messages[2].jsonObject["content"]!!.jsonPrimitive.content)
    }

    // --- Response ---

    @Test fun `đọc tool_calls arguments dạng chuỗi, ghi AiCallLog`() = runBlocking {
        server.enqueue(toolResponse())
        val r = provider().send(photo) as AiResult.FoodLog
        val item = r.payload.items.single()
        assertEquals("Phở bò", item.name)
        assertEquals(450.0, item.kcal, 0.0) // đã qua validator: làm tròn bội 5
        assertEquals(1200, r.usage.inputTokens)
        assertEquals(300, r.usage.outputTokens)
        assertEquals(1000, r.usage.cacheReadTokens)
        assertEquals(0, r.usage.cacheCreationTokens)
        assertEquals(1L, r.callId)

        val log = logs.single()
        assertEquals(AiMode.PHOTO_LOG, log.mode)
        assertEquals("neuralwatt", log.provider)
        assertEquals("kimi-k3.1-pro", log.model)
        assertEquals(1200, log.inputTokens)
        assertEquals(300, log.outputTokens)
        assertEquals(1000, log.cacheReadTokens)
        assertTrue(log.durationMs >= 0)
        assertTrue(log.success)
        assertNull(log.errorType)
        assertEquals(clock.millis(), log.createdAt)
    }

    @Test fun `CHAT trả lời chữ`() = runBlocking {
        server.enqueue(textResponse("Bạn còn 920 kcal."))
        val r = provider().send(chat) as AiResult.Text
        assertEquals("Bạn còn 920 kcal.", r.text)
        assertEquals(900, r.usage.inputTokens)
        assertEquals(1, logs.size)
    }

    @Test fun `CHAT có tool_calls thì ra thẻ món`() = runBlocking {
        server.enqueue(toolResponse(model = "deepseek-v4.1-flash"))
        assertTrue(provider().send(chat) is AiResult.FoodLog)
    }

    @Test fun `PHOTO_LOG không gọi tool thì gọi lại một lần`() = runBlocking {
        server.enqueue(textResponse("Đây là phở."))
        server.enqueue(toolResponse())
        val r = provider().send(photo)
        assertTrue(r is AiResult.FoodLog)
        assertEquals(2, server.requestCount)
        assertEquals(listOf(false, true), logs.map { it.success })
        assertEquals("INVALID_RESPONSE", logs[0].errorType)
    }

    @Test fun `hết max_tokens khi đang suy nghĩ thì coi là hỏng, gọi lại một lần`() = runBlocking {
        server.enqueue(
            ok(
                """{"choices":[{"index":0,"finish_reason":"length","message":{"role":"assistant","content":null,"reasoning":"..."}}],
                "usage":{"prompt_tokens":10,"completion_tokens":8000}}""",
            ),
        )
        server.enqueue(textResponse())
        assertTrue(provider().send(chat) is AiResult.Text)
        assertEquals(2, server.requestCount)
    }

    @Test fun `arguments không phải JSON hai lần thì báo INVALID_RESPONSE`() = runBlocking {
        val broken = ok(
            """{"choices":[{"index":0,"finish_reason":"tool_calls","message":{"role":"assistant","content":null,
            "tool_calls":[{"id":"c","type":"function","function":{"name":"record_food_items","arguments":"{\"items\":[{"}}]}}]}""",
        )
        server.enqueue(broken)
        server.enqueue(broken)
        val r = provider().send(photo) as AiResult.Error
        assertEquals(AiErrorType.INVALID_RESPONSE, r.type)
        assertEquals(2, server.requestCount)
        assertEquals(2, logs.size)
    }

    @Test fun `JSON sai schema hai lần thì báo INVALID_RESPONSE`() = runBlocking {
        val bad = """{"items":[{"name":"Phở"}]}"""
        server.enqueue(toolResponse(bad))
        server.enqueue(toolResponse(bad))
        val r = provider().send(photo) as AiResult.Error
        assertEquals(AiErrorType.INVALID_RESPONSE, r.type)
        assertEquals(2, server.requestCount)
    }

    @Test fun `body không phải JSON thì INVALID_RESPONSE`() = runBlocking {
        server.enqueue(ok("<html>oops</html>"))
        server.enqueue(ok("<html>oops</html>"))
        val r = provider().send(chat) as AiResult.Error
        assertEquals(AiErrorType.INVALID_RESPONSE, r.type)
    }

    @Test fun `content_filter hoặc refusal thì báo REFUSED, không gọi lại`() = runBlocking {
        server.enqueue(
            ok(
                """{"choices":[{"index":0,"finish_reason":"content_filter","message":{"role":"assistant","content":null,
                "refusal":"Không thể giúp."}}],"usage":{"prompt_tokens":10,"completion_tokens":0}}""",
            ),
        )
        val r = provider().send(photo) as AiResult.Error
        assertEquals(AiErrorType.REFUSED, r.type)
        assertEquals(1, server.requestCount)
    }

    // --- Lỗi HTTP ---

    @Test fun `401 là key sai, không retry`() = runBlocking {
        server.enqueue(err(401, "authentication_error", "invalid_api_key"))
        val r = provider().send(chat) as AiResult.Error
        assertEquals(AiErrorType.INVALID_KEY, r.type)
        assertEquals(1, server.requestCount)
        assertTrue(sleeps.isEmpty())
        assertFalse(logs.single().success)
        assertEquals("INVALID_KEY", logs.single().errorType)
    }

    @Test fun `403 cũng là key sai`() = runBlocking {
        server.enqueue(err(403, "permission_error", "forbidden"))
        assertEquals(AiErrorType.INVALID_KEY, (provider().send(chat) as AiResult.Error).type)
    }

    @Test fun `402 hết tiền là QUOTA, kèm thông điệp, không retry`() = runBlocking {
        server.enqueue(err(402, "payment_required", "credit_balance_exhausted", "Hết số dư"))
        val r = provider().send(chat) as AiResult.Error
        assertEquals(AiErrorType.QUOTA, r.type)
        assertTrue(r.message, "credit_balance_exhausted" in r.message)
        assertEquals(1, server.requestCount)
    }

    @Test fun `429 retry 2 lần với backoff 2s rồi 5s`() = runBlocking {
        server.enqueue(err(429, "rate_limit_error", "rate_limit_exceeded"))
        server.enqueue(err(429, "rate_limit_error", "rate_limit_exceeded"))
        server.enqueue(textResponse())
        val r = provider().send(chat)
        assertTrue(r is AiResult.Text)
        assertEquals(3, server.requestCount)
        assertEquals(listOf(2000L, 5000L), sleeps)
        assertEquals(listOf(false, false, true), logs.map { it.success })
    }

    @Test fun `429 ba lần thì dừng sau 2 lần retry`() = runBlocking {
        repeat(4) { server.enqueue(err(429, "rate_limit_error", "rate_limit_exceeded")) }
        val r = provider().send(chat) as AiResult.Error
        assertEquals(AiErrorType.RATE_LIMITED, r.type)
        assertEquals(3, server.requestCount)
        assertEquals(listOf(2000L, 5000L), sleeps)
    }

    @Test fun `429 đánh dấu retryable false thì không retry`() = runBlocking {
        server.enqueue(err(429, "rate_limit_error", "tpm_uncached_request_too_large", extra = ""","retryable":false"""))
        val r = provider().send(chat) as AiResult.Error
        assertEquals(AiErrorType.RATE_LIMITED, r.type)
        assertEquals(1, server.requestCount)
        assertTrue(sleeps.isEmpty())
    }

    @Test fun `503 quá tải ba lần`() = runBlocking {
        repeat(3) { server.enqueue(err(503, "service_unavailable", "model_overloaded")) }
        assertEquals(AiErrorType.OVERLOADED, (provider().send(chat) as AiResult.Error).type)
        assertEquals(3, server.requestCount)
    }

    @Test fun `502 rồi thành công`() = runBlocking {
        server.enqueue(err(502, "bad_gateway", "bad_gateway"))
        server.enqueue(textResponse())
        assertTrue(provider().send(chat) is AiResult.Text)
        assertEquals(listOf(2000L), sleeps)
    }

    @Test fun `400 khác báo UNKNOWN kèm thông điệp`() = runBlocking {
        server.enqueue(err(400, "invalid_request_error", "invalid_request", "Model 'x' not found"))
        val r = provider().send(chat) as AiResult.Error
        assertEquals(AiErrorType.UNKNOWN, r.type)
        assertTrue(r.message, "not found" in r.message)
        assertEquals(1, server.requestCount)
    }

    @Test fun `quá thời gian đọc thì báo TIMEOUT`() = runBlocking {
        server.enqueue(textResponse().newBuilder().headersDelay(3, TimeUnit.SECONDS).build())
        val client = OkHttpClient.Builder().readTimeout(300, TimeUnit.MILLISECONDS).build()
        val r = provider(client = client).send(chat) as AiResult.Error
        assertEquals(AiErrorType.TIMEOUT, r.type)
        assertEquals("TIMEOUT", logs.single().errorType)
    }

    @Test fun `timeout mặc định 15s kết nối, 60s đọc`() {
        val c = NeuralwattProvider.defaultClient(NeuralwattConfig(), logger = null)
        assertEquals(15_000, c.connectTimeoutMillis)
        assertEquals(60_000, c.readTimeoutMillis)
    }

    @Test fun `không kết nối được thì báo NO_NETWORK`() = runBlocking {
        val dead = server.url("/").toString()
        server.close()
        val r = provider(baseUrl = dead).send(chat) as AiResult.Error
        assertEquals(AiErrorType.NO_NETWORK, r.type)
    }

    @Test fun `chưa có key thì không gọi mạng`() = runBlocking {
        apiKey = null
        val r = provider().send(photo) as AiResult.Error
        assertEquals(AiErrorType.NO_KEY, r.type)
        assertEquals(0, server.requestCount)
        assertTrue(logs.isEmpty())
    }

    @Test fun `key rỗng coi như chưa có`() = runBlocking {
        apiKey = "  "
        assertEquals(AiErrorType.NO_KEY, (provider().send(photo) as AiResult.Error).type)
        assertEquals(0, server.requestCount)
    }

    @Test fun `huỷ coroutine thì huỷ request`() = runBlocking {
        server.enqueue(textResponse().newBuilder().headersDelay(10, TimeUnit.SECONDS).build())
        val job = async { provider().send(chat) }
        delay(300)
        val started = System.nanoTime()
        job.cancel()
        try {
            job.await()
            fail("phải bị huỷ")
        } catch (_: CancellationException) {
        }
        assertTrue(System.nanoTime() - started < TimeUnit.SECONDS.toNanos(3))
    }

    // --- Log ---

    @Test fun `log HTTP che Authorization`() = runBlocking {
        server.enqueue(toolResponse())
        provider().send(photo)
        assertTrue(httpLog.isNotEmpty())
        assertTrue(httpLog.none { key in it })
        assertTrue(httpLog.joinToString("\n"), httpLog.any { it == "Authorization: ██" })
        assertTrue(httpLog.any { it.contains("/v1/chat/completions") })
        // Không log body (ảnh, ngữ cảnh người dùng).
        assertTrue(httpLog.none { "user_context" in it || "Phở" in it })
    }

    @Test fun `interceptor che cả x-api-key`() {
        val lines = mutableListOf<String>()
        server.enqueue(ok("{}"))
        val client = OkHttpClient.Builder().addInterceptor(RedactingLogInterceptor { lines += it }).build()
        client.newCall(
            okhttp3.Request.Builder().url(server.url("/x")).header("x-api-key", "TOKEN123").build(),
        ).execute().close()
        assertTrue(lines.none { "TOKEN123" in it })
        assertTrue(lines.any { it == "x-api-key: ██" })
    }

    // --- testConnection ---

    @Test fun `kiểm tra kết nối dùng GET models, scope customer là đạt`() = runBlocking {
        server.enqueue(ok("""{"object":"list","scope":"customer","data":[]}"""))
        assertTrue(provider().testConnection().isSuccess)
        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/v1/models", req.url.encodedPath)
        assertEquals("Bearer $key", req.headers["Authorization"])
    }

    @Test fun `models trả scope public nghĩa là key không được nhận`() = runBlocking {
        server.enqueue(ok("""{"object":"list","scope":"public","data":[]}"""))
        val e = provider().testConnection().exceptionOrNull() as AiException
        assertEquals(AiErrorType.INVALID_KEY, e.type)
    }

    @Test fun `kiểm tra kết nối với key sai`() = runBlocking {
        server.enqueue(err(401, "authentication_error", "invalid_api_key"))
        val e = provider().testConnection().exceptionOrNull() as AiException
        assertEquals(AiErrorType.INVALID_KEY, e.type)
    }

    @Test fun `kiểm tra kết nối khi chưa có key`() = runBlocking {
        apiKey = null
        val e = provider().testConnection().exceptionOrNull() as AiException
        assertEquals(AiErrorType.NO_KEY, e.type)
        assertEquals(0, server.requestCount)
    }

    @Test fun `kiểm tra kết nối khi 429`() = runBlocking {
        server.enqueue(err(429, "rate_limit_error", "rate_limit_exceeded"))
        val e = provider().testConnection().exceptionOrNull() as AiException
        assertEquals(AiErrorType.RATE_LIMITED, e.type)
    }

    // ---------- GĐ6: WEEKLY_REVIEW (08 §5) ----------

    private val weekly = AiRequest(AiMode.WEEKLY_REVIEW, "PROMPT", ctx, userText = "<weekly_stats>\nTuần: 09/11/2026\n</weekly_stats>")

    private val reviewInput = """{"headline":"Ngày thường đều tay","highlights":["4/7 ngày đạt"],"issues":["T7 vượt 620"],
        "patterns":[],"suggestions":["a","b","c"],"tone":"ENCOURAGE"}"""

    private fun reviewResponse(input: String = reviewInput) = ok(
        """{"id":"chatcmpl-3","object":"chat.completion","model":"deepseek-v4.1-flash",
        "choices":[{"index":0,"finish_reason":"tool_calls","message":{"role":"assistant","content":null,
          "tool_calls":[{"id":"call_9","type":"function","function":{"name":"submit_weekly_review",
            "arguments":${JsonPrimitive(input.replace("\n", ""))}}}]}}],
        "usage":{"prompt_tokens":2000,"completion_tokens":250,"total_tokens":2250}}""",
    )

    @Test fun `WEEKLY_REVIEW dung model chat, ep goi submit_weekly_review, chi gui tool do`() = runBlocking {
        server.enqueue(reviewResponse())
        val r = provider().send(weekly)
        val b = server.takeRequest().json()
        assertEquals("deepseek-v4.1-flash", b["model"]!!.jsonPrimitive.content)
        assertEquals("submit_weekly_review", b["tool_choice"]!!.jsonObject["function"]!!.jsonObject["name"]!!.jsonPrimitive.content)
        val tools = b["tools"]!!.jsonArray
        assertEquals(1, tools.size)
        assertEquals("submit_weekly_review", tools[0].jsonObject["function"]!!.jsonObject["name"]!!.jsonPrimitive.content)
        val system = b["messages"]!!.jsonArray[0].jsonObject["content"]!!.jsonPrimitive.content
        assertTrue(system.contains("Đánh giá tuần"))
        val user = b["messages"]!!.jsonArray.last().toString()
        assertTrue(user.contains("weekly_stats"))
        r as AiResult.Review
        assertEquals("Ngày thường đều tay", r.review.headline)
        assertEquals(1L, r.callId)
        assertEquals(AiMode.WEEKLY_REVIEW, logs.single().mode)
    }

    @Test fun `WEEKLY_REVIEW khong goi tool hoac sai dinh dang thi INVALID_RESPONSE`() = runBlocking {
        server.enqueue(textResponse("Tuần này tốt"))
        server.enqueue(reviewResponse("""{"headline":"","highlights":[],"issues":[],"suggestions":[],"tone":"WARN"}"""))
        val r = provider().send(weekly)
        assertEquals(AiErrorType.INVALID_RESPONSE, (r as AiResult.Error).type)
    }
}
