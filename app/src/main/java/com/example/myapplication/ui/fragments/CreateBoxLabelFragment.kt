package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentCreateBoxLabelBinding
import com.example.myapplication.models.MasterLabelData
import com.example.myapplication.ui.utils.contants.BundleKeys
import com.example.myapplication.R
import com.example.myapplication.service.ScanEvent
import com.example.myapplication.ui.utils.DateUiFormatter
import java.time.LocalDate

class CreateBoxLabelFragment : Fragment() {
    companion object {
        private const val  TAG = "CreateBoxLabelFragment"
    }

    private var _binding: FragmentCreateBoxLabelBinding? = null
    private val binding get() = _binding!!

    private var masterLabel : MasterLabelData? = null

    private var selectedPackingType: String = ""


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateBoxLabelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLabelData()

        masterLabel?.let {bindMasterLabel(it)}

        setupDropdown()
        setupButton()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupLabelData(){
        val isoDate = arguments?.getString(BundleKeys.EXTRA_DATE).orEmpty()

        masterLabel = MasterLabelData(
            productCode = "",
            wono = arguments?.getString(BundleKeys.EXTRA_WONO).orEmpty(),
            date = isoDate,
            qty = arguments?.getInt(BundleKeys.EXTRA_QTY) ?: 0
        )
    }

    private fun bindMasterLabel(data: MasterLabelData) = with(binding){
        edtWoNo.setText(data.wono)

        val formattedDate = runCatching {
            val localDate = LocalDate.parse(data.date)
            DateUiFormatter.format(localDate)
        }.getOrNull()

        dateInputView.setDate(formattedDate ?: data.date)

        edtQty.setText(data.qty.toString())


    }
    private fun setupDropdown() {
        // Lấy packing types từ resources
        val packingTypes = resources.getStringArray(R.array.packing_types)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            packingTypes
        )

        binding.actvCategory.setAdapter(adapter)

        if (packingTypes.isNotEmpty()) {
            binding.actvCategory.setText(packingTypes[0], false)
            selectedPackingType = packingTypes[0]
        }
        // Handle selection
        binding.actvCategory.setOnItemClickListener { parent, _, position, _ ->
            selectedPackingType = packingTypes[position]
        }
    }
    private fun setupButton(){
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnContinue.setOnClickListener {
            createBoxLabel()
        }
    }

    private fun createBoxLabel() {
        val fragment = PrintLabelFragment().apply {
            arguments = Bundle().apply {
                putInt(BundleKeys.EXTRA_QTY, masterLabel?.qty ?: 0)
                putString(BundleKeys.EXTRA_PACKING_TYPE, selectedPackingType)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

}