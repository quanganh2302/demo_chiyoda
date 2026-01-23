package com.example.myapplication.helper

/**
 * Model biểu diễn QR Chiyoda
 *
 * Format QR (7 phần):
 * [0] itemCode
 * [1] ...
 * [2] ...
 * [3] ...
 * [4] WONO   <-- TA LẤY CÁI NÀY
 * [5] ...
 * [6] ...
 */
data class ChiyodaLabel(
    val itemCode: String,
    val wono: String
) {
    companion object {

        /**
         * Convert raw QR string -> ChiyodaLabel
         * @return null nếu QR không đúng format Chiyoda
         */
        fun fromQr(rawQr: String): ChiyodaLabel? {
            val sanitized = rawQr
                .trim()
                .replace("\r", "")
                .replace("\n", "")

            val parts = sanitized.split(",")

            // QR Chiyoda BẮT BUỘC 7 field
            if (parts.size != 7) return null

            val itemCode = parts[0].trim()
            val wono = parts[4].trim()

            if (itemCode.isEmpty() || wono.isEmpty()) return null

            return ChiyodaLabel(
                itemCode = itemCode,
                wono = wono
            )
        }
    }
}
