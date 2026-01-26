package com.example.myapplication.ui.fragment_compare

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentCompareLabelBinding
import com.example.myapplication.helper.PackingLabel
import com.example.myapplication.models.MasterLabelData
import com.example.myapplication.service.KeyenceScannerService
import com.example.myapplication.service.ScanEvent
import com.example.myapplication.ui.adapter.RecyclerViewAdapter
import com.example.myapplication.ultis.common.ToastManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CompareFragment : Fragment() {

    companion object {
        const val EXTRA_WONO = "EXTRA_WONO"
        const val EXTRA_DATE = "EXTRA_DATE"
        const val EXTRA_QTY = "EXTRA_QTY"
    }

    private var _binding: FragmentCompareLabelBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecyclerViewAdapter
    private val scannedLabels = mutableListOf<PackingLabel>()

    private var scanJob: Job? = null

    // ================= LIFECYCLE =================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompareLabelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()
        setupRecyclerView()
        setupButtons()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ================= HEADER =================

    private fun setupHeader() {
        val data = MasterLabelData(
            productCode = "",
            wono = arguments?.getString(EXTRA_WONO).orEmpty(),
            date = arguments?.getString(EXTRA_DATE).orEmpty(),
            qty = arguments?.getInt(EXTRA_QTY) ?: 0
        )

        bindMasterLabel(data)
    }

    private fun bindMasterLabel(data: MasterLabelData) = with(binding) {
        tvWoNoValue.text = data.wono
        tvDateValue.text = data.date
        tvQtyValue.text = data.qty.toString()
    }

    // ================= SCAN =================

    private fun handleScanEvent(event: ScanEvent) {
        when (event) {
            is ScanEvent.Success -> handleScanSuccess(event.data)

            ScanEvent.Timeout ->
                ToastManager.info(requireContext(), getString(R.string.timeout_scan))

            ScanEvent.Alert ->
                ToastManager.info(requireContext(), getString(R.string.ocr_scan))

            is ScanEvent.Failed ->
                ToastManager.error(requireContext(), event.reason)

            ScanEvent.Canceled -> Unit
        }
    }

    private fun handleScanSuccess(rawData: String) {
        Log.d("SCAN_FLOW", "HANDLE_SUCCESS | raw=$rawData")

        val packingLabel = PackingLabel.fromQrCodeData(rawData)
        if (packingLabel == null) {
            Log.e("SCAN_FLOW", "PARSE FAILED")
            return
        }

        addItemToList(packingLabel)
    }

    private fun addItemToList(label: PackingLabel) {
        val duplicated = scannedLabels.any {
            it.number == label.number && it.number != null
        }

        if (duplicated) {
            ToastManager.info(
                requireContext(),
                getString(R.string.label_already_scanned)
            )
            return
        }

        val insertPosition = scannedLabels.size
        scannedLabels.add(label)
        adapter.notifyItemInserted(insertPosition)

        updateScannedLabelCount()
        binding.recyclerViewItems.scrollToPosition(insertPosition)
    }

    // ================= RECYCLERVIEW =================

    private fun setupRecyclerView() {
        adapter = RecyclerViewAdapter(scannedLabels) { position ->
            if (position in scannedLabels.indices) {
                scannedLabels.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateScannedLabelCount()
            }
        }

        binding.recyclerViewItems.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerViewItems.adapter = adapter
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
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnCompare.setOnClickListener {
            ToastManager.success(
                requireContext(),
                getString(R.string.compare_success)
            )
        }
    }
}