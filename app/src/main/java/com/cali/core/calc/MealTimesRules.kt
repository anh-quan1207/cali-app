package com.cali.core.calc

import java.time.LocalTime

/**
 * S44 (05 §4.2, 09 §4 GĐ6 mục 5): sửa giờ bắt đầu từng khung bữa.
 * Danh sách giữ nguyên thứ tự (Sáng, Trưa, Phụ, Tối, Phụ); hai khung trùng giờ thì khung đứng sau
 * trong danh sách thắng (MealTypeResolver sắp ổn định), khung kia dài 0 phút ("bị che").
 */
object MealTimesRules {
    fun withStart(list: List<MealBoundary>, index: Int, start: LocalTime): List<MealBoundary> =
        list.mapIndexed { i, b -> if (i == index) b.copy(start = start) else b }

    private fun order(list: List<MealBoundary>): List<Int> = list.indices.sortedBy { list[it].start }

    /** Chỉ số các khung dài 0 phút vì có khung khác cùng giờ bắt đầu đứng sau. */
    fun shadowed(list: List<MealBoundary>): Set<Int> {
        val o = order(list)
        return o.zipWithNext().filter { (a, b) -> list[a].start == list[b].start }.map { it.first }.toSet()
    }

    /** Khung thực sự dùng giờ bắt đầu của [index] (chính nó nếu không trùng). */
    fun winnerOf(list: List<MealBoundary>, index: Int): Int =
        order(list).last { list[it].start == list[index].start }

    /** Giờ kết thúc khung [index] = giờ bắt đầu khác kế tiếp (vòng qua nửa đêm). */
    fun endOf(list: List<MealBoundary>, index: Int): LocalTime {
        val starts = list.map { it.start }.distinct().sorted()
        val s = list[index].start
        return starts.firstOrNull { it.isAfter(s) } ?: starts.first()
    }
}
