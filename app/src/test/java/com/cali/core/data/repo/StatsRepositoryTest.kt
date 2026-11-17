package com.cali.core.data.repo

import com.cali.core.ai.AiErrorType
import com.cali.core.ai.AiProvider
import com.cali.core.ai.AiRequest
import com.cali.core.ai.AiResult
import com.cali.core.ai.ReviewTone
import com.cali.core.ai.SystemPromptProvider
import com.cali.core.ai.TokenUsage
import com.cali.core.ai.UserContextBuilder
import com.cali.core.ai.WeeklyReview
import com.cali.core.ai.WeeklyReviewService
import com.cali.core.ai.WeeklyReviewState
import com.cali.core.calc.model.MealType
import com.cali.core.data.db.DbTestBase
import com.cali.core.data.db.entity.AiMode
import com.cali.core.data.db.entity.GoalSnapshotEntity
import com.cali.core.data.db.entity.WaterSource
import com.cali.core.data.prefs.runTestWithScope
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** GĐ6: dữ liệu kỳ cho S30–S35 (mục tiêu theo snapshot từng ngày, nước, ngày bắt đầu dùng app), nhận xét tuần. */
@RunWith(RobolectricTestRunner::class)
class StatsRepositoryTest : DbTestBase() {
    @get:Rule val tmp = TemporaryFolder()

    private val lastMonday = LocalDate.of(2026, 11, 9)

    private fun snapshot(from: LocalDate, kcal: Int, water: Int, tdee: Double = 2300.0) = GoalSnapshotEntity(
        effectiveFrom = from, weightKgUsed = 76.0, bmr = 1670.0, tdee = tdee, targetKcal = kcal,
        proteinG = 120, carbG = 200, fatG = 55, waterMl = water,
        isKcalOverridden = false, isMacroOverridden = false, clampReason = null, createdAt = 0,
    )

    private fun at(d: LocalDate, h: Int) = LocalDateTime.of(d, LocalTime.of(h, 0))

    @Test fun `muc tieu tung ngay theo snapshot, so mon, nuoc, ngay bat dau dung app`() = runTestWithScope {
        val settings = newSettings(tmp, "a")
        ProfileRepository(db, settings, TEST_CLOCK).completeOnboarding(TEST_PROFILE, 75.0).getOrThrow() // snapshot 16/11: 1740 / 2600
        db.goalSnapshotDao().put(snapshot(LocalDate.of(2026, 11, 2), 1800, 2500, tdee = 2350.0))
        val diary = DiaryRepository(db, TEST_CLOCK)
        val water = WaterRepository(db, settings, TEST_CLOCK) {}
        diary.add(listOf(food("Phở bò", 450.0, 25.0, 60.0, 12.0), food("Trà sữa", 400.0)), at(lastMonday, 8), MealType.BREAKFAST, false).getOrThrow()
        diary.add(listOf(food("Cơm tấm", 650.0)), at(LocalDate.of(2026, 11, 1), 12), MealType.LUNCH, false).getOrThrow()
        water.add(lastMonday, 1000, LocalTime.of(9, 0), WaterSource.APP).getOrThrow()

        val repo = StatsRepository(db, settings, TEST_CLOCK)
        val data = repo.load(lastMonday, TEST_TODAY)
        val mon = data.days.getValue(lastMonday)
        assertEquals(2, mon.entryCount)
        assertEquals(850.0, mon.kcal, 1e-9)
        assertEquals(25.0, mon.proteinG, 1e-9)
        assertEquals(1000, mon.waterMl)
        assertEquals(1800, mon.targetKcal)
        assertEquals(2500, mon.targetWaterMl)
        assertEquals(2350.0, mon.tdee, 1e-9)
        // Ngày không có món vẫn có mục tiêu của ngày (để S35 vẽ vạch, tính nước).
        assertEquals(0, data.days.getValue(lastMonday.plusDays(1)).entryCount)
        assertEquals(1740, data.days.getValue(TEST_TODAY).targetKcal)
        assertEquals(8, data.days.size)
        assertEquals(LocalDate.of(2026, 11, 1), data.firstUseDate) // món cũ hơn ngày tạo hồ sơ
        assertEquals(TEST_TODAY, data.today)
        assertEquals(listOf("Phở bò", "Trà sữa"), data.foods.map { it.name })
        assertEquals(75.0, data.weights.single().kg, 1e-9)

        // Ngày trước mọi snapshot (01/11) lấy snapshot sớm nhất.
        assertEquals(1800, repo.load(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1)).days.values.single().targetKcal)

        // Nước tùy chỉnh (S43) thắng mục tiêu tự động.
        settings.update { it.copy(waterGoalOverrideMl = 3000) }.getOrThrow()
        assertEquals(3000, repo.load(lastMonday, lastMonday).days.values.single().targetWaterMl)
    }

    @Test fun `chua co ho so thi khong co ngay bat dau`() = runTestWithScope {
        val repo = StatsRepository(db, newSettings(tmp, "b"), TEST_CLOCK)
        val data = repo.load(lastMonday, TEST_TODAY)
        assertNull(data.firstUseDate)
        assertEquals(0, data.days.getValue(lastMonday).targetKcal)
    }

    @Test fun `observe phat lai khi them mon`() = runTestWithScope {
        val settings = newSettings(tmp, "c")
        ProfileRepository(db, settings, TEST_CLOCK).completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        val repo = StatsRepository(db, settings, TEST_CLOCK)
        assertEquals(0, repo.observe(TEST_TODAY, TEST_TODAY).first().days.getValue(TEST_TODAY).entryCount)
        DiaryRepository(db, TEST_CLOCK).add(listOf(food("Xôi", 500.0)), at(TEST_TODAY, 7), MealType.BREAKFAST, false).getOrThrow()
        assertEquals(1, repo.observe(TEST_TODAY, TEST_TODAY).first().days.getValue(TEST_TODAY).entryCount)
    }

    // ---------- Nhận xét AI tuần (08 §5) ----------

    private val review = WeeklyReview("Tuần đều tay", listOf("a"), listOf("b"), emptyList(), listOf("1", "2", "3"), ReviewTone.ENCOURAGE)

    private class StubAi(var result: AiResult) : AiProvider {
        val requests = mutableListOf<AiRequest>()
        override suspend fun send(request: AiRequest): AiResult { requests += request; return result }
        override suspend fun testConnection(): Result<Unit> = Result.success(Unit)
    }

    @Test fun `tao nhan xet, luu, doi du lieu thi bao da thay doi`() = runTestWithScope {
        val settings = newSettings(tmp, "d")
        ProfileRepository(db, settings, TEST_CLOCK).completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        db.goalSnapshotDao().put(snapshot(LocalDate.of(2026, 11, 2), 1740, 2600))
        val diary = DiaryRepository(db, TEST_CLOCK)
        val id = diary.add(listOf(food("Phở bò", 450.0)), at(lastMonday, 8), MealType.BREAKFAST, false).getOrThrow().single()
        val stats = StatsRepository(db, settings, TEST_CLOCK)
        val ai = StubAi(AiResult.Review(review, TokenUsage(), callId = null))
        val service = WeeklyReviewService(
            db, stats, ai, UserContextBuilder(db, diary, settings, TEST_CLOCK), SystemPromptProvider { "PROMPT" }, settings, TEST_CLOCK,
        )

        assertTrue(service.state(lastMonday).first() is WeeklyReviewState.None)
        assertTrue(service.generate(lastMonday).isSuccess)
        val req = ai.requests.single()
        assertEquals(AiMode.WEEKLY_REVIEW, req.mode)
        assertTrue(req.userText!!, req.userText.contains("<weekly_stats>"))
        assertTrue(req.userText.contains("Phở bò"))

        val saved = service.state(lastMonday).first() as WeeklyReviewState.Ready
        assertEquals("Tuần đều tay", saved.review.headline)
        assertEquals(false, saved.stale)
        val row = db.weeklyReviewDao().byWeek(lastMonday)!!
        assertEquals(false, row.isPartialWeek)

        // Sửa món cũ trong tuần → "Dữ liệu đã thay đổi · Tạo lại".
        diary.put(diary.byId(id)!!.copy(kcal = 600.0)).getOrThrow()
        val after = service.state(lastMonday).first() as WeeklyReviewState.Ready
        assertTrue(after.stale)
        assertNotEquals(row.statsHash, stats.hashOf(stats.load(lastMonday, lastMonday.plusDays(6)), lastMonday))
    }

    @Test fun `loi AI tra ve that bai, khong luu`() = runTestWithScope {
        val settings = newSettings(tmp, "e")
        ProfileRepository(db, settings, TEST_CLOCK).completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        val diary = DiaryRepository(db, TEST_CLOCK)
        val stats = StatsRepository(db, settings, TEST_CLOCK)
        val service = WeeklyReviewService(
            db, stats, StubAi(AiResult.Error(AiErrorType.NO_NETWORK, "x")),
            UserContextBuilder(db, diary, settings, TEST_CLOCK), SystemPromptProvider { "PROMPT" }, settings, TEST_CLOCK,
        )
        val r = service.generate(lastMonday)
        assertEquals(AiErrorType.NO_NETWORK, r.exceptionOrNull()!!.let { (it as com.cali.core.ai.AiException).type })
        assertNull(db.weeklyReviewDao().byWeek(lastMonday))
    }
}
