package com.example.myapplication.ui.activity_home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.ui.activity_inspect.CreateMasterLabelActivity


class HomeFragment : Fragment(R.layout.fragment_home){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnKiemHang = view.findViewById<Button>(R.id.btnKiemHang)

        val btnKiemNhan = view.findViewById<Button>(R.id.btnKiemNhan)


        btnKiemHang.setOnClickListener {
            val intent = Intent(
                requireContext(),
                CreateMasterLabelActivity::class.java
            )
            startActivity(intent)
        }

        btnKiemNhan.setOnClickListener {

            val hardCodeWono = "WO-2026-001"

            val intent = Intent(
                requireContext(),
                CreateMasterLabelActivity::class.java
            )

            intent.putExtra("EXTRA_WONO", hardCodeWono)

            startActivity(intent)
        }

    }
}