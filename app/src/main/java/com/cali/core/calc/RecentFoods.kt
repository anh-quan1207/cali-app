package com.cali.core.calc

import java.time.LocalDate

data class RecentCandidate(val id: Long, val date: LocalDate, val eatenAt: Long, val name: String, val kcal: Double)

/** Tab "Gần đây" của S13: món đã ghi, nhóm theo ngày, bỏ trùng tên + kcal (giữ lần mới nhất). */
object RecentFoods {
    private val spaces = "\\s+".toRegex()

    fun group(items: List<RecentCandidate>): List<Pair<LocalDate, List<RecentCandidate>>> {
        val seen = HashSet<Pair<String, Long>>()
        val kept = items
            .sortedWith(compareByDescending<RecentCandidate> { it.date }.thenByDescending { it.eatenAt }.thenByDescending { it.id })
            .filter { seen.add(it.name.trim().lowercase().replace(spaces, " ") to Math.round(it.kcal)) }
        return kept.groupBy { it.date }.toList()
    }
}
