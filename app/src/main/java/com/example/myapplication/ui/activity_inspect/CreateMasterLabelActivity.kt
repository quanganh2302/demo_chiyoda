package com.example.myapplication.ui.activity_inspect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.myapplication.R
import com.example.myapplication.models.MasterLabelData
import com.example.myapplication.ui.activity_compare.CompareActivity
import com.example.myapplication.ui.base.BaseActivity
import com.example.myapplication.ui.custom.DateInputView
import com.google.android.material.textfield.TextInputEditText
import com.rejowan.cutetoast.CuteToast

class CreateMasterLabelActivity : BaseActivity() {

    override fun hasDrawer(): Boolean = false

    companion object {
        const val EXTRA_WONO = "EXTRA_WONO"
        const val EXTRA_DATE = "EXTRA_DATE"
        const val EXTRA_QTY = "EXTRA_QTY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_master_label)

        val edtWoNo = findViewById<TextInputEditText>(R.id.edtWoNo)
        val edtQty = findViewById<TextInputEditText>(R.id.edtQty)
        val dateInputView = findViewById<DateInputView>(R.id.dateInputView)

        val wono = intent.getStringExtra("EXTRA_WONO") ?: ""

        edtWoNo.setText(wono)


        val btnCreate = findViewById<Button>(R.id.btnCreate)

        btnCreate.setOnClickListener {
            try {
                val date = dateInputView.getDate()
                val qtyText = edtQty.text?.toString() ?: ""
                val qty = qtyText.toIntOrNull() ?: 0

                if (date.isBlank()) {
                    CuteToast.ct(
                        this,
                        getString(R.string.error_date_empty),
                        CuteToast.LENGTH_SHORT,
                        CuteToast.ERROR,
                        true
                    ).show()
                    return@setOnClickListener
                }

                if (qty <= 0) {
                    CuteToast.ct(
                        this,
                        getString(R.string.error_qty_invalid),
                        CuteToast.LENGTH_SHORT,
                        CuteToast.ERROR,
                        true
                    ).show()
                    return@setOnClickListener
                }

                val masterLabelData = MasterLabelData(
                    productCode = "DEMO_PRODUCT",
                    wono = wono,
                    date = date,
                    qty = qty
                )

                Log.d("MasterLabelData", masterLabelData.toString())


                val intent = Intent(this, CompareActivity::class.java)
                intent.putExtra(EXTRA_WONO, masterLabelData.wono)
                intent.putExtra(EXTRA_DATE, masterLabelData.date)
                intent.putExtra(EXTRA_QTY, masterLabelData.qty)
                startActivity(intent)

            } catch (ex: Exception) {

                Log.e("CreateMasterLabel", ex.message ?: "Unknown error")

                CuteToast.ct(
                    this,
                    getString(R.string.unexpected_error),
                    CuteToast.LENGTH_SHORT,
                    CuteToast.ERROR,
                    true
                ).show()
            }
        }


    }


}
