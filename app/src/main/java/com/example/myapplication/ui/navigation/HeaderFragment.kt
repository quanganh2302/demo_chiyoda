package com.example.myapplication.ui.navigation

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.ui.utils.LanguageManager

class HeaderFragment : Fragment(R.layout.layout_header) {

    interface HeaderListener {
        fun onMenuClicked()
        fun onLanguageChanged(lang: String)
    }

    private var listener: HeaderListener? = null

    // ===== Views =====
    private lateinit var btnMenu: ImageButton
    private lateinit var showFlagEn: View
    private lateinit var showFlagVi: View
    private lateinit var underlineEn: View
    private lateinit var underlineVi: View

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? HeaderListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // ===== Bind views =====
        btnMenu = view.findViewById(R.id.btnMenu)
        showFlagEn = view.findViewById(R.id.showFlagEn)
        showFlagVi = view.findViewById(R.id.showFlagVi)
        underlineEn = view.findViewById(R.id.underlineEn)
        underlineVi = view.findViewById(R.id.underlineVi)

        // ===== Init UI state =====
        val currentLang = LanguageManager.getLanguage(requireContext())
        updateLanguageUI(currentLang)

        // ===== Menu =====
        btnMenu.setOnClickListener {
            listener?.onMenuClicked()
        }

        // ===== Language click =====
        showFlagEn.setOnClickListener {
            if (currentLang != "en") {
                listener?.onLanguageChanged("en")
            }
        }

        showFlagVi.setOnClickListener {
            if (currentLang != "vi") {
                listener?.onLanguageChanged("vi")
            }
        }
    }

    private fun updateLanguageUI(lang: String) {
        when (lang) {
            "en" -> {
                underlineEn.visibility = View.VISIBLE
                underlineVi.visibility = View.GONE

                showFlagEn.alpha = 1.0f
                showFlagVi.alpha = 0.6f
            }
            "vi" -> {
                underlineVi.visibility = View.VISIBLE
                underlineEn.visibility = View.GONE

                showFlagVi.alpha = 1.0f
                showFlagEn.alpha = 0.6f
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val currentLang = LanguageManager.getLanguage(requireContext())
        updateLanguageUI(currentLang)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}