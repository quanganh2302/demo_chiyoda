package com.example.myapplication.ui.activity_home

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.ui.fragment_inspect.CreateMasterLabelFragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnKiemHang = view.findViewById<Button>(R.id.btnKiemHang)
        val btnKiemNhan = view.findViewById<Button>(R.id.btnKiemNhan)

        // ===== KIỂM HÀNG (không truyền dữ liệu) =====
        btnKiemHang.setOnClickListener {
            openCreateMasterLabel()
        }

        // ===== KIỂM NHẬN (có sẵn WO) =====
        btnKiemNhan.setOnClickListener {
            openCreateMasterLabelWithWono("WO-2026-001")
        }
    }

    // ================= NAVIGATION =================

    private fun openCreateMasterLabel() {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                CreateMasterLabelFragment()
            )
            .addToBackStack(null)
            .commit()
    }

    private fun openCreateMasterLabelWithWono(wono: String) {
        val fragment = CreateMasterLabelFragment().apply {
            arguments = Bundle().apply {
                putString(CreateMasterLabelFragment.EXTRA_WONO, wono)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
