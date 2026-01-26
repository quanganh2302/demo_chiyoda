package com.example.myapplication.ultis.common

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object Common {

    /* ================== CONSTANT FORMATTERS ================== */

    /** Chuẩn lưu trữ nội bộ / SharedPreferences: yyyy-MM-dd */
    val STORAGE_DATE_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ISO_LOCAL_DATE

    /** Chuẩn hiển thị VN */
    private val VI_DATE_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("vi", "VN"))

    /* ================== STORAGE / DOMAIN ================== */

    /** Convert LocalDate → String để lưu */
    fun dateToStorageString(date: LocalDate?): String {
        return date?.format(STORAGE_DATE_FORMATTER) ?: ""
    }

    /** Convert String (yyyy-MM-dd) → LocalDate */
    fun storageStringToDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDate.parse(value, STORAGE_DATE_FORMATTER)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    /* ================== UI DISPLAY ================== */

    /** Format LocalDate để hiển thị theo locale */
    fun formatDateForUi(date: LocalDate?): String {
        if (date == null) return ""
        val locale = Locale.getDefault()
        return if (locale.language == "vi") {
            date.format(VI_DATE_FORMATTER)
        } else {
            date.format(STORAGE_DATE_FORMATTER.withLocale(locale))
        }
    }

    /** Parse String từ UI → LocalDate (khi CẦN) */
    fun parseUiDateToLocalDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null
        val locale = Locale.getDefault()
        return try {
            if (locale.language == "vi") {
                LocalDate.parse(value, VI_DATE_FORMATTER)
            } else {
                LocalDate.parse(value, STORAGE_DATE_FORMATTER.withLocale(locale))
            }
        } catch (_: DateTimeParseException) {
            null
        }
    }

    /* ================== TIME ================== */

    /** Parse time dạng HHmmss (từ barcode / payload) */
    fun tryParseTime(value: String?, pattern: String = "HHmmss"): LocalTime? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalTime.parse(value, DateTimeFormatter.ofPattern(pattern))
        } catch (_: DateTimeParseException) {
            null
        }
    }

    /* ================== LEGACY SUPPORT ================== */

    /**
     * ⚠️ Legacy – giữ để không phá code cũ
     * Chỉ dùng khi đọc packing label cũ
     */
    fun parseLocalDatePacking(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDate.parse(value, VI_DATE_FORMATTER)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}