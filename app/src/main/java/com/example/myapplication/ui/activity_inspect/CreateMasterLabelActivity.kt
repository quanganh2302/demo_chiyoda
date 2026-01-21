package com.example.myapplication.ui.activity_inspect

import android.os.Bundle
import com.example.myapplication.R
import com.example.myapplication.ui.base.BaseActivity

class CreateMasterLabelActivity : BaseActivity() {

    override fun hasDrawer(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_master_label)

    }

}
