package com.cali.core.calc

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

data class WeightPoint(val date: LocalDate, val kg: Double)

/** TB một tuần (Thứ Hai → Chủ Nhật). */
data class WeekAvg(val weekStart: LocalDate, val avgKg: Double, val count: Int)

data class WeightHeadline(val week: WeekAvg, val previous: WeekAvg?, val changeKg: Double?)

data class WeightProgress(
    val startKg: Double,
    val latestKg: Double,
    /** Lần cân đầu − lần cân gần nhất (dương = đã giảm). */
    val changedKg: Double,
    val remainingKg: Double,
    val eta: LocalDate?,
    val etaWeeks: Int?,
)

data class WeightRange(
    val points: List<WeightPoint>,
    val weeks: List<WeekAvg>,
    val firstAvgKg: Double,
    val lastAvgKg: Double,
    val changeKg: Double,
    val kgPerWeek: Double?,
)

/** S31 (00-direction: số lớn là TB tuần, tiến độ theo lần cân gần nhất, ETA từ hôm nay). Thuần Kotlin. */
object WeightTrendCalculator {
    /** Tuần này chỉ thành số TB khi có ≥ 2 lần cân (00-direction S31). */
    const val MIN_CURRENT_WEEK = 2

    fun weekStart(date: LocalDate): LocalDate = date.minusDays((date.dayOfWeek.value - 1).toLong())

    /** TB từng tuần có lần cân, cũ → mới; tuần đang diễn ra chỉ có khi đủ [MIN_CURRENT_WEEK] lần. */
    fun weeklyAverages(entries: List<WeightPoint>, today: LocalDate): List<WeekAvg> {
        val thisWeek = weekStart(today)
        return entries.filter { !it.date.isAfter(today) }
            .groupBy { weekStart(it.date) }
            .map { (start, pts) -> WeekAvg(start, round3(pts.sumOf { it.kg } / pts.size), pts.size) }
            .filter { it.weekStart != thisWeek || it.count >= MIN_CURRENT_WEEK }
            .sortedBy { it.weekStart }
    }

    fun headline(entries: List<WeightPoint>, today: LocalDate): WeightHeadline? {
        val weeks = weeklyAverages(entries, today)
        val last = weeks.lastOrNull() ?: return null
        val prev = weeks.getOrNull(weeks.size - 2)
        return WeightHeadline(last, prev, prev?.let { round3(last.avgKg - it.avgKg) })
    }

    /** Tiến độ theo lần cân gần nhất; ETA đếm từ hôm nay theo tốc độ kế hoạch (02 §9). */
    fun progress(entries: List<WeightPoint>, targetKg: Double?, weeklyRateKg: Double, today: LocalDate): WeightProgress? {
        val sorted = entries.filter { !it.date.isAfter(today) }.sortedBy { it.date }
        val first = sorted.firstOrNull() ?: return null
        val latest = sorted.last()
        val days = NutritionCalculator.etaDays(latest.kg, targetKg, weeklyRateKg)
        return WeightProgress(
            startKg = first.kg,
            latestKg = latest.kg,
            changedKg = round3(first.kg - latest.kg),
            remainingKg = targetKg?.let { round3(abs(latest.kg - it)) } ?: 0.0,
            eta = days?.let { today.plusDays(it.toLong()) },
            etaWeeks = days?.let { Math.round(it / 7.0).toInt() },
        )
    }

    /**
     * Khoảng [days] ngày tới hôm nay (null = tất cả). Đường TB tuần gồm mọi tuần có lần cân trong khoảng
     * (TB tính trên cả tuần đó); tốc độ = chênh TB tuần đầu–cuối / số tuần giữa hai tuần.
     */
    fun range(entries: List<WeightPoint>, today: LocalDate, days: Int?): WeightRange? {
        val from = days?.let { today.minusDays((it - 1).toLong()) }
        val points = entries.filter { !it.date.isAfter(today) && (from == null || !it.date.isBefore(from)) }.sortedBy { it.date }
        if (points.isEmpty()) return null
        val inRange = points.map { weekStart(it.date) }.toSet()
        val weeks = weeklyAverages(entries, today).filter { it.weekStart in inRange }
        if (weeks.isEmpty()) return null
        val first = weeks.first()
        val last = weeks.last()
        val spanWeeks = ChronoUnit.WEEKS.between(first.weekStart, last.weekStart)
        return WeightRange(
            points = points,
            weeks = weeks,
            firstAvgKg = first.avgKg,
            lastAvgKg = last.avgKg,
            changeKg = round3(last.avgKg - first.avgKg),
            kgPerWeek = if (spanWeeks > 0) (last.avgKg - first.avgKg) / spanWeeks else null,
        )
    }

    private fun round3(x: Double) = Math.round(x * 1000) / 1000.0
}
