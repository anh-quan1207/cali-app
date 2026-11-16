package com.cali.core.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * 06 §4: next = max(lần uống gần nhất, lần nhắc trước) + chu kỳ, kẹp trong khung giờ;
 * ngoài khung → đầu khung kế tiếp; đã đủ mục tiêu → đầu khung ngày mai. "Để sau" = +30 phút.
 * Ví dụ chính lấy từ mockup s15 f1: khung 07:00–22:00, 90 phút, uống 12:35, nhắc 10:10 → 14:05.
 */
class WaterReminderPlannerTest {

    private val hcm = ZoneId.of("Asia/Ho_Chi_Minh")

    private fun at(d: Int, h: Int, m: Int, zone: ZoneId = hcm): Instant =
        LocalDateTime.of(2026, 11, d, h, m).atZone(zone).toInstant()

    private fun local(i: Instant?, zone: ZoneId = hcm): LocalDateTime? = i?.atZone(zone)?.toLocalDateTime()

    private fun input(
        enabled: Boolean = true,
        permitted: Boolean = true,
        start: LocalTime = LocalTime.of(7, 0),
        end: LocalTime = LocalTime.of(22, 0),
        interval: Int = 90,
        goal: Int = 2600,
        total: Int = 1250,
        lastDrink: Instant? = null,
        lastReminder: Instant? = null,
        snoozeUntil: Instant? = null,
    ) = WaterReminderInput(enabled, permitted, start, end, interval, goal, total, lastDrink, lastReminder, snoozeUntil)

    private fun next(now: Instant, i: WaterReminderInput, zone: ZoneId = hcm) =
        local(WaterReminderPlanner.next(now, zone, i), zone)

    @Test fun `trong khung - vi du mockup max(uong 12h35, nhac 10h10) + 90 = 14h05`() {
        val r = next(at(13, 12, 40), input(lastDrink = at(13, 12, 35), lastReminder = at(13, 10, 10)))
        assertEquals(LocalDateTime.of(2026, 11, 13, 14, 5), r)
    }

    @Test fun `lan nhac gan hon lan uong thi tinh tu lan nhac`() {
        val r = next(at(13, 11, 0), input(lastDrink = at(13, 8, 40), lastReminder = at(13, 10, 10)))
        assertEquals(LocalDateTime.of(2026, 11, 13, 11, 40), r)
    }

    @Test fun `vua uong gan day thi lui lich`() {
        // Đã hẹn 14:05, uống lúc 13:50 → lùi tới 15:20.
        val r = next(at(13, 13, 50), input(lastDrink = at(13, 13, 50), lastReminder = at(13, 10, 10)))
        assertEquals(LocalDateTime.of(2026, 11, 13, 15, 20), r)
    }

    @Test fun `truoc khung gio thi dau khung hom nay`() {
        val r = next(at(13, 5, 30), input(total = 0))
        assertEquals(LocalDateTime.of(2026, 11, 13, 7, 0), r)
    }

    @Test fun `uong truoc khung gio van tinh chu ky - 06h50 + 90 = 08h20`() {
        val r = next(at(13, 6, 55), input(total = 250, lastDrink = at(13, 6, 50)))
        assertEquals(LocalDateTime.of(2026, 11, 13, 8, 20), r)
    }

    @Test fun `ngoai khung gio buoi toi thi dau khung ngay mai`() {
        val r = next(at(13, 22, 30), input(lastDrink = at(13, 21, 0)))
        assertEquals(LocalDateTime.of(2026, 11, 14, 7, 0), r)
    }

    @Test fun `lan ke vuot cuoi khung thi dau khung ngay mai`() {
        // 21:10 + 90 = 22:40 > 22:00.
        val r = next(at(13, 21, 15), input(lastDrink = at(13, 21, 10)))
        assertEquals(LocalDateTime.of(2026, 11, 14, 7, 0), r)
    }

    @Test fun `dung cuoi khung van nhac`() {
        val r = next(at(13, 20, 31), input(lastDrink = at(13, 20, 30)))
        assertEquals(LocalDateTime.of(2026, 11, 13, 22, 0), r)
    }

    @Test fun `da du muc tieu thi dau khung ngay mai - mockup f3`() {
        val r = next(at(13, 21, 30), input(total = 2850, lastDrink = at(13, 21, 10)))
        assertEquals(LocalDateTime.of(2026, 11, 14, 7, 0), r)
    }

    @Test fun `du muc tieu tu sang som thi bo ca khung hom nay`() {
        val r = next(at(13, 5, 0), input(total = 2600, lastDrink = at(13, 4, 0)))
        assertEquals(LocalDateTime.of(2026, 11, 14, 7, 0), r)
    }

    @Test fun `de sau cong 30 phut`() {
        val now = at(13, 14, 5)
        val r = next(now, input(lastDrink = at(13, 12, 35), lastReminder = now, snoozeUntil = at(13, 14, 35)))
        assertEquals(LocalDateTime.of(2026, 11, 13, 14, 35), r)
    }

    @Test fun `de sau vuot cuoi khung thi dau khung ngay mai`() {
        val r = next(at(13, 21, 50), input(lastReminder = at(13, 21, 50), snoozeUntil = at(13, 22, 20)))
        assertEquals(LocalDateTime.of(2026, 11, 14, 7, 0), r)
    }

    @Test fun `de sau da het han thi bo qua`() {
        val r = next(at(13, 15, 0), input(lastDrink = at(13, 14, 50), snoozeUntil = at(13, 14, 35)))
        assertEquals(LocalDateTime.of(2026, 11, 13, 16, 20), r)
    }

    @Test fun `uong sau khi bam de sau thi lui theo lan uong`() {
        // Bấm Để sau 14:05 (→ 14:35), uống 14:20 → 15:50.
        val r = next(at(13, 14, 20), input(lastDrink = at(13, 14, 20), lastReminder = at(13, 14, 5), snoozeUntil = at(13, 14, 35)))
        assertEquals(LocalDateTime.of(2026, 11, 13, 15, 50), r)
    }

    @Test fun `chua uong chua nhac trong khung thi nhac ngay`() {
        // Vừa bật nhắc lúc 12:00 mà cả ngày chưa uống: không để lịch rơi vào quá khứ.
        val r = next(at(13, 12, 0), input(total = 0))
        assertEquals(LocalDateTime.of(2026, 11, 13, 12, 1), r)
    }

    @Test fun `khoi dong lai may lo lan nhac thi nhac ngay trong khung`() {
        // Máy tắt từ 10:00 tới 13:00: lần hẹn 11:40 đã lỡ.
        val r = next(at(13, 13, 0), input(lastReminder = at(13, 10, 10), lastDrink = at(13, 8, 40)))
        assertEquals(LocalDateTime.of(2026, 11, 13, 13, 1), r)
    }

    @Test fun `khoi dong lai may sau cuoi khung thi dau khung ngay mai`() {
        val r = next(at(13, 23, 30), input(lastReminder = at(13, 20, 0)))
        assertEquals(LocalDateTime.of(2026, 11, 14, 7, 0), r)
    }

    @Test fun `khung qua nua dem - trong khung sau 0h`() {
        val r = next(
            at(14, 0, 30),
            input(start = LocalTime.of(22, 0), end = LocalTime.of(2, 0), interval = 60, total = 0, lastReminder = at(13, 23, 45)),
        )
        assertEquals(LocalDateTime.of(2026, 11, 14, 0, 45), r)
    }

    @Test fun `khung qua nua dem - vuot cuoi khung thi 22h dem sau`() {
        val r = next(
            at(14, 1, 30),
            input(start = LocalTime.of(22, 0), end = LocalTime.of(2, 0), interval = 60, total = 0, lastReminder = at(14, 1, 15)),
        )
        assertEquals(LocalDateTime.of(2026, 11, 14, 22, 0), r)
    }

    @Test fun `khung qua nua dem - truoc khung thi 22h cung ngay`() {
        val r = next(at(14, 12, 0), input(start = LocalTime.of(22, 0), end = LocalTime.of(2, 0), total = 0))
        assertEquals(LocalDateTime.of(2026, 11, 14, 22, 0), r)
    }

    @Test fun `doi mui gio tinh lai theo gio dia phuong moi`() {
        // Cùng thời điểm tuyệt đối 05:30 ở HCM = 07:30 ở Tokyo: Tokyo đã trong khung.
        val tokyo = ZoneId.of("Asia/Tokyo")
        val now = at(13, 5, 30)
        assertEquals(LocalDateTime.of(2026, 11, 13, 7, 0), next(now, input(total = 0), hcm))
        assertEquals(LocalDateTime.of(2026, 11, 13, 7, 31), next(now, input(total = 0), tokyo))
    }

    @Test fun `doi mui gio sang tay thi lich ve dau khung theo gio moi`() {
        // 07:30 HCM = 01:30 London (GMT): ngoài khung → 07:00 London.
        val london = ZoneId.of("Europe/London")
        val r = next(at(13, 7, 30), input(total = 0, lastReminder = at(13, 7, 0)), london)
        assertEquals(LocalDateTime.of(2026, 11, 13, 7, 0), r)
    }

    @Test fun `tat nhac thi khong dat lich`() {
        assertNull(WaterReminderPlanner.next(at(13, 12, 0), hcm, input(enabled = false)))
    }

    @Test fun `thieu quyen thong bao thi khong dat lich`() {
        assertNull(WaterReminderPlanner.next(at(13, 12, 0), hcm, input(permitted = false)))
    }

    @Test fun `trong khung - kiem tra lai luc chuong reo`() {
        assertEquals(true, WaterReminderPlanner.shouldNotify(at(13, 14, 5), hcm, input(lastDrink = at(13, 12, 35))))
        // Đã đủ mục tiêu thì chuông reo cũng không hiện.
        assertEquals(false, WaterReminderPlanner.shouldNotify(at(13, 14, 5), hcm, input(total = 2600)))
        // Ngoài khung giờ không hiện.
        assertEquals(false, WaterReminderPlanner.shouldNotify(at(13, 22, 30), hcm, input()))
        // Vừa uống trong 5 phút thì không nhắc (alarm cũ chưa kịp huỷ).
        assertEquals(false, WaterReminderPlanner.shouldNotify(at(13, 14, 5), hcm, input(lastDrink = at(13, 14, 2))))
    }
}
