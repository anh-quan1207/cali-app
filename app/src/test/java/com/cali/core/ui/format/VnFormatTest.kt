package com.cali.core.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class VnFormatTest {
    @Test fun `so kieu Viet`() {
        assertEquals("1.740", VnFormat.int(1740))
        assertEquals("430", VnFormat.int(430))
        assertEquals("12.600", VnFormat.int(12600.0))
        assertEquals("74,1", VnFormat.decimal1(74.1))
        assertEquals("1.658,5", VnFormat.decimal1(1658.5))
        assertEquals("0,52", VnFormat.decimal2(0.5207))
    }

    @Test fun `toc do, can, ngay`() {
        assertEquals("0,25", VnFormat.rate(0.25))
        assertEquals("0,5", VnFormat.rate(0.5))
        assertEquals("1,0", VnFormat.rate(1.0))
        assertEquals("75", VnFormat.kg(75.0))
        assertEquals("74,1", VnFormat.kg(74.1))
        assertEquals("19/02/2027", VnFormat.date(LocalDate.of(2027, 2, 19)))
        assertEquals("09/11", VnFormat.dayMonth(LocalDate.of(2026, 11, 9)))
    }
}
