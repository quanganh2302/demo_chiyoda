package com.example.myapplication.service

import android.content.Context
import android.util.Log
import com.keyence.autoid.sdk.SdkStatus
import com.keyence.autoid.sdk.scan.DecodeResult
import com.keyence.autoid.sdk.scan.ScanManager
import com.keyence.autoid.sdk.scan.scanparams.CodeType
import com.keyence.autoid.sdk.scan.scanparams.DataOutput
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 🔥 Scanner Service dùng chung cho toàn app
 * - Giữ DUY NHẤT 1 ScanManager
 * - Che giấu hoàn toàn Keyence SDK
 * - Các màn hình chỉ SUBSCRIBE kết quả
 */
object KeyenceScannerService : ScanManager.DataListener {

    private const val TAG = "KeyenceScannerService"

    private var scanManager: ScanManager? = null
    private var isInitialized = false

    /* ===== PUBLIC EVENT STREAM ===== */

    private val _scanEventFlow = MutableSharedFlow<ScanEvent>(
        extraBufferCapacity = 1
    )
    val scanEventFlow = _scanEventFlow.asSharedFlow()

    /* ===== INIT ===== */

    fun initialize(context: Context) {
        if (isInitialized) return

        try {
            scanManager = ScanManager.createScanManager(context.applicationContext)
            scanManager?.addDataListener(this)

            configureScanTypes()
            configureDataOutput()

            isInitialized = true
            Log.i(TAG, "Scanner initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize scanner", e)
        }
    }

    /* ===== CONFIG ===== */

    private fun configureScanTypes() {
        val codeType = CodeType().apply {
            qrCode = true
            code128 = true
            code39 = true
            upcEanJan = false
        }

        val status = scanManager?.setConfig(codeType)
        if (status != SdkStatus.SUCCESS) {
            Log.w(TAG, "Failed to set CodeType config: $status")
        }
    }

    private fun configureDataOutput() {
        try {
            val dataOutput = DataOutput().apply {
                setDefault()
                keyStrokeOutput.enabled = false // 🔥 TẮT keyboard wedge
            }
            scanManager?.setConfig(dataOutput)
        } catch (e: Exception) {
            Log.w(TAG, "DataOutput not supported: ${e.message}")
        }
    }

    /* ===== CONTROL ===== */

    fun startScan() {
        scanManager?.startRead()
    }

    fun stopScan() {
        if (scanManager?.isReading() == true) {
            scanManager?.stopRead()
        }
    }

    fun lockScanner() {
        scanManager?.lockScanner()
    }

    fun release() {
        scanManager?.removeDataListener(this)
        scanManager?.releaseScanManager()
        scanManager = null
        isInitialized = false
    }

    /* ===== CALLBACK FROM SDK ===== */

    override fun onDataReceived(result: DecodeResult) {

        Log.d(
            "KeyenceScanner",
            "RAW_SCAN | result=${result.result} | codeType=${result.codeType ?: "N/A"} | data=${result.data ?: "<NO_DATA>"}"
        )


        when (result.result) {
            DecodeResult.Result.SUCCESS -> {
                _scanEventFlow.tryEmit(
                    ScanEvent.Success(
                        data = result.data ?: "",
                        codeType = result.codeType ?: ""
                    )
                )
            }

            DecodeResult.Result.TIMEOUT -> {
                Log.d("KeyenceScanner", "SCAN_TIMEOUT")
                _scanEventFlow.tryEmit(ScanEvent.Timeout)
            }

            DecodeResult.Result.CANCELED -> {
                Log.d("KeyenceScanner", "SCAN_CANCELED")
                _scanEventFlow.tryEmit(ScanEvent.Canceled)
            }

            DecodeResult.Result.FAILED -> {
                Log.e("KeyenceScanner", "SCAN_FAILED")
                _scanEventFlow.tryEmit(
                    ScanEvent.Failed("Scan failed")
                )
            }

            DecodeResult.Result.ALERT -> {
                Log.w("KeyenceScanner", "SCAN_ALERT")
                _scanEventFlow.tryEmit(ScanEvent.Alert)
            }

            else -> {
                Log.w("KeyenceScanner", "UNHANDLED_RESULT=${result.result}")
            }
        }
    }

}
