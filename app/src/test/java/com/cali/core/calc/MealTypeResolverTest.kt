package com.cali.core.calc

import com.cali.core.calc.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class MealTypeResolverTest {

    private val r = MealTypeResolver()
    private fun t(h: Int, m: Int) = LocalTime.of(h, m)

    @Test fun `bien khung gio mac dinh 05 4-2`() {
        assertEquals(MealType.SNACK, r.fromTime(t(4, 59)))
        assertEquals(MealType.BREAKFAST, r.fromTime(t(5, 0)))
        assertEquals(MealType.BREAKFAST, r.fromTime(t(9, 59)))
        assertEquals(MealType.LUNCH, r.fromTime(t(10, 0)))
        assertEquals(MealType.LUNCH, r.fromTime(t(13, 59)))
        assertEquals(MealType.SNACK, r.fromTime(t(14, 0)))
        assertEquals(MealType.SNACK, r.fromTime(t(17, 29)))
        assertEquals(MealType.DINNER, r.fromTime(t(17, 30)))
        assertEquals(MealType.DINNER, r.fromTime(t(21, 59)))
        assertEquals(MealType.SNACK, r.fromTime(t(22, 0)))
        assertEquals(MealType.SNACK, r.fromTime(t(0, 0)))
        assertEquals(MealType.SNACK, r.fromTime(t(23, 30))) // 05 §4.4: 23:30 thuộc bữa Phụ
    }

    @Test fun `uu tien nguoi dung chon roi den AI roi den gio 05 4-3`() {
        assertEquals(MealType.DINNER, r.resolve(t(7, 0), manual = MealType.DINNER, fromAi = MealType.LUNCH))
        assertEquals(MealType.LUNCH, r.resolve(t(7, 0), manual = null, fromAi = MealType.LUNCH))
        assertEquals(MealType.BREAKFAST, r.resolve(t(7, 0)))
    }

    @Test fun `gio giua khung cho ngay khac hom nay 05 4-1`() {
        assertEquals(t(12, 0), r.defaultTimeFor(MealType.LUNCH)) // ví dụ trong đặc tả
        assertEquals(t(7, 30), r.defaultTimeFor(MealType.BREAKFAST))
        assertEquals(t(19, 45), r.defaultTimeFor(MealType.DINNER))
        assertEquals(t(15, 45), r.defaultTimeFor(MealType.SNACK)) // khung Phụ chiều
    }

    @Test fun `khung tuy chinh S44`() {
        val custom = MealTypeResolver(
            listOf(
                MealBoundary(t(6, 0), MealType.BREAKFAST),
                MealBoundary(t(11, 0), MealType.LUNCH),
                MealBoundary(t(18, 0), MealType.DINNER),
                MealBoundary(t(21, 0), MealType.SNACK),
            ),
        )
        assertEquals(MealType.SNACK, custom.fromTime(t(5, 59)))
        assertEquals(MealType.BREAKFAST, custom.fromTime(t(10, 59)))
        assertEquals(MealType.LUNCH, custom.fromTime(t(17, 59)))
        assertEquals(MealType.DINNER, custom.fromTime(t(18, 0)))
        // Khung Phụ vòng qua nửa đêm: 21:00 → 06:00, giữa là 01:30.
        assertEquals(t(1, 30), custom.defaultTimeFor(MealType.SNACK))
    }
}
