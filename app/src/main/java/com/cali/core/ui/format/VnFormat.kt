package com.cali.core.ui.format

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** Số kiểu Việt: 1.740 · 74,1 (CLAUDE.md). */
object VnFormat {
    private val symbols = DecimalFormatSymbols(Locale.forLanguageTag("vi-VN")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    fun int(value: Number): String = DecimalFormat("#,##0", symbols).format(value)

    fun decimal1(value: Double): String = DecimalFormat("#,##0.0", symbols).format(value)
}
