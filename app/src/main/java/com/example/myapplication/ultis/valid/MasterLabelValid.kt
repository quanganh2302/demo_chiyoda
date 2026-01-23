package com.example.myapplication.ultis.valid

object MasterLabelValid {
    private val VALID_PATTERN = Regex("^[A-Za-z0-9-_]+\$")

    fun isValid(raw: String?): Boolean {
        if (raw.isNullOrBlank()) return false

        val data = raw.trim()

        if (data.length > 50) return false
        if (data.contains(",")) return false
        if (data.contains("\n") || data.contains("\r") || data.contains("\t")) return false
        if (data.startsWith("http") || data.startsWith("www")) return false
        if (!data.matches(VALID_PATTERN)) return false

        return true
    }

}