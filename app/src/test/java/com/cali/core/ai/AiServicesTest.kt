package com.cali.core.ai

import com.cali.core.calc.FoodValues
import com.cali.core.calc.model.MealType
import com.cali.core.data.db.DbTestBase
import com.cali.core.data.db.entity.AiCallLogEntity
import com.cali.core.data.db.entity.AiMode
import com.cali.core.data.db.entity.ChatRole as DbRole
import com.cali.core.data.db.entity.FoodLogStatus
import com.cali.core.data.prefs.runTestWithScope
import com.cali.core.data.repo.ChatRepository
import com.cali.core.data.repo.ChatSendResult
import com.cali.core.data.repo.DiaryRepository
import com.cali.core.data.repo.MyFoodRepository
import com.cali.core.data.repo.ProfileRepository
import com.cali.core.data.repo.TEST_CLOCK
import com.cali.core.data.repo.TEST_PROFILE
import com.cali.core.data.repo.TEST_TODAY
import com.cali.core.data.repo.food
import com.cali.core.data.repo.newSettings
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AiServicesTest : DbTestBase() {
    @get:Rule val tmp = TemporaryFolder()

    private val prompt = SystemPromptProvider { "PROMPT" }

    /** Bộ số mẫu: mục tiêu 1.740, sáng 820 (07:20), Món của tôi 2 món, ghi chú cá nhân. */
    private suspend fun com.cali.core.data.prefs.TestScopeHolder.seed(name: String): Pair<UserContextBuilder, DiaryRepository> {
        val settings = newSettings(tmp, name)
        ProfileRepository(db, settings, TEST_CLOCK).completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        settings.update { it.copy(personalNotes = "Không ăn hành") }.getOrThrow()
        val diary = DiaryRepository(db, TEST_CLOCK)
        diary.add(
            listOf(food("Bánh mì ốp la", 280.0, 12.0, 30.0, 12.0), food("Xôi gà", 540.0, 20.0, 80.0, 15.0)),
            LocalDateTime.of(TEST_TODAY, LocalTime.of(7, 20)), MealType.BREAKFAST, false,
        ).getOrThrow()
        val my = MyFoodRepository(db, TEST_CLOCK)
        val com = my.create("Cơm nhà", "1 bát", 180.0, FoodValues(230.0, 4.0, 50.0, 0.5), false).getOrThrow()
        my.create("Phở bò", "1 tô", null, FoodValues(470.0, 30.0, 60.0, 12.0), false).getOrThrow()
        db.myFoodDao().markUsed(com, 5)
        return UserContextBuilder(db, diary, settings, TEST_CLOCK) to diary
    }

    private fun service(ctx: UserContextBuilder, provider: AiProvider = FakeAiProvider(TEST_CLOCK)) =
        AiLogService(provider, ctx, prompt, TEST_CLOCK)

    // --- UserContextBuilder ---

    @Test fun `ngữ cảnh lấy hồ sơ, mục tiêu, món hôm nay, món hay ăn, ghi chú`() = runTestWithScope {
        val (builder, _) = seed("a")
        val c = builder.build(logTime = LocalTime.of(12, 35)).getOrThrow()
        assertEquals(LocalDateTime.of(TEST_TODAY, LocalTime.of(12, 38)), c.now)
        assertEquals(30, c.age) // 2026 − 1996
        assertEquals(75.0, c.weightKg, 0.0)
        assertEquals(68.0, c.targetWeightKg!!, 0.0)
        assertEquals(1740, c.goalKcal)
        assertEquals(122, c.goalProteinG)
        assertEquals(820, c.eatenKcal)
        assertEquals(32, c.eatenProteinG)
        assertEquals(2, c.todayEntries.size)
        assertEquals(MealType.BREAKFAST, c.todayEntries[0].meal)
        assertEquals(LocalTime.of(7, 20), c.todayEntries[0].time)
        assertEquals(LocalTime.of(12, 35), c.logTime)
        assertEquals("Không ăn hành", c.personalNotes)
        assertEquals(listOf("Cơm nhà (1 bát 180 g)", "Phở bò (1 tô)"), c.frequentFoods)
    }

    @Test fun `chưa có hồ sơ thì ngữ cảnh thất bại, không ném`() = runTestWithScope {
        val builder = UserContextBuilder(db, DiaryRepository(db, TEST_CLOCK), newSettings(tmp, "b"), TEST_CLOCK)
        assertTrue(builder.build().isFailure)
    }

    // --- AiLogService ---

    @Test fun `phân tích ảnh ra thẻ món kèm giờ bữa theo lúc chụp`() = runTestWithScope {
        val (builder, _) = seed("c")
        val fake = FakeAiProvider(TEST_CLOCK)
        val at = LocalDateTime.of(TEST_TODAY, LocalTime.of(7, 30))
        val r = service(builder, fake).analyzePhoto(byteArrayOf(1, 2), "ăn một nửa", at) as AiOutcome.FoodLog
        assertEquals(listOf("Bánh mì trứng", "Cà phê sữa đá"), r.log.items.map { it.name })
        assertEquals(at, r.timing.eatenAt)
        assertEquals(MealType.BREAKFAST, r.timing.mealType)
        val req = fake.requests.single()
        assertEquals(AiMode.PHOTO_LOG, req.mode)
        assertEquals("PROMPT", req.systemPrompt)
        assertEquals("ăn một nửa", req.userText)
        assertEquals(LocalTime.of(7, 30), req.context.logTime)
    }

    @Test fun `lỗi của provider đi ra thành Failed`() = runTestWithScope {
        val (builder, _) = seed("d")
        val fake = FakeAiProvider(TEST_CLOCK).apply { nextError = AiErrorType.NO_NETWORK }
        val r = service(builder, fake).analyzePhoto(byteArrayOf(1), null, LocalDateTime.of(TEST_TODAY, LocalTime.NOON))
        assertEquals(AiErrorType.NO_NETWORK, (r as AiOutcome.Failed).type)
    }

    @Test fun `trả lời câu hỏi làm rõ gửi lượt CHAT kèm kết quả trước`() = runTestWithScope {
        val (builder, _) = seed("e")
        val fake = FakeAiProvider(TEST_CLOCK)
        val prev = (service(builder, fake).analyzePhoto(byteArrayOf(1), null, LocalDateTime.of(TEST_TODAY, LocalTime.of(7, 30))) as AiOutcome.FoodLog).log
        val r = service(builder, fake).clarify(prev, "Bạn chan hết không?", "chan hết luôn", hadImage = true, LocalDateTime.of(TEST_TODAY, LocalTime.of(7, 30)))
        assertTrue(r is AiOutcome.FoodLog)
        val req = fake.requests.last()
        assertEquals(AiMode.CHAT, req.mode)
        assertEquals("chan hết luôn", req.userText)
        assertEquals(2, req.history.size)
        assertTrue(req.history[0].hadImage)
        assertEquals(listOf("Bánh mì trứng", "Cà phê sữa đá"), req.history[0].loggedItemNames)
        assertEquals("Bạn chan hết không?", req.history[1].text)
    }

    // --- ChatRepository ---

    private suspend fun com.cali.core.data.prefs.TestScopeHolder.chat(name: String, fake: FakeAiProvider = FakeAiProvider(TEST_CLOCK)): Pair<ChatRepository, FakeAiProvider> {
        val (builder, _) = seed(name)
        return ChatRepository(db, service(builder, fake), TEST_CLOCK) to fake
    }

    @Test fun `chat ghi món - tin người dùng + thẻ chờ xác nhận`() = runTestWithScope {
        val (repo, _) = chat("f")
        val r = repo.send(TEST_TODAY, "trưa ăn bánh mì với cà phê", null, null)
        assertTrue(r is ChatSendResult.Sent)
        assertNull((r as ChatSendResult.Sent).movedTo)
        val msgs = repo.observe(TEST_TODAY).first()
        assertEquals(listOf(DbRole.USER, DbRole.ASSISTANT), msgs.map { it.role })
        val card = msgs[1]
        assertEquals(FoodLogStatus.PENDING, card.foodLogStatus)
        val decoded = AiFoodLogJson.decode(card.foodLogJson)!!
        assertEquals(2, decoded.log.items.size)
        assertEquals(TEST_TODAY, decoded.timing.eatenAt.toLocalDate())
    }

    @Test fun `chat hỏi thì trả lời chữ`() = runTestWithScope {
        val (repo, _) = chat("g")
        repo.send(TEST_TODAY, "tối nay còn ăn gì được?", null, null)
        val ai = repo.observe(TEST_TODAY).first().last()
        assertEquals(DbRole.ASSISTANT, ai.role)
        assertNull(ai.foodLogJson)
        assertTrue(ai.text!!.contains("920"))
    }

    @Test fun `tối qua ăn mì tôm - cả cặp tin chuyển sang luồng hôm qua, bữa Tối`() = runTestWithScope {
        val (repo, _) = chat("h")
        val r = repo.send(TEST_TODAY, "tối qua ăn thêm 1 gói mì tôm", null, null) as ChatSendResult.Sent
        val yesterday = TEST_TODAY.minusDays(1)
        assertEquals(yesterday, r.movedTo)
        assertTrue(repo.observe(TEST_TODAY).first().isEmpty())
        val msgs = repo.observe(yesterday).first()
        assertEquals(2, msgs.size)
        val d = AiFoodLogJson.decode(msgs[1].foodLogJson)!!
        assertEquals(yesterday, d.timing.eatenAt.toLocalDate())
        assertEquals(MealType.DINNER, d.timing.mealType)
        assertEquals(LocalTime.of(19, 45), d.timing.eatenAt.toLocalTime()) // giữa khung Tối (G4-7)
    }

    @Test fun `lỗi thì giữ tin người dùng chưa trả lời, thử lại không nhân đôi`() = runTestWithScope {
        val (repo, fake) = chat("i")
        fake.nextError = AiErrorType.NO_NETWORK
        val r = repo.send(TEST_TODAY, "tối ăn lẩu thái 4 người", null, null) as ChatSendResult.Failed
        assertEquals(AiErrorType.NO_NETWORK, r.type)
        val msgs = repo.observe(TEST_TODAY).first()
        assertEquals(1, msgs.size)
        assertEquals(r.userMessageId, msgs.single().id)
        assertEquals(r.userMessageId, ChatRepository.unansweredId(msgs))

        assertTrue(repo.retry(r.userMessageId, null) is ChatSendResult.Sent)
        val after = repo.observe(TEST_TODAY).first()
        assertEquals(listOf(DbRole.USER, DbRole.ASSISTANT), after.map { it.role })
        assertNull(ChatRepository.unansweredId(after))
    }

    @Test fun `lịch sử gửi kèm lượt trước trong ngày`() = runTestWithScope {
        val (repo, fake) = chat("j")
        repo.send(TEST_TODAY, "sáng ăn bánh mì", null, "/t/x.jpg")
        repo.send(TEST_TODAY, "còn bao nhiêu?", null, null)
        val h = fake.requests.last().history
        assertEquals(2, h.size)
        assertTrue(h[0].hadImage)
        assertEquals(listOf("Bánh mì trứng", "Cà phê sữa đá"), h[0].loggedItemNames)
        assertEquals(ChatRole.ASSISTANT, h[1].role)
        assertEquals(listOf("Bánh mì trứng", "Cà phê sữa đá"), h[1].loggedItemNames)
    }

    @Test fun `đánh dấu đã lưu, bỏ qua rồi hoàn tác`() = runTestWithScope {
        val (repo, _) = chat("k")
        repo.send(TEST_TODAY, "trưa ăn bánh mì", null, null)
        val card = repo.observe(TEST_TODAY).first().last()

        repo.discard(card.id).getOrThrow()
        assertEquals(FoodLogStatus.DISCARDED, repo.observe(TEST_TODAY).first().last().foodLogStatus)
        repo.undoDiscard(card.id).getOrThrow()
        assertEquals(FoodLogStatus.PENDING, repo.observe(TEST_TODAY).first().last().foodLogStatus)

        val at = LocalDateTime.of(TEST_TODAY, LocalTime.of(12, 0))
        repo.markSaved(card.id, at, MealType.LUNCH, mealManual = true).getOrThrow()
        val saved = repo.observe(TEST_TODAY).first().last()
        assertEquals(FoodLogStatus.SAVED, saved.foodLogStatus)
        val d = AiFoodLogJson.decode(saved.foodLogJson)!!
        assertEquals(MealType.LUNCH, d.timing.mealType)
        assertEquals(at, d.timing.eatenAt)
        assertTrue(d.mealManual)
    }

    // --- Thống kê (F16) ---

    @Test fun `thống kê dùng AI từ một mốc thời gian`() = runTestWithScope {
        val dao = db.aiCallLogDao()
        fun log(at: Long, ok: Boolean, inT: Int?, outT: Int?, ms: Long) =
            AiCallLogEntity(mode = AiMode.CHAT, provider = "neuralwatt", model = "m", inputTokens = inT, outputTokens = outT, durationMs = ms, success = ok, createdAt = at)
        dao.insert(log(50, true, 1000, 100, 9000)) // trước mốc
        dao.insert(log(100, true, 1500, 250, 4000))
        dao.insert(log(200, false, null, null, 2000))
        val s = dao.usageSince(100)
        assertNotNull(s)
        assertEquals(2, s.calls)
        assertEquals(1, s.failures)
        assertEquals(1500L, s.inputTokens)
        assertEquals(250L, s.outputTokens)
        assertEquals(3000L, s.avgDurationMs)
    }
}
