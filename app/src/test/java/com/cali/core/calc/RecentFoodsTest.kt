package com.cali.core.calc

import com.cali.core.data.db.TextNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RecentFoodsTest {
    private val d16 = LocalDate.of(2026, 11, 16)
    private val d15 = LocalDate.of(2026, 11, 15)

    private fun r(id: Long, date: LocalDate, at: Long, name: String, kcal: Double) =
        RecentCandidate(id, date, at, name, kcal)

    @Test fun `nhom theo ngay, moi nhat truoc, bo trung ten va kcal`() {
        val groups = RecentFoods.group(
            listOf(
                r(1, d15, 100, "Phở bò", 470.0),
                r(2, d16, 300, "Trà đá", 0.0),
                r(3, d16, 400, "Bún bò Huế", 490.0),
                r(4, d15, 200, "Trà đá", 0.0), // trùng với món hôm nay → chỉ giữ lần mới nhất
                r(5, d16, 350, "bún bò huế ", 490.0), // trùng (không phân biệt hoa thường/khoảng trắng)
                r(6, d15, 150, "Phở bò", 600.0), // khác kcal → giữ
            ),
        )
        assertEquals(listOf(d16, d15), groups.map { it.first })
        assertEquals(listOf(3L, 2L), groups[0].second.map { it.id })
        assertEquals(listOf(6L, 1L), groups[1].second.map { it.id })
    }
}

class HighlightTest {
    @Test fun `khop khong dau tra ve vi tri tren ten goc`() {
        assertEquals(0..2, TextNormalizer.matchRange("Trà sữa trân châu", "tra"))
        assertEquals(5..7, TextNormalizer.matchRange("Bánh tráng trộn", "tra"))
        assertEquals(4..6, TextNormalizer.matchRange("Cơm trắng", "TRA"))
        assertEquals(0..6, TextNormalizer.matchRange("Đậu phụ", "dau phu"))
        assertNull(TextNormalizer.matchRange("Phở bò", "bun"))
        assertNull(TextNormalizer.matchRange("Phở bò", "  "))
    }
}
