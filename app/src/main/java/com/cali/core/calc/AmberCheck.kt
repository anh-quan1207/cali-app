package com.cali.core.calc

import kotlin.math.abs

/** Cờ hổ phách trên một món (03 §5, DESIGN.md The Amber Check Rule). */
enum class AmberFlag { INVALID_NUMBER_FIXED, MACRO_MISMATCH, VERY_HIGH }

data class FoodValues(val kcal: Double, val proteinG: Double, val carbG: Double, val fatG: Double)

data class AmberResult(val values: FoodValues, val flags: Set<AmberFlag>)

/** Kiểm tra số liệu một món sau khi AI trả về. Không chặn lưu, chỉ gắn cờ. */
object AmberCheck {
    const val MISMATCH_RATIO = 0.20
    const val VERY_HIGH_KCAL = 2500.0

    fun check(raw: FoodValues): AmberResult {
        val flags = mutableSetOf<AmberFlag>()
        fun clean(x: Double): Double =
            if (x.isNaN() || x.isInfinite() || x < 0) {
                flags += AmberFlag.INVALID_NUMBER_FIXED
                0.0
            } else {
                x
            }

        val kcal = clean(raw.kcal)
        val p = clean(raw.proteinG)
        val c = clean(raw.carbG)
        val f = clean(raw.fatG)

        // Mẫu số là kcal của AI (09 §8). kcal = 0 mà macro > 0 cũng là lệch.
        val fromMacros = 4 * p + 4 * c + 9 * f
        val diff = abs(kcal - fromMacros)
        if ((kcal > 0 && diff / kcal > MISMATCH_RATIO + 1e-12) || (kcal == 0.0 && fromMacros > 0)) {
            flags += AmberFlag.MACRO_MISMATCH
        }
        if (kcal > VERY_HIGH_KCAL) flags += AmberFlag.VERY_HIGH

        return AmberResult(
            values = FoodValues(
                kcal = Math.round(kcal / 5) * 5.0,
                proteinG = round1(p),
                carbG = round1(c),
                fatG = round1(f),
            ),
            flags = flags,
        )
    }

    private fun round1(x: Double): Double = Math.round(x * 10) / 10.0
}
