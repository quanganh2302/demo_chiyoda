package com.example.myapplication.ui.activity_compare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.myapplication.R
import com.example.myapplication.ui.activity_inspect.CreateMasterLabelActivity
import com.example.myapplication.ui.base.BaseActivity

class CompareActivity : BaseActivity() {
    override fun hasDrawer(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare_label)

        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnCompare = findViewById<Button>(R.id.btnCompare)


        btnBack.setOnClickListener {
            val intent = Intent(this, CreateMasterLabelActivity::class.java)
            startActivity(intent)
        }
    }


}