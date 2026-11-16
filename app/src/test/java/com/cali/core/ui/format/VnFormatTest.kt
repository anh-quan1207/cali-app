package com.cali.core.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test

class VnFormatTest {
    @Test fun `so kieu Viet`() {
        assertEquals("1.740", VnFormat.int(1740))
        assertEquals("430", VnFormat.int(430))
        assertEquals("12.600", VnFormat.int(12600.0))
        assertEquals("74,1", VnFormat.decimal1(74.1))
        assertEquals("1.658,5", VnFormat.decimal1(1658.5))
    }
}
