package com.cali.core.data.db

import java.text.Normalizer

/** Bỏ dấu tiếng Việt + chữ thường + gọn khoảng trắng, cho cột `name_normalized` (04 §2). */
object TextNormalizer {
    private val marks = "\\p{M}+".toRegex()
    private val spaces = "\\s+".toRegex()

    fun normalize(input: String): String =
        Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace(marks, "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .lowercase()
            .trim()
            .replace(spaces, " ")
}
