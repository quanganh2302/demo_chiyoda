package com.example.myapplication.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentPrintLabelBinding
import com.example.myapplication.helper.ChiyodaInfo
import com.example.myapplication.models.Box
import com.example.myapplication.service.signalR.SignalRManager
import com.example.myapplication.service.signalR.SimpleEventListener
import com.example.myapplication.models.SubmitResponseDto
import com.example.myapplication.models.SystemStatusUpdate
import com.example.myapplication.ui.adapter.PackingAdapter
import com.example.myapplication.ui.utils.ToastManager
import com.example.myapplication.ui.utils.contants.BundleKeys
import com.example.myapplication.ultis.common.SignalRResponseHandler
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PrintLabelFragment : Fragment() {

    companion object {
        private const val TAG = "PrintLabelFragment"
    }

    private var _binding: FragmentPrintLabelBinding? = null
    private val binding get() = _binding!!
    private val listBoxes = mutableListOf<Box>()
    private var qty: Int = 0
    private var packingType: String = ""
    private var wono: String = ""
    private var entryDate: String = ""

    private lateinit var packingAdapter: PackingAdapter

    // SignalR Event Listener
    private val signalREventListener = object : SimpleEventListener() {
        override fun onSubmitResponse(response: SubmitResponseDto) {
            // Handle response from RPA
            activity?.runOnUiThread {
                SignalRResponseHandler.handleAndShowResponse(
                    requireContext(),
                    response,
                    onSuccess = { successResponse ->
                        Log.d(TAG, "✅ Task submitted successfully: ${successResponse.jobId}")
                        // Navigate back or clear data
                        clearDataAndNavigateBack()
                    },
                    onError = { errorResponse ->
                        Log.e(TAG, "❌ Task submission failed: ${errorResponse.error}")
                    }
                )
            }
        }

        override fun onSystemStatusUpdate(update: SystemStatusUpdate) {
            // Handle system status updates
            Log.d(TAG, "System status: ${update.status}, Can submit: ${update.canSubmitTasks}")
        }

        override fun onRpaError(error: String) {
            // Handle RPA errors
            activity?.runOnUiThread {
                ToastManager.error(requireContext(), "RPA Error: $error")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrintLabelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get arguments
        qty = arguments?.getInt(BundleKeys.EXTRA_QTY) ?: 0
        packingType = arguments?.getString(BundleKeys.EXTRA_PACKING_TYPE, "").toString()
        wono = arguments?.getString(BundleKeys.EXTRA_WONO, "").toString()
        entryDate = arguments?.getString(BundleKeys.EXTRA_DATE, "").toString()

        binding.edtNumberOfCompleted.setText(qty.toString())

        // Register SignalR event listener
        SignalRManager.registerEventListener(signalREventListener)

        setupRecyclerView()
        updateListTitle()
        setupButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // IMPORTANT: Unregister listener
        SignalRManager.unregisterEventListener(signalREventListener)
        _binding = null
    }

    private fun setupRecyclerView() {
        packingAdapter = PackingAdapter(listBoxes) { position ->
            if (position in listBoxes.indices) {
                listBoxes.removeAt(position)
                packingAdapter.notifyItemRemoved(position)
                updateListTitle()
            }
        }

        binding.rvBoxes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = packingAdapter
        }
    }

    private fun setupButtons() {
        // Add button
        binding.btnAdd.setOnClickListener {
            addBox()
        }

        // Auto button
        binding.btnAuto.setOnClickListener {
            handleAutoDistribute()
        }

        // Print button
        binding.btnCreate.setOnClickListener {
            printLabels()
        }
    }

    private fun addBox() {
        val productPerBox = binding.edtProductsPerBox.text.toString()
        val boxCountText = binding.etBoxCount.text.toString()

        if (productPerBox.isEmpty()) {
            ToastManager.warning(requireContext(), getString(R.string.enter_product_per_box))
            return
        }

        val productsPerBox = productPerBox.toInt()

        if (productsPerBox <= 0) {
            return
        }

        val boxCount = boxCountText.toLongOrNull()?.takeIf { it > 0 } ?: 1

        val existingIndex = listBoxes.indexOfFirst { it.numberBox == productsPerBox }

        if (existingIndex != -1) {
            listBoxes[existingIndex].count += boxCount
            packingAdapter.notifyItemChanged(existingIndex)
            Log.d(TAG, "Updated box: $productsPerBox, count: ${listBoxes[existingIndex].count}")
        } else {
            val newBox = Box(
                numberBox = productsPerBox,
                count = boxCount
            )
            listBoxes.add(newBox)
            packingAdapter.notifyItemInserted(listBoxes.size - 1)
            binding.rvBoxes.smoothScrollToPosition(listBoxes.size - 1)
            Log.d(TAG, "Added new box: $productsPerBox. Total: ${listBoxes.size}")
        }

        binding.edtProductsPerBox.text?.clear()
        binding.etBoxCount.text?.clear()
        updateListTitle()
    }

    private fun updateListTitle() {
        val totalBoxes = listBoxes.sumOf { it.count }
        val totalProducts = listBoxes.sumOf { it.numberBox * it.count }

        if (totalBoxes == 0L) {
            binding.tvListTitle.setText(R.string.list_boxes)
        } else {
            binding.tvListTitle.text =
                getString(R.string.list_boxes) +
                        " $totalBoxes $packingType ($totalProducts ${getString(R.string.product)})"
        }
    }

    private fun handleAutoDistribute() {
        val completedCount = binding.edtNumberOfCompleted.text.toString().toIntOrNull() ?: 0
        if (completedCount < 0) {
            ToastManager.warning(
                requireContext(),
                getString(R.string.toast_invalid_completed_count)
            )
            return
        }

        if (completedCount == 0) {
            ToastManager.warning(
                requireContext(),
                getString(R.string.enter_product_per_box)
            )
            return
        }

        val packingQuantityText = binding.edtProductsPerBox.text.toString()
        if (packingQuantityText.isEmpty()) {
            ToastManager.warning(
                requireContext(),
                getString(R.string.enter_product_per_box)
            )
            return
        }

        val packingQuantity = packingQuantityText.toIntOrNull()
        if (packingQuantity == null || packingQuantity <= 0) {
            ToastManager.warning(
                requireContext(),
                getString(R.string.toast_invalid_packing_quantity)
            )
            return
        }

        autoGenerateBoxes(completedCount, packingQuantity)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun autoGenerateBoxes(totalItems: Int, itemsPerBox: Int) {
        listBoxes.clear()

        val fullBoxes = totalItems / itemsPerBox
        val remainingItems = totalItems % itemsPerBox

        if (fullBoxes > 0) {
            listBoxes.add(
                Box(
                    numberBox = itemsPerBox,
                    count = fullBoxes.toLong()
                )
            )
        }

        if (remainingItems > 0) {
            listBoxes.add(
                Box(
                    numberBox = remainingItems,
                    count = 1
                )
            )
        }

        packingAdapter.notifyDataSetChanged()
        updateListTitle()

        if (listBoxes.isNotEmpty()) {
            binding.rvBoxes.smoothScrollToPosition(listBoxes.size - 1)
        }

        binding.edtProductsPerBox.text?.clear()
        binding.etBoxCount.text?.clear()

        val message = if (remainingItems > 0) {
            getString(
                R.string.toast_boxes_created_with_remaining,
                fullBoxes,
                packingType,
                itemsPerBox,
                remainingItems
            )
        } else {
            getString(
                R.string.toast_boxes_created_full,
                fullBoxes,
                packingType,
                itemsPerBox
            )
        }

        ToastManager.success(requireContext(), message)
        Log.d(TAG, "Auto generated: $fullBoxes full boxes + $remainingItems remaining items")
    }

    /**
     * ✅ CHECK CONNECTION & SUBMIT TASK TO RPA
     */
    private fun printLabels() {
        // 1. Validate data
        if (listBoxes.isEmpty()) {
            ToastManager.warning(requireContext(), getString(R.string.toast_no_boxes))
            return
        }

        // 2. Check SignalR connection
        if (!SignalRManager.isConnected()) {
            ToastManager.error(
                requireContext(),
                "Not connected to RPA system. Please wait for connection..."
            )
            Log.e(TAG, "❌ Cannot submit task - SignalR not connected")
            return
        }

        // 3. Get connection info for debugging
        val connectionInfo = SignalRManager.getConnectionInfo()
        Log.d(TAG, "Connection state: ${connectionInfo.state}")
        Log.d(TAG, "Server URL: ${connectionInfo.serverUrl}")

        // 4. Calculate total completed count
        val completedCount = binding.edtNumberOfCompleted.text.toString().toLongOrNull() ?: 0L

        // 5. Get packing type code (convert display name to code)
        val packingTypeCode = getPackingTypeCode(packingType)

        // 6. Create ChiyodaInfo object
        val chiyodaInfo = ChiyodaInfo(
            wono = wono,
            completedCount = completedCount,
            entryDate = entryDate,  // ISO format: "2026-01-26"
            packingType = packingTypeCode,
            wonoComplete = false
        )

        Log.d(TAG, "═══════════════════════════════════")
        Log.d(TAG, "📤 Submitting task to RPA:")
        Log.d(TAG, "WO No: ${chiyodaInfo.wono}")
        Log.d(TAG, "Completed Count: ${chiyodaInfo.completedCount}")
        Log.d(TAG, "Entry Date: ${chiyodaInfo.entryDate}")
        Log.d(TAG, "Packing Type: ${chiyodaInfo.packingType}")
        Log.d(TAG, "Total Boxes: ${listBoxes.size}")
        Log.d(TAG, "═══════════════════════════════════")

        // 7. Submit task to RPA
        val success = SignalRManager.submitTask(chiyodaInfo)

        if (success) {
            ToastManager.success(
                requireContext(),
                "Task submitted to RPA. Please wait for response..."
            )
            Log.d(TAG, "✅ Task submitted successfully")
        } else {
            ToastManager.error(
                requireContext(),
                "Failed to submit task to RPA"
            )
            Log.e(TAG, "❌ Failed to submit task")
        }
    }

    /**
     * Convert packing type display name to code
     */
    private fun getPackingTypeCode(displayName: String): Int {
        // Assuming packing types are stored in resources
        val packingTypes = resources.getStringArray(R.array.packing_types)
        val index = packingTypes.indexOf(displayName)

        // Return index as code (0, 1, 2, etc.)
        // Adjust this logic based on your actual packing type codes
        return if (index >= 0) index else 0
    }

    /**
     * Clear data and navigate back after successful submission
     */
    private fun clearDataAndNavigateBack() {
        // Clear the fragment back stack to home
        parentFragmentManager.popBackStack(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }
}