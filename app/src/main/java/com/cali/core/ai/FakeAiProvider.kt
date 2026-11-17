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

        if (request.mode == AiMode.WEEKLY_REVIEW) {
            return AiResult.Review(
                WeeklyReview(
                    headline = "Ngày thường đều tay, cuối tuần kéo tổng calo vượt mục tiêu.",
                    highlights = listOf("4/7 ngày trong mục tiêu.", "Cân TB giảm, khớp với dự kiến theo lượng ăn."),
                    issues = listOf("T7 và CN vượt nhiều, xoá phần thâm hụt của 5 ngày đầu.", "T4 chỉ ghi 690 kcal, có thể còn sót món."),
                    patterns = listOf("Cuối tuần vừa ăn vượt vừa uống ít nước."),
                    suggestions = listOf(
                        "Trà sữa gọi 50% đường, bỏ trân châu: bớt khoảng 150 kcal mỗi ly.",
                        "Thêm 2 quả trứng vào bữa sáng: khoảng +12 g đạm mỗi ngày.",
                        "T7–CN chọn trước bữa tối quanh 600 kcal và bật nhắc nước mỗi 2 giờ.",
                    ),
                    tone = ReviewTone.ENCOURAGE,
                ),
                USAGE,
            )
        }
        val wantsAdvice = request.mode == AiMode.CHAT && request.imageJpeg == null &&
            request.userText?.trimEnd()?.endsWith("?") == true
        if (wantsAdvice) {
            return AiResult.Text("Bạn còn ${request.context.goalKcal - request.context.eatenKcal} kcal cho hôm nay.", USAGE)
        }

        val today = LocalDate.now(clock)
        // So khớp không dấu để gõ được bằng adb khi thử tay.
        val text = com.cali.core.data.db.TextNormalizer.normalize(request.userText.orEmpty())
        val raw = when {
            "toi qua" in text -> yesterdayNoodles(today.minusDays(1))
            "nuoc mam" in text -> SAMPLE_UNSURE
            "thuc don" in text -> SAMPLE_NO_FOOD
            "bun bo" in text -> SAMPLE_LUNCH
            else -> SAMPLE_BREAKFAST
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

        /** Bữa trưa của bộ dữ liệu mẫu (s20 f1): bún bò 490 + trà đá 0. */
        const val SAMPLE_LUNCH = """
            {"items":[
              {"name":"Bún bò Huế","portion_desc":"1 tô vừa","grams":550,"kcal":490,"protein_g":33,"carb_g":60,"fat_g":12,"confidence":0.85},
              {"name":"Trà đá","portion_desc":"1 ly","grams":300,"kcal":0,"protein_g":0,"carb_g":0,"fat_g":0,"confidence":0.95}
            ],"comment":"Bữa này vừa phải."}
        """

        /** s12 f3: một món độ tin cậy thấp + câu hỏi làm rõ. */
        const val SAMPLE_UNSURE = """
            {"items":[
              {"name":"Cơm tấm sườn","portion_desc":"1 dĩa","grams":350,"kcal":650,"protein_g":32,"carb_g":80,"fat_g":20,"confidence":0.85},
              {"name":"Mỡ hành + nước mắm","portion_desc":"2 thìa · 1 chén","grams":60,"kcal":140,"protein_g":1,"carb_g":8,"fat_g":12,
               "confidence":0.4,"assumptions":"~2 thìa mỡ hành, chan hết 1 chén nước mắm pha đường"}
            ],"clarifying_question":"Bạn có chan hết chén nước mắm không?"}
        """

        /** s12 f7: ảnh không có món ăn. */
        const val SAMPLE_NO_FOOD = """
            {"items":[],"comment":"Ảnh có vẻ là thực đơn quán, chưa thấy món ăn thật. Bạn chụp lại phần ăn nhé."}
        """

        private fun yesterdayNoodles(date: LocalDate) = """
            {"items":[{"name":"Mì tôm","portion_desc":"1 gói","grams":75,"kcal":350,"protein_g":7,"carb_g":46,
              "fat_g":15,"confidence":0.9}],"meal_type":"DINNER","eaten_date":"$date"}
        """
    }
}
