package com.cali.core.ai

import com.cali.core.calc.WeeklyStatsCalculator
import com.cali.core.data.db.AppDatabase
import com.cali.core.data.db.entity.AiMode
import com.cali.core.data.db.entity.WeeklyReviewEntity
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.StatsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Trạng thái thẻ "Nhận xét của AI" trên S35 (08 §6 mục 8). */
sealed interface WeeklyReviewState {
    data object None : WeeklyReviewState
    data class Ready(val review: WeeklyReview, val createdAt: Long, val stale: Boolean, val partial: Boolean) : WeeklyReviewState
}

/** Nhận xét AI tuần (08 §5): chỉ tạo khi người dùng mở / bấm, lưu `weekly_review`, so `stats_hash` để báo đã đổi. */
@Singleton
class WeeklyReviewService @Inject constructor(
    private val db: AppDatabase,
    private val stats: StatsRepository,
    private val provider: AiProvider,
    private val contexts: UserContextBuilder,
    private val prompts: SystemPromptProvider,
    private val settings: SettingsRepository,
    private val clock: Clock,
) {
    private val dao = db.weeklyReviewDao()

    fun state(weekStart: LocalDate): Flow<WeeklyReviewState> =
        combine(dao.observeByWeek(weekStart), stats.observe(weekStart, weekStart.plusDays(6))) { row, data ->
            val review = row?.let { WeeklyReview.decode(it.reviewJson) }
            if (row == null || review == null) {
                WeeklyReviewState.None
            } else {
                WeeklyReviewState.Ready(review, row.createdAt, stale = row.statsHash != stats.hashOf(data, weekStart), partial = row.isPartialWeek)
            }
        }

    /** Lỗi trả [AiException] (UI chọn thông báo theo [AiException.type]). */
    suspend fun generate(weekStart: LocalDate): Result<WeeklyReview> {
        return try {
            val prevStart = weekStart.minusWeeks(1)
            val end = weekStart.plusDays(6)
            val data = stats.load(prevStart, end)
            val profile = data.profile ?: return Result.failure(AiException(AiErrorType.UNKNOWN, "Chưa có hồ sơ"))
            val week = weekStart..end
            val weekWeights = data.weights.filter { it.date in week }.map { it.kg }
            val prevWeights = data.weights.filter { it.date in prevStart..prevStart.plusDays(6) }.map { it.kg }
            val s = WeeklyStatsCalculator.compute(
                weekStart, data.today, data.daysIn(weekStart, end), data.foods,
                weekWeights, prevWeights.takeIf { it.isNotEmpty() }?.average(), data.firstUseDate,
            )
            val cmp = WeeklyStatsCalculator.compareSameDays(
                s, prevStart, 7, data.daysIn(prevStart, weekStart.minusDays(1)), data.today, data.firstUseDate,
            ).trends
            val dayFoods = data.foods.filter { it.date in week }.groupBy { it.date }.mapValues { (_, l) -> l.map { it.name to it.kcal } }
            val text = WeeklyStatsPrompt.build(s, profile.goalType, profile.weeklyRateKg, cmp, dayFoods)
            val ctx = contexts.build().getOrElse { return Result.failure(AiException(AiErrorType.UNKNOWN, it.message ?: "Thiếu hồ sơ")) }
            when (val r = provider.send(AiRequest(AiMode.WEEKLY_REVIEW, prompts.current(), ctx, userText = text))) {
                is AiResult.Review -> {
                    dao.put(
                        WeeklyReviewEntity(
                            weekStart = weekStart,
                            score = s.score.total,
                            statsHash = stats.hashOf(data, weekStart),
                            reviewJson = r.review.encode(),
                            isPartialWeek = s.isPartial,
                            aiCallId = r.callId,
                            createdAt = clock.millis(),
                        ),
                    )
                    Result.success(r.review)
                }
                is AiResult.Error -> Result.failure(AiException(r.type, r.message))
                else -> Result.failure(AiException(AiErrorType.INVALID_RESPONSE, "AI không trả nhận xét tuần"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(AiException(AiErrorType.UNKNOWN, e.message ?: "Lỗi"))
        }
    }
}
