package com.example.myapplication.ui.activity_inspect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.models.MasterLabelData
import com.example.myapplication.service.KeyenceScannerService
import com.example.myapplication.service.ScanEvent
import com.example.myapplication.ui.activity_compare.CompareActivity
import com.example.myapplication.ui.base.BaseActivity
import com.example.myapplication.ui.custom.DateInputView
import com.google.android.material.textfield.TextInputEditText
import com.rejowan.cutetoast.CuteToast
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CreateMasterLabelActivity : BaseActivity() {

    override fun hasDrawer(): Boolean = false

    companion object {
        private const val TAG = "CreateMasterLabel"
        const val EXTRA_WONO = "EXTRA_WONO"
        const val EXTRA_DATE = "EXTRA_DATE"
        const val EXTRA_QTY = "EXTRA_QTY"
    }

    // ===== UI =====
    private lateinit var edtWoNo: TextInputEditText
    private lateinit var edtQty: TextInputEditText
    private lateinit var dateInputView: DateInputView
    private lateinit var btnCreate: Button

    // ===== SCAN =====
    private var scanJob: Job? = null

    // ================= LIFECYCLE =================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_master_label)

        bindViews()
        setupActions()
    }

    override fun onStart() {
        super.onStart()
        scanJob = lifecycleScope.launch {
            KeyenceScannerService.scanEventFlow.collect { event ->
                handleScanEvent(event)
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        KeyenceScannerService.startScan()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        KeyenceScannerService.stopScan()
//    }

    override fun onStop() {
        super.onStop()
        scanJob?.cancel()
        scanJob = null
    }
//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//
//        Log.d("SCAN_RAW", "==== RAW INTENT START ====")
//        intent?.extras?.keySet()?.forEach { key ->
//            Log.d("SCAN_RAW", "$key = ${intent.extras?.get(key)}")
//        }
//        Log.d("SCAN_RAW", "==== RAW INTENT END ====")
//    }
    // ================= INIT =================

    private fun bindViews() {
        edtWoNo = findViewById(R.id.edtWoNo)
        edtQty = findViewById(R.id.edtQty)
        dateInputView = findViewById(R.id.dateInputView)
        btnCreate = findViewById(R.id.btnCreate)
    }

    private fun setupActions() {
        btnCreate.setOnClickListener {
            createMasterLabel()
        }
    }

    // ================= CORE =================

    private fun createMasterLabel() {
        val wono = edtWoNo.text?.toString()?.trim().orEmpty()
        val qtyText = edtQty.text?.toString()?.trim().orEmpty()
        val date = dateInputView.getDate().trim()

        if (wono.isEmpty()) {
            showInfo(getString(R.string.validate_wono))
            return
        }

        val qty = safeParseInt(qtyText)
        if (qty == null || qty <= 0) {
            showInfo(getString(R.string.error_qty_invalid))
            return
        }

        if (date.isEmpty()) {
            showInfo(getString(R.string.error_date_empty))
            return
        }

        val masterLabel = MasterLabelData(
            productCode = "DEMO_PRODUCT",
            wono = wono,
            date = date,
            qty = qty
        )

        Log.d(TAG, "Create MasterLabel: $masterLabel")

        val intent = Intent(this, CompareActivity::class.java).apply {
            putExtra(EXTRA_WONO, masterLabel.wono)
            putExtra(EXTRA_DATE, masterLabel.date)
            putExtra(EXTRA_QTY, masterLabel.qty)
        }
        startActivity(intent)
    }

    // ================= SCAN =================

    private fun handleScanEvent(event: ScanEvent) {
        when (event) {

            is ScanEvent.Success -> {
                val sanitized = sanitizeScanData(event.data)
                if (isValidProductionCode(sanitized)) {
                    edtWoNo.setText(sanitized)
                } else {
                    showInfo(getString(R.string.scan_not_master))
                }
            }

            ScanEvent.Timeout ->
                showInfo(getString(R.string.timeout_scan))

            ScanEvent.Alert ->
                showInfo(getString(R.string.ocr_scan))

            is ScanEvent.Failed ->
                showError(event.reason)

            ScanEvent.Canceled -> {
                // ignore
            }
        }
    }

    // ================= VALIDATION =================

    private fun sanitizeScanData(data: String): String {
        return data.trim()
            .replace("\r", "")
            .replace("\n", "")
            .replace("\t", "")
            .take(50)
    }

    private fun isValidProductionCode(data: String): Boolean {
        if (data.isEmpty()) return false
        if (data.length > 50) return false
        if (data.contains(",")) return false
        if (data.contains("\n") || data.contains("\r") || data.contains("\t")) return false
        if (data.startsWith("http") || data.startsWith("www")) return false
        if (!data.matches(Regex("^[A-Za-z0-9-_]+$"))) return false
        return true
    }

    private fun safeParseInt(value: String): Int? {
        return try {
            value.toInt()
        } catch (e: Exception) {
            null
        }
    }

    // ================= TOAST =================

    private fun showInfo(message: String) {
        CuteToast.ct(
            this,
            message,
            CuteToast.LENGTH_SHORT,
            CuteToast.INFO,
            true
        ).show()
    }

    private fun showError(message: String) {
        CuteToast.ct(
            this,
            message,
            CuteToast.LENGTH_SHORT,
            CuteToast.ERROR,
            true
        ).show()
    }
}
