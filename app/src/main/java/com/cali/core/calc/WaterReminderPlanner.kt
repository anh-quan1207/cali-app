package com.cali.core.calc

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/** Đầu vào tính lịch nhắc nước. Thời điểm là [Instant]; giờ khung hiểu theo múi giờ truyền vào. */
data class WaterReminderInput(
    val enabled: Boolean,
    /** Có quyền POST_NOTIFICATIONS và kênh chưa bị tắt. */
    val permitted: Boolean,
    val start: LocalTime,
    val end: LocalTime,
    val intervalMin: Int,
    val goalMl: Int,
    /** Tổng nước của ngày lịch hiện tại. */
    val totalTodayMl: Int,
    val lastDrinkAt: Instant?,
    val lastReminderAt: Instant?,
    /** "Để sau" hẹn tới lúc này (06 §4: +30 phút). */
    val snoozeUntil: Instant?,
)

/**
 * Lịch nhắc uống nước (06 §4), thuần Kotlin.
 * next = max(lần uống gần nhất, lần nhắc trước) + chu kỳ, kẹp trong khung giờ;
 * ngoài khung → đầu khung kế tiếp; đủ mục tiêu → đầu khung của ngày mai.
 * Khung có thể qua nửa đêm (start > end). Không bao giờ trả thời điểm đã qua.
 */
object WaterReminderPlanner {

    const val SNOOZE_MIN = 30L
    /** Lần hẹn đã lỡ (máy tắt, Doze) → nhắc sau 1 phút thay vì im tới lần sau. */
    private const val CATCH_UP_MIN = 1L
    /** Chuông reo sớm hơn hạn vài phút vẫn coi là đúng hạn (setAndAllowWhileIdle không chính xác). */
    private const val EARLY_TOLERANCE_MIN = 5L

    private class Window(val start: LocalDateTime, val end: LocalDateTime) {
        operator fun contains(t: LocalDateTime) = !t.isBefore(start) && !t.isAfter(end)
    }

    private fun windowOf(day: LocalDate, s: LocalTime, e: LocalTime): Window {
        val start = day.atTime(s)
        val end = if (e.isAfter(s)) day.atTime(e) else day.plusDays(1).atTime(e)
        return Window(start, end)
    }

    /** Khung đang chứa [t] (xét cả khung bắt đầu hôm trước khi qua nửa đêm). */
    private fun windowContaining(t: LocalDateTime, s: LocalTime, e: LocalTime): Window? =
        listOf(t.toLocalDate().minusDays(1), t.toLocalDate())
            .map { windowOf(it, s, e) }
            .firstOrNull { t in it }

    /** Đầu khung sớm nhất sau [t] (bắt đầu đúng [t] cũng tính), chỉ xét ngày ≥ [minDay]. */
    private fun nextWindowStart(t: LocalDateTime, s: LocalTime, minDay: LocalDate): LocalDateTime {
        var d = maxOf(t.toLocalDate(), minDay)
        while (d.atTime(s).isBefore(t)) d = d.plusDays(1)
        return d.atTime(s)
    }

    fun next(now: Instant, zone: ZoneId, input: WaterReminderInput): Instant? {
        if (!input.enabled || !input.permitted) return null
        val n = now.atZone(zone).toLocalDateTime()
        val today = n.toLocalDate()
        val (s, e) = input.start to input.end
        fun out(t: LocalDateTime) = t.atZone(zone).toInstant()

        if (input.totalTodayMl >= input.goalMl) return out(nextWindowStart(n, s, today.plusDays(1)))

        // Đang trong khung → khung này; ngoài khung → khung kế tiếp (vẫn xét lần uống ngay trước khung).
        val window = windowContaining(n, s, e) ?: windowOf(nextWindowStart(n, s, today).toLocalDate(), s, e)

        val snooze = input.snoozeUntil?.atZone(zone)?.toLocalDateTime()?.takeIf { it.isAfter(n) }
        val interval = Duration.ofMinutes(input.intervalMin.toLong())
        // Lần uống / nhắc trước đầu khung một chu kỳ vẫn có hiệu lực (uống 06:50 → 08:20, không phải 07:00).
        val horizon = window.start.minus(interval)
        val base = listOfNotNull(input.lastDrinkAt, input.lastReminderAt)
            .map { it.atZone(zone).toLocalDateTime() }
            .filter { !it.isBefore(horizon) }
            .maxOrNull()

        val byInterval = base?.plus(interval)?.let { maxOf(it, window.start) } ?: window.start
        // "Để sau" thắng chu kỳ, trừ khi người dùng uống sau khi bấm (lịch lùi theo lần uống).
        val drankAfterSnooze = snooze != null && input.lastDrinkAt != null &&
            input.lastReminderAt != null && input.lastDrinkAt.isAfter(input.lastReminderAt)
        var candidate = if (snooze != null && !drankAfterSnooze) snooze else byInterval

        if (!candidate.isAfter(n)) candidate = n.plusMinutes(CATCH_UP_MIN)
        if (candidate.isAfter(window.end)) return out(nextWindowStart(window.end, s, today))
        return out(candidate)
    }

    /** Kiểm tra lại lúc chuông reo (WaterAlarmReceiver): có nên hiện thông báo không. */
    fun shouldNotify(now: Instant, zone: ZoneId, input: WaterReminderInput): Boolean {
        if (!input.enabled || !input.permitted) return false
        if (input.totalTodayMl >= input.goalMl) return false
        val n = now.atZone(zone).toLocalDateTime()
        if (windowContaining(n, input.start, input.end) == null) return false
        val early = Duration.ofMinutes(EARLY_TOLERANCE_MIN)
        val drink = input.lastDrinkAt
        if (drink != null && now.isBefore(drink.plus(Duration.ofMinutes(input.intervalMin.toLong())).minus(early))) return false
        val snooze = input.snoozeUntil
        if (snooze != null && now.isBefore(snooze.minus(early))) return false
        return true
    }
}
