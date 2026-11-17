package com.cali.core.calc

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

/** 08 §1, §6: lịch thông báo tổng kết tuần, thẻ S10, tuần mặc định của S35. Thuần Kotlin. */
object WeeklySummaryPlanner {
    fun weekStart(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /** Thứ Hai gần nhất lúc [time], sau [now] (đúng giờ thì đã qua → tuần sau). */
    fun nextFire(now: LocalDateTime, time: LocalTime): LocalDateTime {
        val thisMonday = weekStart(now.toLocalDate()).atTime(time)
        return if (thisMonday.isAfter(now)) thisMonday else thisMonday.plusWeeks(1)
    }

    /** Thông báo sáng Thứ Hai nói về tuần vừa kết thúc. */
    fun reviewedWeek(monday: LocalDate): LocalDate = weekStart(monday).minusWeeks(1)

    fun showLastWeekCard(today: LocalDate): Boolean =
        today.dayOfWeek == DayOfWeek.MONDAY || today.dayOfWeek == DayOfWeek.TUESDAY

    fun defaultWeek(today: LocalDate): LocalDate =
        if (showLastWeekCard(today)) weekStart(today).minusWeeks(1) else weekStart(today)
}
