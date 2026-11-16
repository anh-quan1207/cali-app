package com.cali.core.calc

import kotlin.math.ceil
import kotlin.math.roundToInt

/** Số của một ngày nước (S15, ô Nước S10): làn dài = mục tiêu, phần vượt chạy làn riêng. */
object WaterDayMath {
    fun of(amounts: List<Int>, goalMl: Int, cupMl: Int): WaterDayStats {
        val cumulative = amounts.runningReduce(Int::plus)
        val total = cumulative.lastOrNull() ?: 0
        val reached = if (goalMl > 0) cumulative.indexOfFirst { it >= goalMl }.takeIf { it >= 0 } else null
        val left = (goalMl - total).coerceAtLeast(0)
        val segments = buildList {
            var c = 0
            for (a in amounts) {
                val from = minOf(c, goalMl)
                val to = minOf(c + a, goalMl)
                if (to > from) add(from to to)
                c += a
            }
        }
        return WaterDayStats(
            total = total,
            percent = if (goalMl > 0) (total * 100.0 / goalMl).roundToInt() else 0,
            leftMl = left,
            cupsLeft = if (cupMl > 0) ceil(left.toDouble() / cupMl).toInt() else 0,
            overMl = if (goalMl > 0) (total - goalMl).coerceAtLeast(0) else 0,
            reachedIndex = reached,
            cumulative = cumulative,
            laneSegments = segments,
        )
    }
}

data class WaterDayStats(
    val total: Int,
    val percent: Int,
    val leftMl: Int,
    val cupsLeft: Int,
    val overMl: Int,
    /** Lần uống làm tổng chạm mục tiêu (hàng "Đạt mục tiêu · N ml"). */
    val reachedIndex: Int?,
    val cumulative: List<Int>,
    /** Đoạn [từ, tới) ml của từng lần uống trên làn chính, cắt tại mục tiêu. */
    val laneSegments: List<Pair<Int, Int>>,
)
