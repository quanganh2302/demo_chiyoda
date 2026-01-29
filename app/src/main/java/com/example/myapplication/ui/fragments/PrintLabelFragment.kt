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
import com.example.myapplication.models.Box
import com.example.myapplication.ui.adapter.PackingAdapter
import com.example.myapplication.ui.utils.ToastManager
import com.example.myapplication.ui.utils.contants.BundleKeys

class PrintLabelFragment : Fragment() {

    companion object {
        private const val TAG = "PrintLabelFragment"
    }

    private var _binding: FragmentPrintLabelBinding? = null
    private val binding get() = _binding!!
    private val listBoxes = mutableListOf<Box>()
    private var qty: Int = 0
    private var packingType: String = ""

    private lateinit var packingAdapter: PackingAdapter

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

        qty = arguments?.getInt(BundleKeys.EXTRA_QTY) ?: 0
        packingType = arguments?.getString(BundleKeys.EXTRA_PACKING_TYPE, "").toString()
        binding.edtNumberOfCompleted.setText(qty.toString())

        setupRecyclerView()
        updateListTitle()
        setupButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
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

        // Tìm box đã tồn tại
        val existingIndex = listBoxes.indexOfFirst { it.numberBox == productsPerBox }

        if (existingIndex != -1) {
            // Box đã tồn tại -> tăng count
            listBoxes[existingIndex].count += boxCount
            packingAdapter.notifyItemChanged(existingIndex)

            Log.d(TAG, "Updated box: $productsPerBox, count: ${listBoxes[existingIndex].count}")
        } else {
            // Box mới -> thêm vào list
            val newBox = Box(
                numberBox = productsPerBox,
                count = boxCount
            )
            listBoxes.add(newBox)
            packingAdapter.notifyItemInserted(listBoxes.size - 1)

            // Scroll đến item mới
            binding.rvBoxes.smoothScrollToPosition(listBoxes.size - 1)

            Log.d(TAG, "Added new box: $productsPerBox. Total: ${listBoxes.size}")
        }

        // Clear input
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

    /**
     * Xử lý nút Auto - Tự động phân phối boxes
     */
    private fun handleAutoDistribute() {
        // Lấy số lượng đã hoàn thành
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

        // Lấy số lượng sản phẩm mỗi box
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

        // Gọi logic tạo boxes tự động
        autoGenerateBoxes(completedCount, packingQuantity)
    }

    /**
     * Logic tạo boxes tự động
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun autoGenerateBoxes(totalItems: Int, itemsPerBox: Int) {
        // Xóa tất cả boxes cũ
        listBoxes.clear()

        // Tính số boxes đầy và số lượng còn lại
        val fullBoxes = totalItems / itemsPerBox
        val remainingItems = totalItems % itemsPerBox

        // Thêm các boxes đầy
        if (fullBoxes > 0) {
            listBoxes.add(
                Box(
                    numberBox = itemsPerBox,
                    count = fullBoxes.toLong()
                )
            )
        }

        // Thêm box chứa số lượng còn lại (nếu có)
        if (remainingItems > 0) {
            listBoxes.add(
                Box(
                    numberBox = remainingItems,
                    count = 1
                )
            )
        }

        // Cập nhật UI
        packingAdapter.notifyDataSetChanged()
        updateListTitle()

        // Scroll đến item cuối
        if (listBoxes.isNotEmpty()) {
            binding.rvBoxes.smoothScrollToPosition(listBoxes.size - 1)
        }

        // Clear input
        binding.edtProductsPerBox.text?.clear()
        binding.etBoxCount.text?.clear()

        // Show success message
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

    private fun printLabels() {
//        if (listBoxes.isEmpty()) {
//            ToastManager.warning(requireContext(), getString(R.string.toast_no_boxes))
//            return
//        }

        // TODO: Implement print labels logic
        Log.d(TAG, "Print labels clicked. Total boxes: ${listBoxes.size}")
        ToastManager.success(requireContext(), "Preparing to print ${listBoxes.size} labels")
    }
}