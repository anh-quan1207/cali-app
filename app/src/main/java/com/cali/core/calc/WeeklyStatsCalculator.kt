package com.cali.core.calc

import com.cali.core.calc.model.MealType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.min

/** 08 §2. */
enum class DayStatus { ON_TARGET, OVER, UNDER, NOT_LOGGED }

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
    val weekStart: LocalDate,
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

/** 08 §2–§4. Thuần Kotlin. */
object WeeklyStatsCalculator {
    private const val TOLERANCE = 0.10
    private const val UNDER_LOG_RATIO = 0.50
    private const val PROTEIN_MET_RATIO = 0.90
    private const val TOP_FOODS = 5

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
    ): WeeklyStats {
        require(weekStart.dayOfWeek == DayOfWeek.MONDAY) { "Tuần bắt đầu Thứ Hai" }
        val elapsed = (ChronoUnit.DAYS.between(weekStart, today) + 1).toInt().coerceIn(1, 7)
        val window = (0 until elapsed).map { weekStart.plusDays(it.toLong()) }
        val byDate = days.associateBy { it.date }
        val inputs = window.map { byDate[it] }

        val results = window.mapIndexed { i, date -> dayResult(date, inputs[i]) }
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
            calorie = 40.0 * onTarget / elapsed,
            protein = 20.0 * proteinRatio,
            water = 20.0 * waterMet / elapsed,
            logging = 20.0 * consistent / elapsed,
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

        val windowFoods = foods.filter { it.date in window }
        val topFoods = windowFoods.groupBy { it.name }
            .map { (name, items) -> TopFood(name, items.size, items.sumOf { it.kcal }) }
            .sortedByDescending { it.kcal }
            .take(TOP_FOODS)
        val topMeal = windowFoods.groupBy { it.mealType }
            .mapValues { (_, items) -> items.groupBy { it.date }.values.map { d -> d.sumOf { it.kcal } }.average() }
            .maxByOrNull { it.value }?.key

        val sumTarget = logged.sumOf { it.targetKcal.toDouble() }
        val sumEaten = logged.sumOf { it.kcal }
        val totalScore = score.total

        return WeeklyStats(
            weekStart = weekStart,
            elapsedDays = elapsed,
            isPartial = elapsed < 7,
            days = (0 until 7).map { i ->
                if (i < elapsed) results[i] else dayResult(weekStart.plusDays(i.toLong()), null)
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
            avgWaterMl = waterTotal.toDouble() / elapsed,
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
            grade = grade(totalScore),
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
        val prev = compute(previousWeekStart, sameDayToday, previousDays)
        return WeekComparison(
            avgKcal = trendOrNull(current.avgKcal, prev.avgKcal),
            onTargetDays = trend(current.onTargetDays.toDouble(), prev.onTargetDays.toDouble()),
            avgWaterMl = trend(current.avgWaterMl, prev.avgWaterMl),
            avgProteinG = trendOrNull(current.avgProteinG, prev.avgProteinG),
            score = trend(current.score.total.toDouble(), prev.score.total.toDouble()),
        )
    }

    private fun dayResult(date: LocalDate, d: DayInput?): DayResult {
        if (d == null) return DayResult(date, DayStatus.NOT_LOGGED, null, false, false, false)
        val status = dayStatus(d.entryCount, d.kcal, d.targetKcal)
        val logged = status != DayStatus.NOT_LOGGED
        return DayResult(
            date = date,
            status = status,
            delta = if (logged) d.kcal - d.targetKcal else null,
            suspectedUnderLog = logged && d.kcal < UNDER_LOG_RATIO * d.targetKcal,
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
