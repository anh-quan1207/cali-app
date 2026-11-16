package com.cali.core.ai

import com.cali.core.calc.MealTypeResolver
import com.cali.core.calc.model.MealType
import com.cali.core.calc.model.Sex
import com.cali.core.data.db.entity.AiMode
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAiProviderTest {

    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val now = LocalDateTime.of(2026, 11, 13, 12, 35)
    private val clock = Clock.fixed(now.atZone(zone).toInstant(), zone)
    private val ctx = UserContext(
        now = now, sex = Sex.MALE, age = 30, heightCm = 170.0, weightKg = 74.1, targetWeightKg = 68.0,
        goalKcal = 1740, goalProteinG = 122, goalCarbG = 196, goalFatG = 52,
        eatenKcal = 1310, eatenProteinG = 70, eatenCarbG = 150, eatenFatG = 40,
    )

    @Test fun `tối qua ăn thêm 1 gói mì tôm → hôm qua, bữa Tối`() = runBlocking {
        val fake = FakeAiProvider(clock)
        val r = fake.send(AiRequest(AiMode.CHAT, "p", ctx, userText = "tối qua ăn thêm 1 gói mì tôm")) as AiResult.FoodLog
        val t = FoodLogTiming(MealTypeResolver()).resolve(r.payload, now)
        assertEquals(LocalDateTime.of(2026, 11, 12, 19, 45), t.eatenAt)
        assertEquals(MealType.DINNER, t.mealType)
        assertEquals("Mì tôm", r.payload.items.single().name)
    }

    @Test fun `ảnh mẫu ra bữa sáng của bộ dữ liệu mẫu`() = runBlocking {
        val r = FakeAiProvider(clock).send(AiRequest(AiMode.PHOTO_LOG, "p", ctx, imageJpeg = byteArrayOf(1))) as AiResult.FoodLog
        assertEquals(520.0, r.payload.items.sumOf { it.kcal }, 0.0)
    }

    @Test fun `câu hỏi ra tư vấn chữ`() = runBlocking {
        val r = FakeAiProvider(clock).send(AiRequest(AiMode.CHAT, "p", ctx, userText = "tối nay ăn gì?")) as AiResult.Text
        assertTrue("430" in r.text)
    }

    @Test fun `lỗi giả lập dùng một lần`() = runBlocking {
        val fake = FakeAiProvider(clock).apply { nextError = AiErrorType.NO_NETWORK }
        val req = AiRequest(AiMode.PHOTO_LOG, "p", ctx, imageJpeg = byteArrayOf(1))
        assertEquals(AiErrorType.NO_NETWORK, (fake.send(req) as AiResult.Error).type)
        assertTrue(fake.send(req) is AiResult.FoodLog)
    }
}
