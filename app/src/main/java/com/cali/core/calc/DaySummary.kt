package com.cali.core.calc

import com.cali.core.calc.model.MealType

/** Một món trong ngày, đủ để tính tổng và vẽ vòng. [eatenAtMinute] = phút trong ngày của `eaten_at`. */
data class DayFood(
    val id: Long,
    val eatenAtMinute: Int,
    val meal: MealType,
    val kcal: Double,
    val proteinG: Double = 0.0,
    val carbG: Double = 0.0,
    val fatG: Double = 0.0,
)

/** Trạng thái vòng calo (DESIGN.md): trong mục tiêu = primary, vượt ≤ 10% = hổ phách, vượt nhiều = error. */
enum class RingStatus { EMPTY, WITHIN, OVER_LIGHT, OVER_MUCH, NO_GOAL }

/** Một đoạn vòng calo: các món liên tiếp (theo giờ ăn) cùng bữa. */
data class RingPart(val meal: MealType, val kcal: Double)

data class MealTotal(val kcal: Double, val count: Int)

data class DaySummary(
    val totalKcal: Double,
    val proteinG: Double,
    val carbG: Double,
    val fatG: Double,
    val meals: Map<MealType, MealTotal>,
    val segments: List<RingPart>,
    val targetKcal: Int?,
    val remainingKcal: Int,
    val overKcal: Int,
    val overPercent: Int,
    val status: RingStatus,
)

object DaySummaryCalculator {
    const val LIGHT_OVER_RATIO = 0.10

    fun summarize(foods: List<DayFood>, targetKcal: Int?): DaySummary {
        val sorted = foods.sortedWith(compareBy<DayFood> { it.eatenAtMinute }.thenBy { it.id })
        val segments = mutableListOf<RingPart>()
        for (f in sorted) {
            val last = segments.lastOrNull()
            if (last != null && last.meal == f.meal) {
                segments[segments.lastIndex] = last.copy(kcal = last.kcal + f.kcal)
            } else {
                segments += RingPart(f.meal, f.kcal)
            }
        }
        val meals = MealType.entries.associateWith { m ->
            val list = foods.filter { it.meal == m }
            MealTotal(list.sumOf { it.kcal }, list.size)
        }
        val total = foods.sumOf { it.kcal }
        val r = statusOf(total, targetKcal, empty = foods.isEmpty())
        return DaySummary(
            totalKcal = total,
            proteinG = foods.sumOf { it.proteinG },
            carbG = foods.sumOf { it.carbG },
            fatG = foods.sumOf { it.fatG },
            meals = meals,
            segments = segments,
            targetKcal = targetKcal,
            remainingKcal = r.remaining,
            overKcal = r.over,
            overPercent = r.overPercent,
            status = r.status,
        )
    }

    internal data class Remain(val remaining: Int, val over: Int, val overPercent: Int, val status: RingStatus)

    /** Số hiển thị làm tròn; ngưỡng vượt so trên số chưa làm tròn. */
    internal fun statusOf(total: Double, target: Int?, empty: Boolean): Remain {
        if (target == null || target <= 0) return Remain(0, 0, 0, RingStatus.NO_GOAL)
        val diff = total - target
        val remaining = if (diff < 0) Math.round(-diff).toInt() else 0
        val over = if (diff > 0) Math.round(diff).toInt() else 0
        val pct = if (diff > 0) Math.round(diff / target * 100).toInt() else 0
        val status = when {
            empty -> RingStatus.EMPTY
            diff <= 0 -> RingStatus.WITHIN
            diff <= target * LIGHT_OVER_RATIO + 1e-9 -> RingStatus.OVER_LIGHT
            else -> RingStatus.OVER_MUCH
        }
        return Remain(remaining, over, pct, status)
    }
}

/** Xem trước ngày sau khi lưu (S12) hoặc sửa một món (S14). Thêm mới: oldKcal = 0. */
data class Projection(
    /** Phần giữ nguyên (đoạn đặc). */
    val baseKcal: Double,
    /** Phần thêm (đoạn chờ, tô nhạt có viền). */
    val addedKcal: Double,
    /** Phần bỏ đi (nét đứt, không tô). */
    val removedKcal: Double,
    val afterKcal: Double,
    val remainingAfter: Int,
    val overAfter: Int,
    val overPercentAfter: Int,
    val statusAfter: RingStatus,
) {
    companion object {
        fun of(dayKcal: Double, oldKcal: Double, newKcal: Double, targetKcal: Int?): Projection {
            val after = dayKcal - oldKcal + newKcal
            val r = DaySummaryCalculator.statusOf(after, targetKcal, empty = after <= 0.0)
            return Projection(
                baseKcal = dayKcal - oldKcal + minOf(oldKcal, newKcal),
                addedKcal = maxOf(0.0, newKcal - oldKcal),
                removedKcal = maxOf(0.0, oldKcal - newKcal),
                afterKcal = after,
                remainingAfter = r.remaining,
                overAfter = r.over,
                overPercentAfter = r.overPercent,
                statusAfter = r.status,
            )
        }
    }
}
