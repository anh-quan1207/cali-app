package com.cali.core.ui.format

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Số kiểu Việt: 1.740 · 74,1 (CLAUDE.md). */
object VnFormat {
    private val symbols = DecimalFormatSymbols(Locale.forLanguageTag("vi-VN")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    fun int(value: Number): String = DecimalFormat("#,##0", symbols).format(value)

    fun decimal1(value: Double): String = DecimalFormat("#,##0.0", symbols).format(value)

    fun decimal2(value: Double): String = DecimalFormat("#,##0.00", symbols).format(value)

    /** Tốc độ kg/tuần: 0,25 · 0,5 · 1,0. */
    fun rate(value: Double): String = DecimalFormat("0.0#", symbols).format(value)

    /** Hệ số vận động: 1,2 · 1,375 · 1,55. */
    fun factor(value: Double): String = DecimalFormat("0.0##", symbols).format(value)

    /** Cân nặng: 75 → "75", 74,1 → "74,1". */
    fun kg(value: Double): String = DecimalFormat("#,##0.#", symbols).format(value)

    private val date = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dayMonth = DateTimeFormatter.ofPattern("dd/MM")

    fun date(d: LocalDate): String = d.format(date)
    fun dayMonth(d: LocalDate): String = d.format(dayMonth)
}
