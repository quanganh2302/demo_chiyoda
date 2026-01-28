package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.models.MasterLabelData
import com.example.myapplication.service.ScannerConfig
import com.example.myapplication.service.ScanEvent
import com.example.myapplication.ui.custom.DateInputView
import com.example.myapplication.ui.utils.ToastManager
import com.example.myapplication.ui.utils.contants.BundleKeys
import com.example.myapplication.ultis.valid.MasterLabelValid
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CreateMasterLabelFragment : Fragment() {

    companion object {
        private const val TAG = "CreateMasterLabelFragment"
    }

    // ================= UI =================
    private lateinit var edtWoNo: TextInputEditText
    private lateinit var edtQty: TextInputEditText
    private lateinit var dateInputView: DateInputView
    private lateinit var btnCreate: Button
    private var btnDebug: Button? = null

    // ================= SCAN =================
    private var scanJob: Job? = null

    // ================= LIFECYCLE =================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_create_master_label,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        setupActions()

        arguments?.getString(BundleKeys.EXTRA_WONO)?.let {
            edtWoNo.setText(it)
        }

        parentFragmentManager.setFragmentResultListener(
            "clear_data_request",
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean("should_clear", false)) {
                clearAllInputFields()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        scanJob = viewLifecycleOwner.lifecycleScope.launch {
            ScannerConfig.initialize(requireContext())

            ScannerConfig.scanEventFlow.collect { event ->
                handleScanEvent(event)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        scanJob?.cancel()
        scanJob = null
    }

    // ================= INIT =================

    private fun bindViews(view: View) {
        edtWoNo = view.findViewById(R.id.edtWoNo)
        edtQty = view.findViewById(R.id.edtQty)
        dateInputView = view.findViewById(R.id.dateInputView)
        btnCreate = view.findViewById(R.id.btnCreate)
        btnDebug = view.findViewById(R.id.btnBack)
    }

    private fun setupActions() {
        btnCreate.setOnClickListener {
            createMasterLabel()
        }

        btnDebug?.setOnClickListener {
            mockScanWO()
        }
    }

    // ================= CORE =================

    private fun createMasterLabel() {
        val wono = edtWoNo.text?.toString()?.trim().orEmpty()
        val qtyText = edtQty.text?.toString()?.trim().orEmpty()
        val dateText = dateInputView.getDate().trim()

        if (wono.isEmpty()) {
            ToastManager.info(requireContext(), getString(R.string.validate_wono))
            return
        }

        val qty = qtyText.toIntOrNull()
        if (qty == null || qty <= 0) {
            ToastManager.info(requireContext(), getString(R.string.error_qty_invalid))
            return
        }

        if (dateText.isEmpty()) {
            ToastManager.info(requireContext(), getString(R.string.error_date_empty))
            return
        }

        // Parse date từ UI format sang LocalDate
        val localDate = runCatching {
            LocalDate.parse(
                dateText,
                DateTimeFormatter
                    .ofLocalizedDate(java.time.format.FormatStyle.SHORT)
                    .withLocale(Locale.getDefault())
            )
        }.getOrNull()

        if (localDate == null) {
            ToastManager.error(requireContext(), getString(R.string.error_date_invalid))
            return
        }

        // Convert LocalDate sang ISO format string (yyyy-MM-dd)
        val isoDate = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val masterLabel = MasterLabelData(
            productCode = "DEMO_PRODUCT",
            wono = wono,
            date = isoDate,  // Truyền ISO format: "2026-01-26"
            qty = qty
        )

        Log.d(TAG, "Create master label: $masterLabel")

        goToCompare(masterLabel)
    }

    private fun goToCompare(master: MasterLabelData) {
        val fragment = CompareFragment().apply {
            arguments = Bundle().apply {
                putString(BundleKeys.EXTRA_WONO, master.wono)
                putString(BundleKeys.EXTRA_DATE, master.date)  // ISO format
                putInt(BundleKeys.EXTRA_QTY, master.qty)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // ================= SCAN =================

    private fun handleScanEvent(event: ScanEvent) {
        when (event) {
            is ScanEvent.Success -> {
                val sanitized = sanitizeScanData(event.data)
                if (MasterLabelValid.isValid(sanitized)) {
                    edtWoNo.setText(sanitized)
                } else {
                    ToastManager.info(
                        requireContext(),
                        getString(R.string.scan_not_master)
                    )
                }
            }

            ScanEvent.Timeout ->
                ToastManager.info(requireContext(), getString(R.string.timeout_scan))

            ScanEvent.Alert ->
                ToastManager.info(requireContext(), getString(R.string.ocr_scan))

            is ScanEvent.Failed ->
                ToastManager.error(requireContext(), event.reason)

            ScanEvent.Canceled -> Unit
        }
    }

    // ================= UTIL =================

    private fun clearAllInputFields() {
        edtWoNo.setText("")
        edtQty.setText("")
        dateInputView.clearDate()
    }

    private fun sanitizeScanData(data: String): String {
        return data.trim()
            .replace("\r", "")
            .replace("\n", "")
            .replace("\t", "")
            .take(50)
    }

    private fun mockScanWO() {
        val mockData = "WOA00902735"
        handleScanEvent(ScanEvent.Success(data = mockData, codeType = "QR_CODE"))
    }
}