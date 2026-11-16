package com.cali.core.ai.anthropic

import com.cali.core.ai.AiCallRecorder
import com.cali.core.ai.AiErrorType
import com.cali.core.ai.AiException
import com.cali.core.ai.AiModelSource
import com.cali.core.ai.AiRequest
import com.cali.core.ai.AiResult
import com.cali.core.ai.ApiKeySource
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
import kotlinx.serialization.json.boolean
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

class AnthropicProviderTest {

    private val key = "sk-ant-test-0123456789-SECRET"
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
    ) = AnthropicProvider(
        client = client,
        keySource = ApiKeySource { apiKey },
        models = AiModelSource { mode -> if (mode == AiMode.PHOTO_LOG) "claude-sonnet-5-5" else "claude-haiku-5-5" },
        recorder = AiCallRecorder { logs += it; logs.size.toLong() },
        config = AnthropicConfig(baseUrl = baseUrl),
        clock = clock,
        sleep = { sleeps += it },
    )

    private fun ok(body: String) = MockResponse.Builder().code(200)
        .addHeader("content-type", "application/json").body(body).build()

    private fun err(code: Int, type: String = "error", msg: String = "lỗi") = MockResponse.Builder().code(code)
        .addHeader("content-type", "application/json")
        .body("""{"type":"error","error":{"type":"$type","message":"$msg"}}""").build()

    private val goodInput = """{"items":[{"name":"Phở bò","portion_desc":"1 tô vừa","grams":500,"kcal":452,
        "protein_g":25,"carb_g":60,"fat_g":12,"confidence":0.9}],"meal_type":"LUNCH"}"""

    private fun toolResponse(input: String = goodInput, model: String = "claude-sonnet-5-5") = ok(
        """{"id":"msg_1","type":"message","role":"assistant","model":"$model",
        "content":[{"type":"thinking","thinking":"","signature":"sig"},
          {"type":"text","text":"Mình ghi nhé."},
          {"type":"tool_use","id":"toolu_1","name":"record_food_items","input":$input}],
        "stop_reason":"tool_use","stop_details":null,
        "usage":{"input_tokens":1200,"output_tokens":300,"cache_read_input_tokens":1000,"cache_creation_input_tokens":50}}""",
    )

    private fun textResponse(text: String = "Bạn còn 920 kcal.") = ok(
        """{"id":"msg_2","type":"message","role":"assistant","model":"claude-haiku-5-5",
        "content":[{"type":"text","text":"$text"}],"stop_reason":"end_turn",
        "usage":{"input_tokens":900,"output_tokens":40}}""",
    )

    private fun RecordedRequest.json(): JsonObject = Json.parseToJsonElement(body!!.utf8()).jsonObject

    // --- Request ---

    @Test fun `PHOTO_LOG gửi đúng header và body`() = runBlocking {
        server.enqueue(toolResponse())
        provider().send(photo)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/v1/messages", req.url.encodedPath)
        assertEquals(key, req.headers["x-api-key"])
        assertEquals("2026-10-21", req.headers["anthropic-version"])
        assertTrue(req.headers["content-type"]!!.startsWith("application/json"))
        assertEquals("server-side-fallback-2026-07-01", req.headers["anthropic-beta"])

        val b = req.json()
        assertEquals("claude-sonnet-5-5", b["model"]!!.jsonPrimitive.content)
        assertEquals(8000, b["max_tokens"]!!.jsonPrimitive.int)
        assertEquals("auto", b["tool_choice"]!!.jsonObject["type"]!!.jsonPrimitive.content)
        val tool = b["tools"]!!.jsonArray.single().jsonObject
        assertEquals("record_food_items", tool["name"]!!.jsonPrimitive.content)
        assertTrue(tool["strict"]!!.jsonPrimitive.boolean)
        assertEquals("object", tool["input_schema"]!!.jsonObject["type"]!!.jsonPrimitive.content)
        val system = b["system"]!!.jsonArray
        assertEquals(2, system.size)
        assertEquals("ephemeral", system[0].jsonObject["cache_control"]!!.jsonObject["type"]!!.jsonPrimitive.content)
        assertTrue(system[0].jsonObject["text"]!!.jsonPrimitive.content.startsWith("PROMPT"))
        assertNull(system[1].jsonObject["cache_control"])
        assertTrue(system[1].jsonObject["text"]!!.jsonPrimitive.content.startsWith("<user_context>"))
        assertEquals("medium", b["output_config"]!!.jsonObject["effort"]!!.jsonPrimitive.content)
        assertEquals("default", b["fallbacks"]!!.jsonPrimitive.content)

        val content = b["messages"]!!.jsonArray.single().jsonObject["content"]!!.jsonArray
        val image = content[0].jsonObject
        assertEquals("image", image["type"]!!.jsonPrimitive.content)
        val source = image["source"]!!.jsonObject
        assertEquals("base64", source["type"]!!.jsonPrimitive.content)
        assertEquals("image/jpeg", source["media_type"]!!.jsonPrimitive.content)
        assertEquals(Base64.getEncoder().encodeToString(jpeg), source["data"]!!.jsonPrimitive.content)
        assertEquals("ăn một nửa", content[1].jsonObject["text"]!!.jsonPrimitive.content)

        for (k in listOf("temperature", "top_p", "top_k", "thinking", "stream")) assertFalse(k, k in b)
    }

    @Test fun `CHAT dùng model chat, effort low, không fallback`() = runBlocking {
        server.enqueue(textResponse())
        provider().send(chat)
        val req = server.takeRequest()
        assertNull(req.headers["anthropic-beta"])
        val b = req.json()
        assertEquals("claude-haiku-5-5", b["model"]!!.jsonPrimitive.content)
        assertEquals("low", b["output_config"]!!.jsonObject["effort"]!!.jsonPrimitive.content)
        assertFalse("fallbacks" in b)
        assertEquals("auto", b["tool_choice"]!!.jsonObject["type"]!!.jsonPrimitive.content)
    }

    // --- Response ---

    @Test fun `đọc tool_use đứng sau khối thinking, ghi AiCallLog`() = runBlocking {
        server.enqueue(toolResponse())
        val r = provider().send(photo) as AiResult.FoodLog
        val item = r.payload.items.single()
        assertEquals("Phở bò", item.name)
        assertEquals(450.0, item.kcal, 0.0) // đã qua validator: làm tròn bội 5
        assertEquals(1200, r.usage.inputTokens)
        assertEquals(300, r.usage.outputTokens)
        assertEquals(1000, r.usage.cacheReadTokens)
        assertEquals(50, r.usage.cacheCreationTokens)
        assertEquals(1L, r.callId)

        val log = logs.single()
        assertEquals(AiMode.PHOTO_LOG, log.mode)
        assertEquals("anthropic", log.provider)
        assertEquals("claude-sonnet-5-5", log.model)
        assertEquals(1200, log.inputTokens)
        assertEquals(300, log.outputTokens)
        assertEquals(1000, log.cacheReadTokens)
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

    @Test fun `CHAT có tool_use thì ra thẻ món`() = runBlocking {
        server.enqueue(toolResponse(model = "claude-haiku-5-5"))
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

    @Test fun `JSON sai schema hai lần thì báo INVALID_RESPONSE`() = runBlocking {
        val bad = """{"items":[{"name":"Phở"}]}"""
        server.enqueue(toolResponse(bad))
        server.enqueue(toolResponse(bad))
        val r = provider().send(photo) as AiResult.Error
        assertEquals(AiErrorType.INVALID_RESPONSE, r.type)
        assertEquals(2, server.requestCount)
        assertEquals(2, logs.size)
    }

    @Test fun `refusal thì báo REFUSED, không gọi lại`() = runBlocking {
        server.enqueue(
            ok(
                """{"id":"m","type":"message","role":"assistant","model":"claude-sonnet-5-5","content":[],
                "stop_reason":"refusal","stop_details":{"type":"refusal","category":null,"explanation":"x"},
                "usage":{"input_tokens":10,"output_tokens":0}}""",
            ),
        )
        val r = provider().send(photo) as AiResult.Error
        assertEquals(AiErrorType.REFUSED, r.type)
        assertEquals(1, server.requestCount)
    }

    // --- Lỗi HTTP ---

    @Test fun `401 là key sai, không retry`() = runBlocking {
        server.enqueue(err(401, "authentication_error", "invalid x-api-key"))
        val r = provider().send(chat) as AiResult.Error
        assertEquals(AiErrorType.INVALID_KEY, r.type)
        assertEquals(1, server.requestCount)
        assertTrue(sleeps.isEmpty())
        assertFalse(logs.single().success)
        assertEquals("INVALID_KEY", logs.single().errorType)
    }

    @Test fun `403 cũng là key sai`() = runBlocking {
        server.enqueue(err(403, "permission_error"))
        assertEquals(AiErrorType.INVALID_KEY, (provider().send(chat) as AiResult.Error).type)
    }

    @Test fun `429 retry 2 lần với backoff 2s rồi 5s`() = runBlocking {
        server.enqueue(err(429, "rate_limit_error"))
        server.enqueue(err(429, "rate_limit_error"))
        server.enqueue(textResponse())
        val r = provider().send(chat)
        assertTrue(r is AiResult.Text)
        assertEquals(3, server.requestCount)
        assertEquals(listOf(2000L, 5000L), sleeps)
        assertEquals(listOf(false, false, true), logs.map { it.success })
    }

    @Test fun `429 ba lần thì dừng sau 2 lần retry`() = runBlocking {
        repeat(4) { server.enqueue(err(429, "rate_limit_error")) }
        val r = provider().send(chat) as AiResult.Error
        assertEquals(AiErrorType.RATE_LIMITED, r.type)
        assertEquals(3, server.requestCount)
        assertEquals(listOf(2000L, 5000L), sleeps)
    }

    @Test fun `529 quá tải ba lần`() = runBlocking {
        repeat(3) { server.enqueue(err(529, "overloaded_error")) }
        assertEquals(AiErrorType.OVERLOADED, (provider().send(chat) as AiResult.Error).type)
        assertEquals(3, server.requestCount)
    }

    @Test fun `500 rồi thành công`() = runBlocking {
        server.enqueue(err(500, "api_error"))
        server.enqueue(textResponse())
        assertTrue(provider().send(chat) is AiResult.Text)
        assertEquals(listOf(2000L), sleeps)
    }

    @Test fun `400 khác báo UNKNOWN kèm thông điệp`() = runBlocking {
        server.enqueue(err(400, "invalid_request_error", "max_tokens: quá lớn"))
        val r = provider().send(chat) as AiResult.Error
        assertEquals(AiErrorType.UNKNOWN, r.type)
        assertTrue(r.message, "max_tokens" in r.message)
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
        val c = AnthropicProvider.defaultClient(AnthropicConfig(), logger = null)
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

    @Test fun `log HTTP che x-api-key`() = runBlocking {
        server.enqueue(toolResponse())
        provider().send(photo)
        assertTrue(httpLog.isNotEmpty())
        assertTrue(httpLog.none { key in it })
        assertTrue(httpLog.joinToString("\n"), httpLog.any { it.startsWith("x-api-key: ██") })
        assertTrue(httpLog.any { it.contains("/v1/messages") })
        // Không log body (ảnh, ngữ cảnh người dùng).
        assertTrue(httpLog.none { "user_context" in it || "Phở" in it })
    }

    @Test fun `interceptor che cả Authorization`() {
        val lines = mutableListOf<String>()
        server.enqueue(ok("{}"))
        val client = OkHttpClient.Builder().addInterceptor(RedactingLogInterceptor { lines += it }).build()
        client.newCall(
            okhttp3.Request.Builder().url(server.url("/x")).header("Authorization", "Bearer TOKEN123").build(),
        ).execute().close()
        assertTrue(lines.none { "TOKEN123" in it })
        assertTrue(lines.any { it == "Authorization: ██" })
    }

    // --- testConnection ---

    @Test fun `kiểm tra kết nối dùng GET models, không tốn token`() = runBlocking {
        server.enqueue(ok("""{"data":[],"has_more":false}"""))
        assertTrue(provider().testConnection().isSuccess)
        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/v1/models", req.url.encodedPath)
        assertEquals("1", req.url.queryParameter("limit"))
        assertEquals(key, req.headers["x-api-key"])
        assertEquals("2026-10-21", req.headers["anthropic-version"])
    }

    @Test fun `kiểm tra kết nối với key sai`() = runBlocking {
        server.enqueue(err(401, "authentication_error"))
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
        server.enqueue(err(429, "rate_limit_error"))
        val e = provider().testConnection().exceptionOrNull() as AiException
        assertEquals(AiErrorType.RATE_LIMITED, e.type)
    }
}
