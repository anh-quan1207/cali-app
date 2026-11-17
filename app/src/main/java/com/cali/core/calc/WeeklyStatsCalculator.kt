package com.cali.core.calc

import com.cali.core.calc.model.MealType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.min

/** 08 §2. */
enum class DayStatus {
    ON_TARGET, OVER, UNDER, NOT_LOGGED,

    /** Trước ngày bắt đầu dùng app: để trống, không tính là chưa ghi (S30 f4, S32 f5). */
    BEFORE_START,

    /** Sau hôm nay. */
    FUTURE,
}

enum class WeekGrade { EXCELLENT, GOOD, NEEDS_WORK, POOR }

enum class Trend { UP, DOWN, SAME }

/** Số liệu thô của một ngày, đã gộp từ food_entry / water_entry / goal_snapshot áp dụng cho ngày đó. */
data class DayInput(
    val date: LocalDate,
    val entryCount: Int,
    val kcal: Double,
    val proteinG: Double,
    val carbG: Double,
    val fatG: Double,
    val waterMl: Int,
    val targetKcal: Int,
    val targetProteinG: Int,
    val targetCarbG: Int,
    val targetFatG: Int,
    val targetWaterMl: Int,
    val tdee: Double,
)

data class FoodContribution(val name: String, val kcal: Double, val mealType: MealType, val date: LocalDate)

data class DayResult(
    val date: LocalDate,
    val status: DayStatus,
    /** kcal ăn − mục tiêu; null nếu chưa ghi. */
    val delta: Double?,
    /** Đã ghi nhưng < 50% mục tiêu (08 §2). */
    val suspectedUnderLog: Boolean,
    val waterMet: Boolean,
    val proteinMet: Boolean,
)

data class TopFood(val name: String, val count: Int, val kcal: Double)

data class ScoreBreakdown(
    val calorie: Double,
    val protein: Double,
    val water: Double,
    val logging: Double,
) {
    /** Làm tròn tổng chưa làm tròn (08 §4); có thể lệch 1 so với tổng bốn phần đã làm tròn. */
    val total: Int get() = Math.round(calorie + protein + water + logging).toInt()
}

data class WeeklyStats(
    /** Ngày đầu kỳ (Thứ Hai với tuần, ngày 1 với tháng). */
    val weekStart: LocalDate,
    /** Số ngày ở cuối kỳ còn chưa tới (S32 "còn 3 ngày"). */
    val remainingDays: Int = 0,
    /** false khi người mới bắt đầu dùng app trong kỳ và chưa đủ 3 ngày ghi (S32 f5). */
    val averagesReady: Boolean = true,
    /** Số ngày đã qua tính cả hôm nay (7 nếu tuần đã kết thúc). */
    val elapsedDays: Int,
    val isPartial: Boolean,
    val days: List<DayResult>,
    val loggedDays: Int,
    val notLoggedDays: Int,
    val onTargetDays: Int,
    val overDays: Int,
    val underDays: Int,
    val suspectedUnderLogDays: Int,
    val sumTargetKcal: Double,
    val sumEatenKcal: Double,
    val sumDeltaKcal: Double,
    val avgKcal: Double?,
    val maxDay: DayResult?,
    val minDay: DayResult?,
    /** Σ(kcal ăn − TDEE ngày) trên các ngày đã ghi. */
    val tdeeBalanceKcal: Double,
    val expectedWeightChangeKg: Double,
    val avgProteinG: Double?,
    val avgCarbG: Double?,
    val avgFatG: Double?,
    val avgTargetProteinG: Double?,
    val proteinMetDays: Int,
    val waterTotalMl: Int,
    val waterTargetTotalMl: Int,
    val avgWaterMl: Double,
    val waterMetDays: Int,
    val waterShortfallMl: Int,
    val avgWeightKg: Double?,
    val weightChangeKg: Double?,
    val topFoods: List<TopFood>,
    val topMeal: MealType?,
    val score: ScoreBreakdown,
    val grade: WeekGrade,
)

data class WeekComparison(
    val avgKcal: Trend?,
    val onTargetDays: Trend,
    val avgWaterMl: Trend,
    val avgProteinG: Trend?,
    val score: Trend,
)

/** Same-Days Rule: kỳ trước cắt đúng số ngày đầu như kỳ này; [trends] null khi kỳ trước chưa đủ ngày ghi. */
data class SameDaysComparison(val previous: WeeklyStats, val enough: Boolean, val trends: WeekComparison?)

/** 08 §2–§4. Thuần Kotlin. */
object WeeklyStatsCalculator {
    private const val TOLERANCE = 0.10
    private const val UNDER_LOG_RATIO = 0.50
    private const val PROTEIN_MET_RATIO = 0.90
    private const val TOP_FOODS = 5
    const val MIN_DAYS_FOR_AVERAGES = 3

    fun dayStatus(entryCount: Int, kcal: Double, targetKcal: Int): DayStatus {
        if (entryCount <= 0) return DayStatus.NOT_LOGGED
        val delta = kcal - targetKcal
        val band = TOLERANCE * targetKcal
        return when {
            delta > band -> DayStatus.OVER
            delta < -band -> DayStatus.UNDER
            else -> DayStatus.ON_TARGET
        }
    }

    fun compute(
        weekStart: LocalDate,
        today: LocalDate,
        days: List<DayInput>,
        foods: List<FoodContribution> = emptyList(),
        weights: List<Double> = emptyList(),
        previousWeekAvgWeightKg: Double? = null,
        firstUseDate: LocalDate? = null,
    ): WeeklyStats {
        require(weekStart.dayOfWeek == DayOfWeek.MONDAY) { "Tuần bắt đầu Thứ Hai" }
        return computePeriod(weekStart, 7, today, days, foods, weights, previousWeekAvgWeightKg, firstUseDate)
    }

    /** Tháng dùng cùng chỉ số (S30, S32); điểm chỉ có ý nghĩa với tuần. */
    fun computeMonth(
        month: YearMonth,
        today: LocalDate,
        days: List<DayInput>,
        foods: List<FoodContribution> = emptyList(),
        firstUseDate: LocalDate? = null,
    ): WeeklyStats = computePeriod(month.atDay(1), month.lengthOfMonth(), today, days, foods, emptyList(), null, firstUseDate)

    /**
     * Same-Days Rule (00-direction, S32/S35): kỳ trước tính đúng bấy nhiêu ngày đầu như kỳ này đã qua,
     * chỉ những ngày từ lúc bắt đầu dùng app; chỉ so khi kỳ trước có >= [minLoggedDays] ngày ghi (tuần 3, tháng 7).
     */
    fun compareSameDays(
        current: WeeklyStats,
        previousStart: LocalDate,
        previousLength: Int,
        previousDays: List<DayInput>,
        today: LocalDate,
        firstUseDate: LocalDate? = null,
        minLoggedDays: Int = 3,
    ): SameDaysComparison {
        val currentEnd = current.weekStart.plusDays((current.days.size - 1).toLong())
        val offset = ChronoUnit.DAYS.between(current.weekStart, minOf(today, currentEnd)).coerceAtLeast(0)
        val prev = computePeriod(
            previousStart, previousLength, previousStart.plusDays(offset), previousDays,
            emptyList(), emptyList(), null, firstUseDate,
        )
        val enough = prev.loggedDays >= minLoggedDays
        return SameDaysComparison(prev, enough, if (enough) trends(current, prev) else null)
    }

    private fun computePeriod(
        start: LocalDate,
        length: Int,
        today: LocalDate,
        days: List<DayInput>,
        foods: List<FoodContribution>,
        weights: List<Double>,
        previousWeekAvgWeightKg: Double?,
        firstUseDate: LocalDate?,
    ): WeeklyStats {
        val end = start.plusDays((length - 1).toLong())
        val from = if (firstUseDate != null && firstUseDate.isAfter(start)) firstUseDate else start
        val to = minOf(end, today)
        val window = if (to.isBefore(from)) emptyList() else (0..ChronoUnit.DAYS.between(from, to)).map { from.plusDays(it) }
        val elapsed = window.size
        val divisor = elapsed.coerceAtLeast(1)
        val byDate = days.associateBy { it.date }
        val inputs = window.map { byDate[it] }

        // Hôm nay còn đang ghi: chưa gắn cờ nghi ghi thiếu (S30 f0).
        val results = window.mapIndexed { i, date -> dayResult(date, inputs[i], inProgress = date == today) }
        val logged = inputs.filterNotNull().filter { it.entryCount > 0 }
        val loggedResults = results.filter { it.status != DayStatus.NOT_LOGGED }

        val onTarget = results.count { it.status == DayStatus.ON_TARGET }
        val waterMet = results.count { it.waterMet }
        val consistent = loggedResults.count { !it.suspectedUnderLog }

        val avgProtein = logged.avgOrNull { it.proteinG }
        val avgTargetProtein = logged.avgOrNull { it.targetProteinG.toDouble() }
        val proteinRatio = if (avgProtein != null && avgTargetProtein != null && avgTargetProtein > 0) {
            min(1.0, avgProtein / avgTargetProtein)
        } else {
            0.0
        }
        val score = ScoreBreakdown(
            calorie = 40.0 * onTarget / divisor,
            protein = 20.0 * proteinRatio,
            water = 20.0 * waterMet / divisor,
            logging = 20.0 * consistent / divisor,
        )

        val present = inputs.filterNotNull()
        val waterTotal = present.sumOf { it.waterMl }
        // Ngày không có bản ghi nào vẫn có mục tiêu nước: lấy theo ngày gần nhất có dữ liệu.
        val fallbackWaterTarget = present.lastOrNull()?.targetWaterMl ?: 0
        val waterTargetTotal = inputs.sumOf { it?.targetWaterMl ?: fallbackWaterTarget }
        val shortfall = inputs.sumOf { d ->
            val target = d?.targetWaterMl ?: fallbackWaterTarget
            (target - (d?.waterMl ?: 0)).coerceAtLeast(0)
        }

        val tdeeBalance = logged.sumOf { it.kcal - it.tdee }
        val avgWeight = weights.takeIf { it.isNotEmpty() }?.average()

        val windowSet = window.toSet()
        val windowFoods = foods.filter { it.date in windowSet }
        val topFoods = windowFoods.groupBy { it.name }
            .map { (name, items) -> TopFood(name, items.size, items.sumOf { it.kcal }) }
            .sortedByDescending { it.kcal }
            .take(TOP_FOODS)
        val topMeal = windowFoods.groupBy { it.mealType }
            .mapValues { (_, items) -> items.groupBy { it.date }.values.map { d -> d.sumOf { it.kcal } }.average() }
            .maxByOrNull { it.value }?.key

        val sumTarget = logged.sumOf { it.targetKcal.toDouble() }
        val sumEaten = logged.sumOf { it.kcal }
        val resultByDate = results.associateBy { it.date }
        val partial = today.isBefore(end)

        return WeeklyStats(
            weekStart = start,
            remainingDays = when {
                !partial -> 0
                today.isBefore(start) -> length
                else -> ChronoUnit.DAYS.between(today, end).toInt()
            },
            averagesReady = firstUseDate == null || !firstUseDate.isAfter(start) || logged.size >= MIN_DAYS_FOR_AVERAGES,
            elapsedDays = elapsed,
            isPartial = partial,
            days = (0 until length).map { i ->
                val date = start.plusDays(i.toLong())
                resultByDate[date] ?: if (date.isBefore(from)) {
                    DayResult(date, DayStatus.BEFORE_START, null, false, false, false)
                } else {
                    DayResult(date, DayStatus.FUTURE, null, false, false, false)
                }
            },
            loggedDays = logged.size,
            notLoggedDays = elapsed - logged.size,
            onTargetDays = onTarget,
            overDays = results.count { it.status == DayStatus.OVER },
            underDays = results.count { it.status == DayStatus.UNDER },
            suspectedUnderLogDays = results.count { it.suspectedUnderLog },
            sumTargetKcal = sumTarget,
            sumEatenKcal = sumEaten,
            sumDeltaKcal = sumEaten - sumTarget,
            avgKcal = logged.avgOrNull { it.kcal },
            maxDay = loggedResults.maxByOrNull { r -> byDate.getValue(r.date).kcal },
            minDay = loggedResults.minByOrNull { r -> byDate.getValue(r.date).kcal },
            tdeeBalanceKcal = tdeeBalance,
            expectedWeightChangeKg = tdeeBalance / NutritionCalculator.KCAL_PER_KG,
            avgProteinG = avgProtein,
            avgCarbG = logged.avgOrNull { it.carbG },
            avgFatG = logged.avgOrNull { it.fatG },
            avgTargetProteinG = avgTargetProtein,
            proteinMetDays = results.count { it.proteinMet },
            waterTotalMl = waterTotal,
            waterTargetTotalMl = waterTargetTotal,
            avgWaterMl = waterTotal.toDouble() / divisor,
            waterMetDays = waterMet,
            waterShortfallMl = shortfall,
            avgWeightKg = avgWeight,
            weightChangeKg = if (avgWeight != null && previousWeekAvgWeightKg != null) {
                Math.round((avgWeight - previousWeekAvgWeightKg) * 1000) / 1000.0
            } else {
                null
            },
            topFoods = topFoods,
            topMeal = topMeal,
            score = score,
            grade = grade(score.total),
        )
    }

    fun grade(score: Int): WeekGrade = when {
        score >= 85 -> WeekGrade.EXCELLENT
        score >= 70 -> WeekGrade.GOOD
        score >= 50 -> WeekGrade.NEEDS_WORK
        else -> WeekGrade.POOR
    }

    fun compare(current: WeeklyStats, previousWeekStart: LocalDate, previousDays: List<DayInput>): WeekComparison {
        val sameDayToday = previousWeekStart.plusDays((current.elapsedDays - 1).toLong())
        return trends(current, compute(previousWeekStart, sameDayToday, previousDays))
    }

    private fun trends(current: WeeklyStats, prev: WeeklyStats) = WeekComparison(
        avgKcal = trendOrNull(current.avgKcal, prev.avgKcal),
        onTargetDays = trend(current.onTargetDays.toDouble(), prev.onTargetDays.toDouble()),
        avgWaterMl = trend(current.avgWaterMl, prev.avgWaterMl),
        avgProteinG = trendOrNull(current.avgProteinG, prev.avgProteinG),
        score = trend(current.score.total.toDouble(), prev.score.total.toDouble()),
    )

    private fun dayResult(date: LocalDate, d: DayInput?, inProgress: Boolean = false): DayResult {
        if (d == null) return DayResult(date, DayStatus.NOT_LOGGED, null, false, false, false)
        val status = dayStatus(d.entryCount, d.kcal, d.targetKcal)
        val logged = status != DayStatus.NOT_LOGGED
        return DayResult(
            date = date,
            status = status,
            delta = if (logged) d.kcal - d.targetKcal else null,
            suspectedUnderLog = logged && !inProgress && d.kcal < UNDER_LOG_RATIO * d.targetKcal,
            waterMet = d.targetWaterMl > 0 && d.waterMl >= d.targetWaterMl,
            proteinMet = logged && d.proteinG >= PROTEIN_MET_RATIO * d.targetProteinG,
        )
    }

    private fun trend(now: Double, before: Double): Trend = when {
        abs(now - before) < 1e-9 -> Trend.SAME
        now > before -> Trend.UP
        else -> Trend.DOWN
    }

    private fun trendOrNull(now: Double?, before: Double?): Trend? =
        if (now == null || before == null) null else trend(now, before)

    private inline fun List<DayInput>.avgOrNull(selector: (DayInput) -> Double): Double? =
        if (isEmpty()) null else sumOf(selector) / size
}
