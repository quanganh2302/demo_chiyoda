package com.example.myapplication.ultis.valid

import com.example.myapplication.helper.PackingLabel
import com.example.myapplication.models.MasterLabelData

object CompareScanValidator {

    fun canAddToList(
        newLabel: PackingLabel?,
        master: MasterLabelData,
        currentList: List<PackingLabel>
    ): Boolean {
        if (newLabel == null) return false

        // Match WO
        if (newLabel.workOrderNo != master.wono) return false

        // Match date (so string, vì UI đang dùng string)
        if (newLabel.date?.toString() != master.date) return false

        // Không cho trùng (theo number hoặc itemCode)
        val duplicated = currentList.any {
            it.number == newLabel.number && it.number != null
        }
        if (duplicated) return false

        return true
    }
}