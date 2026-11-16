package com.cali.core.calc

import com.cali.core.calc.model.MealType
import java.time.LocalTime

/** Mốc bắt đầu một khung bữa; khung kéo tới mốc kế tiếp (vòng qua nửa đêm). */
data class MealBoundary(val start: LocalTime, val meal: MealType)

/** 05 §4.2–4.3. Khung giờ cấu hình được ở S44. */
class MealTypeResolver(boundaries: List<MealBoundary> = DEFAULT) {

    private val sorted = boundaries.sortedBy { it.start }

    /** Các mốc đã sắp theo giờ. */
    val boundaries: List<MealBoundary> get() = sorted

    init {
        require(sorted.isNotEmpty())
    }

    fun fromTime(time: LocalTime): MealType =
        // Trước mốc đầu tiên trong ngày → thuộc khung cuối (vòng qua nửa đêm).
        (sorted.lastOrNull { !time.isBefore(it.start) } ?: sorted.last()).meal

    fun resolve(eatenAt: LocalTime, manual: MealType? = null, fromAi: MealType? = null): MealType =
        manual ?: fromAi ?: fromTime(eatenAt)

    /**
     * Giờ giữa khung của bữa (ghi cho ngày khác hôm nay, 05 §4.1).
     * Bữa có nhiều khung (Phụ) lấy khung đầu tiên tính từ mốc Sáng; không có Sáng thì từ mốc sớm nhất.
     */
    fun defaultTimeFor(meal: MealType): LocalTime {
        val anchor = sorted.indexOfFirst { it.meal == MealType.BREAKFAST }.coerceAtLeast(0)
        val ordered = sorted.drop(anchor) + sorted.take(anchor)
        val idx = ordered.indexOfFirst { it.meal == meal }
        require(idx >= 0) { "Không có khung cho $meal" }
        val start = ordered[idx].start.toSecondOfDay()
        var end = ordered[(idx + 1) % ordered.size].start.toSecondOfDay()
        if (end <= start) end += DAY_SECONDS
        val mid = (start + (end - start) / 2) % DAY_SECONDS
        return LocalTime.ofSecondOfDay(mid.toLong())
    }

    companion object {
        private const val DAY_SECONDS = 24 * 60 * 60

        val DEFAULT = listOf(
            MealBoundary(LocalTime.of(5, 0), MealType.BREAKFAST),
            MealBoundary(LocalTime.of(10, 0), MealType.LUNCH),
            MealBoundary(LocalTime.of(14, 0), MealType.SNACK),
            MealBoundary(LocalTime.of(17, 30), MealType.DINNER),
            MealBoundary(LocalTime.of(22, 0), MealType.SNACK),
        )
    }
}
