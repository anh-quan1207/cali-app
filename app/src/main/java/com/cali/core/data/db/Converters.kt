package com.cali.core.data.db

import androidx.room.TypeConverter
import java.time.LocalDate

/** 04 §1: LocalDate ↔ chuỗi ISO yyyy-MM-dd. */
class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)
}
