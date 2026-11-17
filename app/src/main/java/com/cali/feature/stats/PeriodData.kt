package com.cali.feature.stats

import com.cali.core.calc.DayInput
import com.cali.core.calc.DayStatus
import com.cali.core.calc.SameDaysComparison
import com.cali.core.calc.WeeklyStats
import com.cali.core.calc.WeeklyStatsCalculator
import com.cali.core.data.repo.RangeData
import java.time.LocalDate
import java.time.YearMonth

/** Một kỳ (tuần hoặc tháng) đã tính xong cho S30 / S32 / S35. */
data class PeriodView(
    val start: LocalDate,
    val end: LocalDate,
    val isWeek: Boolean,
    val today: LocalDate,
    val firstUse: LocalDate?,
    val stats: WeeklyStats,
    val inputs: Map<LocalDate, DayInput>,
    /** Mục tiêu đang áp cho ngày cuối đã qua của kỳ (vạch biểu đồ, "so với 1.740"). */
    val target: DayInput?,
    val comparison: SameDaysComparison?,
    val weights: List<Double>,
    val prevWeights: List<Double>,
    /** Mọi lần cân (cho S30 / S32 hàng cân). */
    val data: RangeData,
) {
    val kcalOf: (LocalDate) -> Double = { inputs[it]?.kcal ?: 0.0 }
    val includesToday: Boolean get() = !today.isBefore(start) && !today.isAfter(end)
    val weightAvg: Double? get() = weights.takeIf { it.isNotEmpty() }?.average()
    val prevWeightAvg: Double? get() = prevWeights.takeIf { it.isNotEmpty() }?.average()
    val todayLogged: Boolean get() = includesToday && (inputs[today]?.entryCount ?: 0) > 0
    val prevStart: LocalDate get() = if (isWeek) start.minusWeeks(1) else start.minusMonths(1).withDayOfMonth(1)
}

object Periods {
    /** Khoảng dữ liệu cần tải cho tuần [weekStart] (gồm tuần trước để so). */
    fun weekRange(weekStart: LocalDate) = weekStart.minusWeeks(1) to weekStart.plusDays(6)

    fun monthRange(month: YearMonth) = month.minusMonths(1).atDay(1) to month.atEndOfMonth()

    fun week(data: RangeData, weekStart: LocalDate): PeriodView {
        val end = weekStart.plusDays(6)
        val prev = weekStart.minusWeeks(1)
        val weights = data.weights.filter { it.date in weekStart..end }.map { it.kg }
        val prevWeights = data.weights.filter { it.date in prev..prev.plusDays(6) }.map { it.kg }
        val stats = WeeklyStatsCalculator.compute(
            weekStart, data.today, data.daysIn(weekStart, end), data.foods, weights,
            prevWeights.takeIf { it.isNotEmpty() }?.average(), data.firstUseDate,
        )
        val cmp = WeeklyStatsCalculator.compareSameDays(stats, prev, 7, data.daysIn(prev, end), data.today, data.firstUseDate, 3)
        return PeriodView(weekStart, end, true, data.today, data.firstUseDate, stats, data.days, targetOf(data, weekStart, end), cmp, weights, prevWeights, data)
    }

    fun month(data: RangeData, month: YearMonth): PeriodView {
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        val prevMonth = month.minusMonths(1)
        val stats = WeeklyStatsCalculator.computeMonth(month, data.today, data.daysIn(start, end), data.foods, data.firstUseDate)
        val cmp = WeeklyStatsCalculator.compareSameDays(
            stats, prevMonth.atDay(1), prevMonth.lengthOfMonth(), data.daysIn(prevMonth.atDay(1), prevMonth.atEndOfMonth()),
            data.today, data.firstUseDate, 7,
        )
        val weights = data.weights.filter { it.date in start..end }.map { it.kg }
        val prevWeights = data.weights.filter { it.date in prevMonth.atDay(1)..prevMonth.atEndOfMonth() }.map { it.kg }
        return PeriodView(start, end, false, data.today, data.firstUseDate, stats, data.days, targetOf(data, start, end), cmp, weights, prevWeights, data)
    }

    private fun targetOf(data: RangeData, start: LocalDate, end: LocalDate): DayInput? {
        val last = minOf(end, data.today)
        return data.days[last]?.takeIf { it.targetKcal > 0 }
            ?: data.days.values.filter { it.date in start..end && it.targetKcal > 0 }.maxByOrNull { it.date }
    }

    /** Ngày đếm vào kỳ (đã qua, sau khi dùng app). */
    fun counted(v: PeriodView) = v.stats.days.filter { it.status != DayStatus.FUTURE && it.status != DayStatus.BEFORE_START }
}
