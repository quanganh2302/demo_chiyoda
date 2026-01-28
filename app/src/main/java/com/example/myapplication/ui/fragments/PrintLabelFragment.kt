package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentPrintLabelBinding
import com.example.myapplication.models.Box
import com.example.myapplication.ui.adapter.PackingAdapter
import com.example.myapplication.ui.utils.contants.BundleKeys

class PrintLabelFragment : Fragment() {

    companion object {
        private const val TAG = "PrintLabelFragment"
    }

    private var _binding: FragmentPrintLabelBinding? = null
    private val binding get() = _binding!!
    private val listBoxes = mutableListOf<Box>()
    private var qty: Int = 0

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
        binding.edtNumberOfCompleted.setText(qty.toString())

        setupRecyclerView()
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
                Log.d(TAG, "Box removed at position $position. Total: ${listBoxes.size}")
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
            autoDistribute()
        }

        // Print button
        binding.btnCreate.setOnClickListener {
            printLabels()
        }
    }

    private fun addBox() {
        val input = binding.edtProductsPerBox.text.toString()

        if (input.isBlank()) {
            Log.w(TAG, "Input is blank")
            return
        }

        val productsPerBox = input.toIntOrNull()

        if (productsPerBox == null || productsPerBox <= 0) {
            Log.w(TAG, "Invalid input: $input")
            return
        }

        // Tìm box đã tồn tại
        val existingIndex = listBoxes.indexOfFirst { it.numberBox == productsPerBox }

        if (existingIndex != -1) {
            // ✅ Box đã tồn tại -> tăng count
            listBoxes[existingIndex].count += 1
            packingAdapter.notifyItemChanged(existingIndex)
            Log.d(TAG, "Updated box: $productsPerBox, count: ${listBoxes[existingIndex].count}")
        } else {
            // ✅ Box mới -> thêm vào list
            val newBox = Box(
                numberBox = productsPerBox,
                count = 1
            )
            listBoxes.add(newBox)
            packingAdapter.notifyItemInserted(listBoxes.size - 1)

            // ✅ SCROLL ĐẾN ITEM MỚI
            binding.rvBoxes.smoothScrollToPosition(listBoxes.size - 1)

            Log.d(TAG, "Added new box: $productsPerBox. Total: ${listBoxes.size}")
        }

        // Clear input
        binding.edtProductsPerBox.text?.clear()
    }

    private fun autoDistribute() {
        // TODO: Implement auto distribution logic
        Log.d(TAG, "Auto distribute clicked")
    }

    private fun printLabels() {
        // TODO: Implement print labels logic
        Log.d(TAG, "Print labels clicked. Total boxes: ${listBoxes.size}")
    }
}