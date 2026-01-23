package com.example.myapplication.ui.activity_compare

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCompareLabelBinding
import com.example.myapplication.helper.PackingLabel
import com.example.myapplication.models.MasterLabelData
import com.example.myapplication.service.KeyenceScannerService
import com.example.myapplication.service.ScanEvent
import com.example.myapplication.ui.activity_inspect.CreateMasterLabelActivity
import com.example.myapplication.ui.adapter.RecyclerViewAdapter
import com.example.myapplication.ui.base.BaseActivity
import com.example.myapplication.ultis.valid.MasterLabelValid
import com.rejowan.cutetoast.CuteToast
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CompareActivity : BaseActivity() {

    override fun hasDrawer(): Boolean = false

    private lateinit var binding: ActivityCompareLabelBinding
    private lateinit var adapter: RecyclerViewAdapter
    private val productList = mutableListOf<MasterLabelData>()
    private val scannedLabels = mutableListOf<PackingLabel>()

    private var scanJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCompareLabelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupRecyclerView()
        setupButtons()
    }

    // ================= HEADER =================
    private fun setupHeader() {
        val data = MasterLabelData(
            productCode = "",
            wono = intent.getStringExtra(CreateMasterLabelActivity.EXTRA_WONO) ?: "",
            date = intent.getStringExtra(CreateMasterLabelActivity.EXTRA_DATE) ?: "",
            qty = intent.getIntExtra(CreateMasterLabelActivity.EXTRA_QTY, 0)
        )

        bindMasterLabel(data)
    }

    private fun bindMasterLabel(data: MasterLabelData) = with(binding) {
        tvWoNoValue.text = data.wono
        tvDateValue.text = data.date
        tvQtyValue.text = data.qty.toString()
    }
    override fun onStart() {
        super.onStart()
        scanJob = lifecycleScope.launch {
            KeyenceScannerService.scanEventFlow.collect { event ->
                handleScanEvent(event)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        scanJob?.cancel()
        scanJob = null
    }
    // ================= SCAN =================

    private fun handleScanEvent(event: ScanEvent) {
        when (event) {

            is ScanEvent.Success -> {
                handleScanSuccess(event.data)
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
    private fun handleScanSuccess(rawData: String) {
        Log.d("SCAN_FLOW", "HANDLE_SUCCESS | raw=$rawData")

        val packingLabel = PackingLabel.fromQrCodeData(rawData)

        if (packingLabel == null) {
            Log.e("SCAN_FLOW", "HANDLE_SUCCESS | PARSE FAILED")
            return
        }

        Log.d(
            "SCAN_FLOW",
            "HANDLE_SUCCESS | PARSED = $packingLabel"
        )
        addItemToList(packingLabel)
    }
    private fun addItemToList(label: PackingLabel) {

        // (Optional) chống scan trùng theo number / code
        val duplicated = scannedLabels.any {
            it.number == label.number && it.number != null
        }

        if (duplicated) {
            CuteToast.ct(
                this,
                getString(R.string.label_already_scanned),
                CuteToast.LENGTH_SHORT,
                CuteToast.INFO,
                true
            ).show()
            return
        }

        val insertPosition = scannedLabels.size
        scannedLabels.add(label)

        adapter.notifyItemInserted(insertPosition)

        updateScannedLabelCount()

        // Scroll tới item mới (UX tốt)
        binding.recyclerViewItems.scrollToPosition(insertPosition)
    }



    // ================= RECYCLERVIEW =================
    private fun setupRecyclerView() {
        adapter = RecyclerViewAdapter(scannedLabels) { position ->
            if (position in scannedLabels.indices) {
                scannedLabels.removeAt(position)
                adapter.notifyItemRemoved(position)

                updateScannedLabelCount()

                if (scannedLabels.isEmpty()) {
                    CuteToast.ct(
                        this,
                        getString(com.example.myapplication.R.string.list_empty),
                        CuteToast.LENGTH_SHORT,
                        CuteToast.INFO,
                        true
                    ).show()
                }
            }
        }

        binding.recyclerViewItems.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewItems.adapter = adapter

        adapter.notifyDataSetChanged()

        updateScannedLabelCount()
    }

    private fun updateScannedLabelCount() {
        binding.bfRecyclerViewItems.text = getString(
            R.string.scanned_label_count_format,
            scannedLabels.size
        )
    }

    // ================= BUTTONS =================
    private fun setupButtons() = with(binding) {
        btnBack.setOnClickListener { finish() }

        btnCompare.setOnClickListener {
            CuteToast.ct(
                this@CompareActivity,
                getString(com.example.myapplication.R.string.compare_success),
                CuteToast.LENGTH_SHORT,
                CuteToast.SUCCESS,
                true
            ).show()
        }
    }
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
