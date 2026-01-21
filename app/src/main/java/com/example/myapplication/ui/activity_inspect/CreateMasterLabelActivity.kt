package com.example.myapplication.ui.activity_inspect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.myapplication.R
import com.example.myapplication.ui.activity_compare.CompareActivity
import com.example.myapplication.ui.base.BaseActivity

class CreateMasterLabelActivity : BaseActivity() {

    override fun hasDrawer(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_master_label)

        val btnCreate = findViewById<Button>(R.id.btnCreate)

        btnCreate.setOnClickListener {
            val intent = Intent(this, CompareActivity::class.java)
            startActivity(intent)
        }
    }


}
