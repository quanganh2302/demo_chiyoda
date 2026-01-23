package com.example.myapplication.helper

import com.example.myapplication.ultis.common.Common
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalTime

data class PackingLabel(
    val itemCode: String = "",
    val revision: String = "",
    val quantity: Int = 0,
    val date: LocalDate? =  LocalDate.now(),
    val workOrderNo: String = "",
    val customerRevision: String = "",
    val time: LocalTime? = null,
    val number: String? = null
): Serializable {
    companion object {
        fun fromQrCodeData(qrData: String): PackingLabel? {
            val parts = qrData.split(",")
            return try {
                if (parts.size < 6) return null

                val itemCode = parts[0]
                val revision = parts[1]
                val quantity = parts[2].toInt()
                val date = Common.parseLocalDatePacking(parts[3]) ?: return null
                val woNo = parts[4]
                val cusRev = parts[5]
                return if (parts.size >= 8) {
                    val time = Common.tryParseTime(parts[6]) ?: return null
                    val number = parts[7]
                    PackingLabel(itemCode,revision, quantity, date, woNo, cusRev, time, number)
                } else {
                    PackingLabel(itemCode,revision, quantity, date, woNo, cusRev, null, null)
                }

            } catch (e: Exception) {
                null
            }
        }
    }
}