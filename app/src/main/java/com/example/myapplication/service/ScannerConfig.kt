package com.example.myapplication.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.SharedFlow

/**
 * Configuration để switch giữa Real Scanner và Mock Scanner
 */
object ScannerConfig {

    private const val TAG = "ScannerConfig"  // 🔥 THÊM TAG ĐỂ DEBUG

    // ⚙️ Đổi flag này để switch giữa real/mock scanner
    private const val USE_MOCK_SCANNER = false // ✅ true = mock (không cần thiết bị), false = real

    suspend fun initialize(context: Context) {
        // 🔥 THÊM LOG ĐỂ KIỂM TRA
        Log.d(TAG, "🔥 Initializing scanner... USE_MOCK_SCANNER = $USE_MOCK_SCANNER")

        if (USE_MOCK_SCANNER) {
            Log.d(TAG, "→ Using MOCK Scanner (No device needed)")
            MockKeyenceScannerService.initialize(context)
        } else {
            Log.d(TAG, "→ Using REAL Scanner (Device required)")
            KeyenceScannerService.initialize(context)
        }
    }

    val scanEventFlow: SharedFlow<ScanEvent>
        get() = if (USE_MOCK_SCANNER) {
            MockKeyenceScannerService.scanEventFlow
        } else {
            KeyenceScannerService.scanEventFlow
        }

    fun startScan() {
        if (USE_MOCK_SCANNER) {
            MockKeyenceScannerService.startScan()
        } else {
            KeyenceScannerService.startScan()
        }
    }

    fun stopScan() {
        if (USE_MOCK_SCANNER) {
            MockKeyenceScannerService.stopScan()
        } else {
            KeyenceScannerService.stopScan()
        }
    }

    fun lockScanner() {
        if (USE_MOCK_SCANNER) {
            MockKeyenceScannerService.lockScanner()
        } else {
            KeyenceScannerService.lockScanner()
        }
    }

    fun release() {
        if (USE_MOCK_SCANNER) {
            MockKeyenceScannerService.release()
        } else {
            KeyenceScannerService.release()
        }
    }

    // ===== MOCK HELPERS =====

    fun simulateScanMasterLabel(wono: String = "WOA00902735") {
        if (USE_MOCK_SCANNER) {
            MockKeyenceScannerService.simulateScanMasterLabel(wono)
        }
    }

    fun simulateScanPackingLabel(
        workOrderNo: String = "WOA00902735",
        number: String = "001",
        date: String = "26/01/2026",
        productCode: String = "PROD-ABC"
    ) {
        if (USE_MOCK_SCANNER) {
            MockKeyenceScannerService.simulateScanPackingLabel(
                workOrderNo, number, date, productCode
            )
        }
    }
}