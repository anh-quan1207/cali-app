package com.cali.core.data.db

import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizerTest {
    @Test fun `bo dau, chu thuong, gon khoang trang`() {
        assertEquals("pho bo", TextNormalizer.normalize("Phở bò"))
        assertEquals("dau hu", TextNormalizer.normalize("Đậu hũ"))
        assertEquals("ca phe sua da", TextNormalizer.normalize("  Cà   phê sữa đá "))
        assertEquals("bun bo hue", TextNormalizer.normalize("BÚN BÒ HUẾ"))
    }
}
