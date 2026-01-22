package com.example.myapplication.hardware

import android.content.Context
import android.util.Log
import com.keyence.autoid.sdk.SdkStatus
import com.keyence.autoid.sdk.scan.DecodeResult
import com.keyence.autoid.sdk.scan.ScanManager
import com.keyence.autoid.sdk.scan.scanparams.CodeType
import com.keyence.autoid.sdk.scan.scanparams.DataOutput

interface OnScanResultListener {
    fun onScanSuccess(data: String, codeType: String)
    fun onScanFailed(reason: String)
    fun onScanTimeout()
    fun onScanCanceled()
    fun onScanAlert()

}

// 2. Tạo lớp ScanManagerHelper
class ScanManagerHelper(private val context: Context, private val listener: OnScanResultListener) :
    ScanManager.DataListener {

    private var mScanManager: ScanManager? = null

    // 3. Khởi tạo ScanManager
    fun initializeScanner() {
        if (mScanManager == null) {
            mScanManager = ScanManager.createScanManager(context)
            mScanManager?.addDataListener(this)

        }
    }

    // Tùy chọn: Cấu hình loại mã quét
    fun configureScanSettings(
        enableQrCode: Boolean = true,
        enableUpcEanJan: Boolean = true, // Ví dụ: Bật UPC/EAN/JAN
        enableCode128: Boolean = true,   // Ví dụ: Bật Code128
        enableCode39: Boolean = true,    // Ví dụ: Tắt Code39 nếu không cần
    ) {
        val codeType = CodeType()
        codeType.qrCode = enableQrCode
        codeType.upcEanJan = enableUpcEanJan
        codeType.code128 = enableCode128
        codeType.code39 = enableCode39
        // Thêm các loại mã khác nếu cần, ví dụ:
        // codeType.itf = true [8]
        // codeType.pdf417 = true [8]
        // codeType.datamatrix = true [8]
        // ... xem danh sách đầy đủ trong nguồn [5, 8-10]
        val status = mScanManager?.setConfig(codeType)
        if (status != SdkStatus.SUCCESS) {
            listener.onScanFailed("Không thể cấu hình quét: ${status?.name}")
        }
    }

    // 4. Bắt đầu quá trình quét
    fun startScan() {
        val status = mScanManager?.startRead()
        if (status != SdkStatus.SUCCESS) {
            listener.onScanFailed("Không thể bắt đầu quét: ${status?.name}")
        }
    }

    // 5. Dừng quá trình quét
    fun stopScan() {
        if (mScanManager?.isReading() == true) {
            mScanManager?.stopRead()
        }
    }

    // 6. Triển khai phương thức callback của ScanManager.DataListener
    override fun onDataReceived(decodeResult: DecodeResult) {
        // Lấy kết quả đọc
        val result = decodeResult.result
        val codeType = decodeResult.codeType
        val data = decodeResult.data
        Log.d("ScannerDebug", "onScanSuccess: codeType=$codeType, data=$data")
        when (result) {
            DecodeResult.Result.SUCCESS -> {
                // Đọc thành công, gửi dữ liệu về cho listener
                listener.onScanSuccess(data, codeType)
            }

            DecodeResult.Result.TIMEOUT -> {
                // Hết thời gian chờ đọc
                listener.onScanTimeout()
            }

            DecodeResult.Result.CANCELED -> {
                // Đọc bị hủy bỏ
                listener.onScanCanceled()
            }

            DecodeResult.Result.FAILED -> {
                // Lỗi khác khi đọc
                listener.onScanFailed("Lỗi khi quét mã: ${decodeResult.result.name}")
            }

            DecodeResult.Result.ALERT -> {
                // Cảnh báo (chỉ OCR)
                listener.onScanAlert()
            }

            DecodeResult.Result.OCR_MULTI_DATES,
            DecodeResult.Result.OCR_MULTI_DATES_ALERT,
            DecodeResult.Result.UPDATE_COLLECTION_DATA,
            DecodeResult.Result.SUCCESS_TEMPORARY -> {
                listener.onScanFailed("Unhandled result type: ${decodeResult.result.name}")
            }

            else -> {
                listener.onScanFailed("Unknown result type: ${decodeResult.result.name}")
            }
        }
    }
    fun configureDataOutput() {
        try {
//
            val dataOutput = DataOutput().apply {
                setDefault()
                keyStrokeOutput.enabled = false
            }
            val status = mScanManager?.setConfig(dataOutput)
            if (status == SdkStatus.SUCCESS) {
                Log.d("Keyence", "Cấu hình DataOutput thành công: KeyStrokeOutput đã tắt.")
            } else {
                Log.w("Keyence", "Không thể cấu hình DataOutput: ${status?.name} - Sử dụng quản lý focus thay thế")
                // Không cần throw exception, vì chúng ta đã có các biện pháp bảo vệ khác
            }
        } catch (e: Exception) {
            Log.w("Keyence", "DataOutput configuration không được hỗ trợ: ${e.message} - Sử dụng quản lý focus thay thế")
            // Không cần xử lý lỗi vì đã có focus management trong fragment
        }
    }
    // 7. Giải phóng tài nguyên khi không cần thiết nữa
    fun releaseScanner() {
        // Loại bỏ listener khi không cần thiết nữa để tránh rò rỉ bộ nhớ.
        mScanManager?.removeDataListener(this)
        // Giải phóng tài nguyên của thể hiện ScanManager.
        mScanManager?.releaseScanManager()
        mScanManager = null
    }
    fun lockScanner() {
       mScanManager?.lockScanner()
        //mScanManager?.
    }
    private fun debugFocusState() {

    }
}
