package com.example.myapplication.ui.fragments

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
import com.example.myapplication.service.ScanEvent
import com.example.myapplication.service.ScannerConfig
import com.example.myapplication.ui.adapter.RecyclerViewAdapter
import com.example.myapplication.ui.utils.DateUiFormatter
import com.example.myapplication.ui.utils.ToastManager
import com.keyence.autoid.sdk.notification.Notification
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate

class CompareFragment : Fragment() {

    companion object {
        const val EXTRA_WONO = "EXTRA_WONO"
        const val EXTRA_DATE = "EXTRA_DATE"
        const val EXTRA_QTY = "EXTRA_QTY"
        private const val TAG = "CompareFragment"
    }

    private var _binding: FragmentCompareLabelBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecyclerViewAdapter
    private val scannedLabels = mutableListOf<PackingLabel>()

    private var scanJob: Job? = null
    private var isDialogShowing = false
    private var masterLabel: MasterLabelData? = null
    private var mNotification: Notification? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(
            "SCANNED_LABELS",
            ArrayList(scannedLabels)
        )
    }

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

        // 1. Lấy dữ liệu từ arguments và tạo masterLabel
        setupMasterLabelData()

        // 2. Bind dữ liệu vào các view trong header
        // Việc này sẽ chạy lại mỗi khi view được tạo, sử dụng Locale mới nhất
        masterLabel?.let { bindMasterLabel(it) }

        setupRecyclerView()

        savedInstanceState
            ?.getSerializable("SCANNED_LABELS")
            ?.let { restored ->
                scannedLabels.clear()
                scannedLabels.addAll(restored as List<PackingLabel>)
                adapter.notifyDataSetChanged()
            }

        setupButtons()
    }

    override fun onStart() {
        super.onStart()

        Log.d(TAG, "onStart() - Initializing scanner...")

        scanJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                ScannerConfig.initialize(requireContext())
                Log.d(TAG, "✅ Scanner initialized successfully")

                ScannerConfig.scanEventFlow.collect { event ->
                    handleScanEvent(event)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Scanner initialization failed", e)
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

    private fun setupMasterLabelData() {
        val isoDate = arguments?.getString(EXTRA_DATE).orEmpty()

        masterLabel = MasterLabelData(
            productCode = "",
            wono = arguments?.getString(EXTRA_WONO).orEmpty(),
            date = isoDate,
            qty = arguments?.getInt(EXTRA_QTY) ?: 0
        )
    }

    private fun bindMasterLabel(data: MasterLabelData) = with(binding) {
        tvWoNoValue.text = data.wono

        // Format date từ ISO string sang định dạng UI theo locale hiện tại
        val formattedDate = runCatching {
            // Parse ISO date string thành LocalDate
            val localDate = LocalDate.parse(data.date)

            // Format theo locale hiện tại
            DateUiFormatter.format(localDate)
        }.getOrNull()

        // Gán giá trị đã format, hoặc giá trị gốc nếu có lỗi
        tvDateValue.text = formattedDate ?: data.date

        tvQtyValue.text = data.qty.toString()
    }

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
        if(isDialogShowing) return
        Log.d(TAG, "HANDLE_SUCCESS | raw=$rawData")

        val packingLabel = PackingLabel.fromQrCodeData(rawData)
        if (packingLabel == null) {
            ToastManager.warning(requireContext(), getString(R.string.scan_not_packing))
            return
        }

        if(packingLabel.workOrderNo != masterLabel?.wono) {
            isDialogShowing = true
            ToastManager.warning(requireContext(), getString(R.string.not_match_wo_no))
            view?.postDelayed({ isDialogShowing = false }, 800)
            playWarningSound()
            return
        }

        addItemToList(packingLabel)
    }

    fun playWarningSound(tone: Int = 15): Boolean {
        return false
    }

    private fun addItemToList(label: PackingLabel) {
        val duplicated = scannedLabels.any {
            it.number == label.number && it.number != null
        }

        if (duplicated) {
            ToastManager.info(requireContext(), getString(R.string.label_already_scanned))
            return
        }

        val insertPosition = scannedLabels.size
        scannedLabels.add(label)
        adapter.notifyItemInserted(insertPosition)
        updateScannedLabelCount()
        binding.recyclerViewItems.scrollToPosition(insertPosition)
    }

    private fun setupRecyclerView() {
        adapter = RecyclerViewAdapter(scannedLabels) { position ->
            if (position in scannedLabels.indices) {
                scannedLabels.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateScannedLabelCount()
            }
        }
        binding.recyclerViewItems.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewItems.adapter = adapter
        updateScannedLabelCount()
    }

    private fun updateScannedLabelCount() {
        binding.bfRecyclerViewItems.text = getString(
            R.string.scanned_label_count_format,
            scannedLabels.size
        )
    }

    private fun setupButtons() = with(binding) {
        btnBack.setOnClickListener {
            mockScanPackingLabel()
        }

        btnCompare.setOnClickListener {
            ToastManager.success(requireContext(), getString(R.string.compare_success))
        }
    }

    private fun mockScanPackingLabel() {
        val mockQrData =
            "835E 37833,R04,100,09/07/2025,${masterLabel?.wono},"

        Log.d(TAG, "🧪 Mock packing QR = $mockQrData")

        handleScanEvent(
            ScanEvent.Success(
                data = mockQrData,
                codeType = "QR_CODE"
            )
        )
    }
}