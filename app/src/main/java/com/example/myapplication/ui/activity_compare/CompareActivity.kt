package com.example.myapplication.ui.activity_compare

import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.MasterLabelData
import com.example.myapplication.ui.adapter.RecyclerViewAdapter
import com.example.myapplication.ui.base.BaseActivity
//import com.example.myapplication.ui.utils.ToastManager
import com.rejowan.cutetoast.CuteToast

class CompareActivity : BaseActivity() {

    override fun hasDrawer(): Boolean = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerViewAdapter
    private val productList = mutableListOf<MasterLabelData>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare_label)

        recyclerView = findViewById(R.id.recyclerViewItems)

        setupRecyclerView()
        setupButtons()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = RecyclerViewAdapter(productList) { position ->
            // Kiểm tra position hợp lệ trước khi xóa
            if (position >= 0 && position < productList.size) {
                val deletedItem = productList[position]
                productList.removeAt(position)
                adapter.notifyItemRemoved(position)

                // Cập nhật lại range của các items còn lại
                if (position < productList.size) {
                    adapter.notifyItemRangeChanged(position, productList.size - position)
                }

                CuteToast.ct(
                this,
                "Đã xóa thành công",
                CuteToast.LENGTH_SHORT,
                CuteToast.SUCCESS,
                true
                ).show()

                if (productList.isEmpty()) {
                    CuteToast.ct(
                        this,
                        "Danh sách trống",
                        CuteToast.LENGTH_SHORT,
                        CuteToast.INFO,
                        true
                    ).show()
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadData() {
        productList.add(
            MasterLabelData("ABC123", "456789", "24/04/2024", 100)
        )
        productList.add(
            MasterLabelData("XYZ999", "888888", "25/04/2024", 200)
        )
        productList.add(
            MasterLabelData("XYZ111", "888888", "25/04/2024", 200)
        )

        adapter.notifyDataSetChanged()
    }

    private fun setupButtons() {
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnCompare = findViewById<Button>(R.id.btnCompare)

        btnBack.setOnClickListener {
            finish()
        }

        btnCompare.setOnClickListener {
            CuteToast.ct(
                this,
                "@string/compare_success",
                CuteToast.LENGTH_SHORT,
                CuteToast.ERROR,
                true
            ).show()
        }
    }
}