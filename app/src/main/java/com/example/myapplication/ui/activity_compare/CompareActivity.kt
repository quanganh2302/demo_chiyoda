package com.example.myapplication.ui.activity_compare

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCompareLabelBinding
import com.example.myapplication.models.MasterLabelData
import com.example.myapplication.ui.activity_inspect.CreateMasterLabelActivity
import com.example.myapplication.ui.adapter.RecyclerViewAdapter
import com.example.myapplication.ui.base.BaseActivity
import com.rejowan.cutetoast.CuteToast

class CompareActivity : BaseActivity() {

    override fun hasDrawer(): Boolean = false

    private lateinit var binding: ActivityCompareLabelBinding
    private lateinit var adapter: RecyclerViewAdapter
    private val productList = mutableListOf<MasterLabelData>()

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

    // ================= RECYCLERVIEW =================
    private fun setupRecyclerView() {
        adapter = RecyclerViewAdapter(productList) { position ->
            if (position in productList.indices) {
                productList.removeAt(position)
                adapter.notifyItemRemoved(position)

                updateScannedLabelCount()

                if (productList.isEmpty()) {
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

        // Demo data (sau này thay bằng API / scan)
        productList.add(
            MasterLabelData("A01", "WO001", "01/01/2026", 10)
        )
        productList.add(
            MasterLabelData("A02", "WO001", "01/01/2026", 20)
        )
        productList.add(
            MasterLabelData("A03", "WO001", "01/01/2026", 20)
        )

        adapter.notifyDataSetChanged()

        updateScannedLabelCount()
    }
    private fun updateScannedLabelCount() {
        binding.bfRecyclerViewItems.text = getString(
            R.string.scanned_label_count_format,
            productList.size
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
}
