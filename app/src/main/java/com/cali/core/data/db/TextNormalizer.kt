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

    /** Vị trí (trên chuỗi gốc) của lần khớp không dấu đầu tiên, để tô phần khớp ở S13/S34. */
    fun matchRange(name: String, query: String): IntRange? {
        val q = normalize(query)
        if (q.isEmpty()) return null
        // Bỏ dấu từng ký tự để giữ nguyên vị trí trên chuỗi gốc.
        val folded = buildString { name.forEach { append(foldChar(it)) } }
        val i = folded.indexOf(q)
        return if (i < 0) null else i until i + q.length
    }

    private fun foldChar(c: Char): Char {
        val s = Normalizer.normalize(c.toString(), Normalizer.Form.NFD).replace(marks, "")
        val base = s.firstOrNull() ?: c
        return when (base) {
            'đ', 'Đ' -> 'd'
            else -> base.lowercaseChar()
        }
    }
}
