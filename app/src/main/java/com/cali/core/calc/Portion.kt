package com.cali.core.calc

/** Hệ số khẩu phần ½ · 1 · 1½ · 2 · gram (S12/S14). Giá trị không làm tròn; hiển thị mới làm tròn. */
object PortionScaler {
    val PRESETS = listOf(0.5, 1.0, 1.5, 2.0)

    fun scale(base: FoodValues, factor: Double): FoodValues = FoodValues(
        kcal = base.kcal * factor,
        proteinG = base.proteinG * factor,
        carbG = base.carbG * factor,
        fatG = base.fatG * factor,
    )

    fun factorForGrams(baseGrams: Double?, grams: Double): Double? =
        if (baseGrams == null || baseGrams <= 0 || grams <= 0) null else grams / baseGrams

    fun label(factor: Double): String = when (factor) {
        0.5 -> "½"
        1.0 -> "×1"
        1.5 -> "1½"
        2.0 -> "2"
        else -> "×" + (Math.round(factor * 100) / 100.0).toString().removeSuffix(".0").replace('.', ',')
    }
}

/** Ô nhập cân (S01, S31): khoảng 30–250 kg (09 §8 mục 21), gợi ý "Có phải 74,1 kg?". */
object WeightInput {
    const val MIN = 30.0
    const val MAX = 250.0

    fun parse(text: String): Double? = text.trim().replace(',', '.').takeIf { it.isNotEmpty() }?.toDoubleOrNull()

    fun inRange(kg: Double): Boolean = kg in MIN..MAX

    /** Gõ thiếu dấu phẩy (741 → 74,1; 7410 → 74,1). Đã hợp lệ hoặc không đoán được → null. */
    fun suggest(kg: Double): Double? {
        if (inRange(kg)) return null
        for (div in listOf(10.0, 100.0)) {
            val v = Math.round(kg / div * 10) / 10.0
            if (kg > MAX && inRange(v)) return v
        }
        return null
    }
}
