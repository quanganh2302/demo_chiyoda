package com.example.myapplication.ui.fragment_inspect

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
import com.example.myapplication.service.KeyenceScannerService
import com.example.myapplication.service.ScanEvent
import com.example.myapplication.ui.fragment_compare.CompareFragment
import com.example.myapplication.ui.custom.DateInputView
import com.example.myapplication.ultis.common.ToastManager
import com.example.myapplication.ultis.valid.MasterLabelValid
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CreateMasterLabelFragment : Fragment() {

    companion object {
        const val EXTRA_WONO = "EXTRA_WONO"
        const val EXTRA_DATE = "EXTRA_DATE"
        const val EXTRA_QTY = "EXTRA_QTY"

        private const val TAG = "CreateMasterLabelFragment"
    }

    // ================= UI =================
    private lateinit var edtWoNo: TextInputEditText
    private lateinit var edtQty: TextInputEditText
    private lateinit var dateInputView: DateInputView
    private lateinit var btnCreate: Button

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

        // Nhận WONO nếu đi từ HomeFragment (Kiểm nhận)
        arguments?.getString(EXTRA_WONO)?.let {
            edtWoNo.setText(it)
        }
    }

    override fun onStart() {
        super.onStart()
        scanJob = viewLifecycleOwner.lifecycleScope.launch {
            KeyenceScannerService.scanEventFlow.collect {
                handleScanEvent(it)
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
            ToastManager.info(requireContext(), getString(R.string.validate_wono))
            return
        }

        val qty = qtyText.toIntOrNull()
        if (qty == null || qty <= 0) {
            ToastManager.info(requireContext(), getString(R.string.error_qty_invalid))
            return
        }

        if (date.isEmpty()) {
            ToastManager.info(requireContext(), getString(R.string.error_date_empty))
            return
        }

        val masterLabel = MasterLabelData(
            productCode = "DEMO_PRODUCT",
            wono = wono,
            date = date,
            qty = qty
        )

        Log.d(TAG, "Create master label: $masterLabel")

        // 👉 CHUYỂN SANG BƯỚC TIẾP THEO (CompareFragment)
        goToCompare(masterLabel)
    }

    private fun goToCompare(master: MasterLabelData) {
        val fragment = CompareFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_WONO, master.wono)
                putString(EXTRA_DATE, master.date)
                putInt(EXTRA_QTY, master.qty)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // ⚠️ đúng container
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

    private fun sanitizeScanData(data: String): String {
        return data.trim()
            .replace("\r", "")
            .replace("\n", "")
            .replace("\t", "")
            .take(50)
    }
}
