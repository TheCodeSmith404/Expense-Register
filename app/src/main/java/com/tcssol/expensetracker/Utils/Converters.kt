package com.tcssol.expensetracker.Utils

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {
    companion object {
        private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        @JvmStatic
        @TypeConverter
        fun fromString(value: String?): LocalDate? {
            return value?.let { LocalDate.parse(it, formatter) }
        }

        @JvmStatic
        @TypeConverter
        fun toString(date: LocalDate?): String? {
            return date?.format(formatter)
        }
    }
}
