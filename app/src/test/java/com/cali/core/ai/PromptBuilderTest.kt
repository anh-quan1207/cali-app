package com.cali.core.ai

import com.cali.core.ai.PromptPart.Image
import com.cali.core.ai.PromptPart.Text
import com.cali.core.calc.model.MealType
import com.cali.core.calc.model.Sex
import com.cali.core.data.db.entity.AiMode
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

    private val builder = PromptBuilder()

    /** 03 §4 ví dụ: 13/11/2026 (Thứ Sáu thật), 12:35, đã ăn 820. */
    private val ctx = UserContext(
        now = LocalDateTime.of(2026, 11, 13, 12, 35),
        sex = Sex.MALE,
        age = 30,
        heightCm = 170.0,
        weightKg = 74.2,
        targetWeightKg = 68.0,
        goalKcal = 1740,
        goalProteinG = 122,
        goalCarbG = 196,
        goalFatG = 52,
        eatenKcal = 820,
        eatenProteinG = 45,
        eatenCarbG = 98,
        eatenFatG = 30,
        todayEntries = listOf(
            ContextEntry(MealType.BREAKFAST, LocalTime.of(7, 15), "Cà phê sữa đá", 140),
            ContextEntry(MealType.BREAKFAST, LocalTime.of(7, 10), "Bánh mì trứng", 380),
            ContextEntry(MealType.SNACK, LocalTime.of(10, 0), "Chuối", 100),
            ContextEntry(MealType.BREAKFAST, LocalTime.of(9, 30), "Sữa chua", 200),
        ),
        personalNotes = "Cơm nhà nấu ít dầu.",
        frequentFoods = listOf("Cơm nhà (1 bát 180 g)", "Cà phê sữa đá ít đường (110 kcal)"),
    )

    @Test fun `khối user_context đúng định dạng 03 §4`() {
        val expected = """
            <user_context>
            Ngày: 2026-11-13 (Thứ Sáu), giờ hiện tại: 12:35
            Hồ sơ: Nam, 30 tuổi, 170 cm, 74.2 kg, mục tiêu giảm còn 68 kg
            Mục tiêu ngày: 1740 kcal | Đạm 122 g | Tinh bột 196 g | Béo 52 g
            Đã ăn hôm nay: 820 kcal | Đạm 45 g | Tinh bột 98 g | Béo 30 g
            Còn lại: 920 kcal
            Các bữa đã ghi: Sáng: 07:10 Bánh mì trứng (380 kcal), 07:15 Cà phê sữa đá (140 kcal), 09:30 Sữa chua (200 kcal); Phụ: 10:00 Chuối (100 kcal)
            Thời điểm của lần ghi này: 12:35 → bữa Trưa (tự gán theo giờ)
            Ghi chú cá nhân: Cơm nhà nấu ít dầu.
            Món hay ăn: Cơm nhà (1 bát 180 g); Cà phê sữa đá ít đường (110 kcal)
            </user_context>
        """.trimIndent()
        assertEquals(expected, builder.userContextBlock(ctx))
    }

    @Test fun `nữ, tăng cân, chưa ghi món, không ghi chú`() {
        val block = builder.userContextBlock(
            ctx.copy(
                sex = Sex.FEMALE, weightKg = 50.0, targetWeightKg = 55.5, todayEntries = emptyList(),
                eatenKcal = 0, eatenProteinG = 0, eatenCarbG = 0, eatenFatG = 0,
                personalNotes = " ", frequentFoods = emptyList(),
            ),
        )
        assertTrue(block, "Hồ sơ: Nữ, 30 tuổi, 170 cm, 50 kg, mục tiêu tăng lên 55.5 kg" in block)
        assertTrue(block, "Các bữa đã ghi: chưa có" in block)
        assertTrue(block, "Còn lại: 1740 kcal" in block)
        assertFalse(block, "Ghi chú cá nhân" in block)
        assertFalse(block, "Món hay ăn" in block)
    }

    @Test fun `giữ cân và đã vượt mục tiêu`() {
        val block = builder.userContextBlock(ctx.copy(targetWeightKg = null, eatenKcal = 1860))
        assertTrue(block, "74.2 kg, mục tiêu giữ cân" in block)
        assertTrue(block, "Còn lại: 0 kcal (đã vượt 120 kcal)" in block)
    }

    @Test fun `giờ của lần ghi lấy từ logTime nếu có`() {
        val block = builder.userContextBlock(ctx.copy(logTime = LocalTime.of(19, 5)))
        assertTrue(block, "Thời điểm của lần ghi này: 19:05 → bữa Tối (tự gán theo giờ)" in block)
    }

    @Test fun `thứ trong tuần tiếng Việt`() {
        val block = builder.userContextBlock(ctx.copy(now = LocalDateTime.of(2026, 11, 15, 8, 0)))
        assertTrue(block, "Ngày: 2026-11-15 (Chủ Nhật), giờ hiện tại: 08:00" in block)
        val monday = builder.userContextBlock(ctx.copy(now = LocalDateTime.of(2026, 11, 16, 8, 0)))
        assertTrue(monday, "(Thứ Hai)" in monday)
    }

    @Test fun `prompt tĩnh tách khỏi ngữ cảnh động`() {
        val p = builder.build(chat(userText = "Trưa nay ăn gì?"))
        assertTrue(p.staticSystem.startsWith("PROMPT GỐC"))
        assertFalse("<user_context>" in p.staticSystem)
        assertEquals(builder.userContextBlock(ctx), p.userContext)
    }

    @Test fun `PHOTO_LOG thêm câu bắt buộc gọi tool, CHAT thì không`() {
        val photo = builder.build(photo())
        val chat = builder.build(chat(userText = "hi"))
        assertTrue(photo.staticSystem.contains("record_food_items"))
        assertFalse(chat.staticSystem.contains("record_food_items"))
        assertTrue(photo.staticSystem.startsWith("PROMPT GỐC"))
    }

    @Test fun `PHOTO_LOG ảnh đứng trước ghi chú`() {
        val jpeg = byteArrayOf(1, 2, 3)
        val p = builder.build(photo(jpeg = jpeg, note = "ăn một nửa"))
        val msg = p.messages.single()
        assertEquals(ChatRole.USER, msg.role)
        assertArrayEquals(jpeg, (msg.parts[0] as Image).jpeg)
        assertEquals("ăn một nửa", (msg.parts[1] as Text).text)
    }

    @Test fun `PHOTO_LOG không ghi chú chỉ có ảnh`() {
        val p = builder.build(photo(note = null))
        assertEquals(1, p.messages.single().parts.size)
    }

    @Test fun `cắt còn 10 tin nhắn gần nhất`() {
        val history = (1..15).map {
            ChatTurn(if (it % 2 == 1) ChatRole.USER else ChatRole.ASSISTANT, text = "m$it")
        }
        val p = builder.build(chat(history = history, userText = "mới"))
        // 15 tin → giữ m6..m15; m6 là của AI nên bị bỏ (tin đầu phải là của người dùng).
        val texts = p.messages.flatMap { m -> m.parts.map { (it as Text).text } }
        assertEquals(listOf("m7", "m8", "m9", "m10", "m11", "m12", "m13", "m14", "m15", "mới"), texts)
        assertEquals(ChatRole.USER, p.messages.first().role)
        assertEquals(ChatRole.USER, p.messages.last().role)
        // m15 (người dùng) gộp với lượt hiện tại → 9 tin nhắn.
        assertEquals(9, p.messages.size)
    }

    @Test fun `ảnh cũ thay bằng mô tả, không gửi lại ảnh`() {
        val history = listOf(
            ChatTurn(ChatRole.USER, text = "trưa nay", hadImage = true, loggedItemNames = listOf("Cơm tấm", "Trứng ốp la")),
            ChatTurn(ChatRole.ASSISTANT, text = "Mình đã ước lượng.", loggedItemNames = listOf("Cơm tấm", "Trứng ốp la")),
            ChatTurn(ChatRole.USER, hadImage = true),
            ChatTurn(ChatRole.ASSISTANT, text = "Ảnh không rõ."),
        )
        val p = builder.build(chat(history = history, userText = "còn bao nhiêu calo?"))
        assertTrue(p.messages.none { m -> m.parts.any { it is Image } })
        assertEquals("[ảnh: Cơm tấm, Trứng ốp la]\ntrưa nay", (p.messages[0].parts.single() as Text).text)
        assertEquals("Mình đã ước lượng.\n[đã ghi: Cơm tấm, Trứng ốp la]", (p.messages[1].parts.single() as Text).text)
        assertEquals("[ảnh]", (p.messages[2].parts.single() as Text).text)
    }

    @Test fun `ảnh của lượt hiện tại vẫn được gửi trong CHAT`() {
        val p = builder.build(chat(userText = "cái này bao nhiêu calo", jpeg = byteArrayOf(9)))
        val parts = p.messages.single().parts
        assertTrue(parts[0] is Image)
        assertEquals("cái này bao nhiêu calo", (parts[1] as Text).text)
    }

    @Test fun `gộp các tin liền nhau cùng vai, bỏ tin rỗng`() {
        val history = listOf(
            ChatTurn(ChatRole.ASSISTANT, text = "Chào bạn"),
            ChatTurn(ChatRole.USER, text = "a"),
            ChatTurn(ChatRole.USER, text = " "),
            ChatTurn(ChatRole.USER, text = "b"),
            ChatTurn(ChatRole.ASSISTANT, text = "c"),
        )
        val p = builder.build(chat(history = history, userText = "d"))
        assertEquals(listOf(ChatRole.USER, ChatRole.ASSISTANT, ChatRole.USER), p.messages.map { it.role })
        assertEquals(listOf("a", "b"), p.messages[0].parts.map { (it as Text).text })
        assertEquals("d", (p.messages[2].parts.single() as Text).text)
    }

    @Test fun `tin cuối lịch sử là của người dùng thì gộp với lượt hiện tại`() {
        val p = builder.build(chat(history = listOf(ChatTurn(ChatRole.USER, text = "a")), userText = "b"))
        assertEquals(1, p.messages.size)
        assertEquals(listOf("a", "b"), p.messages[0].parts.map { (it as Text).text })
    }

    private fun chat(history: List<ChatTurn> = emptyList(), userText: String?, jpeg: ByteArray? = null) =
        AiRequest(AiMode.CHAT, "PROMPT GỐC", ctx, history, userText, jpeg)

    private fun photo(jpeg: ByteArray = byteArrayOf(1), note: String? = null) =
        AiRequest(AiMode.PHOTO_LOG, "PROMPT GỐC", ctx, emptyList(), note, jpeg)
}
