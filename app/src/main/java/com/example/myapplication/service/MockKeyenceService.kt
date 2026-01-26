package com.example.myapplication.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object MockKeyenceScannerService {

    private const val TAG = "MockScannerService"

    private var isInitialized = false

    private val _scanEventFlow = MutableSharedFlow<ScanEvent>(
        extraBufferCapacity = 1
    )
    val scanEventFlow = _scanEventFlow.asSharedFlow()

    suspend fun initialize(context: Context) {
        if (isInitialized) {
            Log.d(TAG, "Mock Scanner already initialized")
            return
        }

        delay(100) // Giả lập thời gian khởi tạo (rất nhanh)
        isInitialized = true
        Log.i(TAG, "✅ Mock Scanner initialized successfully (NO DEVICE NEEDED)")
    }

    fun startScan() {
        Log.d(TAG, "Mock startScan called")
    }

    fun stopScan() {
        Log.d(TAG, "Mock stopScan called")
    }

    fun lockScanner() {
        Log.d(TAG, "Mock lockScanner called")
    }

    fun release() {
        isInitialized = false
        Log.d(TAG, "Mock release called")
    }

    fun simulateScanMasterLabel(wono: String = "WOA00902735") {
        Log.d(TAG, "🧪 Simulating Master Label scan: $wono")
        _scanEventFlow.tryEmit(
            ScanEvent.Success(
                data = wono,
                codeType = "QR_CODE"
            )
        )
    }

    fun simulateScanPackingLabel(
        workOrderNo: String = "WOA00902735",
        number: String = "001",
        date: String = "26/01/2026",
        productCode: String = "PROD-ABC"
    ) {
        val qrData = "$workOrderNo|$number|$date|$productCode"
        Log.d(TAG, "🧪 Simulating Packing Label scan: $qrData")
        _scanEventFlow.tryEmit(
            ScanEvent.Success(
                data = qrData,
                codeType = "QR_CODE"
            )
        )
    }

    fun simulateTimeout() {
        Log.d(TAG, "Simulating timeout")
        _scanEventFlow.tryEmit(ScanEvent.Timeout)
    }

    fun simulateFailed(reason: String = "Mock scan failed") {
        Log.d(TAG, "Simulating failed: $reason")
        _scanEventFlow.tryEmit(ScanEvent.Failed(reason))
    }

    fun simulateAlert() {
        Log.d(TAG, "Simulating alert")
        _scanEventFlow.tryEmit(ScanEvent.Alert)
    }
}