package com.cali.core.ai

import com.cali.core.data.db.entity.AiMode
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.delay

/**
 * Provider giả cho test, preview và chế độ demo khi chưa có key (06 §8).
 * Trả payload mẫu đi qua [FoodLogValidator] như kết quả thật. Đặt [nextError] để giả lập lỗi một lần.
 */
class FakeAiProvider(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val latencyMs: Long = 0,
) : AiProvider {

    var nextError: AiErrorType? = null
    var connectionError: AiErrorType? = null
    val requests = mutableListOf<AiRequest>()

    override suspend fun send(request: AiRequest): AiResult {
        requests += request
        if (latencyMs > 0) delay(latencyMs)
        nextError?.let { nextError = null; return AiResult.Error(it, "Lỗi giả lập: $it") }

        val wantsAdvice = request.mode == AiMode.CHAT && request.imageJpeg == null &&
            request.userText?.trimEnd()?.endsWith("?") == true
        if (wantsAdvice) {
            return AiResult.Text("Bạn còn ${request.context.goalKcal - request.context.eatenKcal} kcal cho hôm nay.", USAGE)
        }

        val today = LocalDate.now(clock)
        val raw = if (request.userText?.contains("tối qua", ignoreCase = true) == true) {
            yesterdayNoodles(today.minusDays(1))
        } else {
            SAMPLE_BREAKFAST
        }
        return when (val v = FoodLogValidator.validate(raw, today)) {
            is FoodLogValidator.Outcome.Valid -> AiResult.FoodLog(v.log, USAGE)
            is FoodLogValidator.Outcome.Invalid -> AiResult.Error(AiErrorType.INVALID_RESPONSE, v.reason)
        }
    }

    override suspend fun testConnection(): Result<Unit> =
        connectionError?.let { Result.failure(AiException(it, "Lỗi giả lập: $it")) } ?: Result.success(Unit)

    companion object {
        val USAGE = TokenUsage(inputTokens = 1500, outputTokens = 250, cacheReadTokens = 1200)

        /** Bữa sáng của bộ dữ liệu mẫu (bánh mì trứng 380 + cà phê sữa đá 140). */
        const val SAMPLE_BREAKFAST = """
            {"items":[
              {"name":"Bánh mì trứng","portion_desc":"1 ổ","grams":180,"kcal":380,"protein_g":15,"carb_g":45,"fat_g":15,
               "confidence":0.85,"assumptions":"2 trứng ốp, ít bơ"},
              {"name":"Cà phê sữa đá","portion_desc":"1 ly","grams":250,"kcal":140,"protein_g":3,"carb_g":24,"fat_g":3.5,
               "confidence":0.8,"assumptions":"sữa đặc mức phổ biến"}
            ],"comment":"Bữa sáng khoảng 520 kcal."}
        """

        private fun yesterdayNoodles(date: LocalDate) = """
            {"items":[{"name":"Mì tôm","portion_desc":"1 gói","grams":75,"kcal":350,"protein_g":7,"carb_g":46,
              "fat_g":15,"confidence":0.9}],"meal_type":"DINNER","eaten_date":"$date"}
        """
    }
}
