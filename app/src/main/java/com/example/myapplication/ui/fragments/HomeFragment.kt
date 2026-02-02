package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.models.SystemStatusUpdate
import com.example.myapplication.service.signalR.SignalRManager
import com.example.myapplication.service.signalR.SimpleEventListener
import com.example.myapplication.ui.utils.ToastManager

class HomeFragment : Fragment(R.layout.fragment_home) {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private var btnKiemHang: Button? = null
    private var btnKiemNhan: Button? = null

    private val signalREventListener = object : SimpleEventListener() {
        override fun onSystemStatusUpdate(update: SystemStatusUpdate) {
            activity?.runOnUiThread {
                Log.d(TAG, "📡 SystemStatusUpdate received: ${update.status}, canSubmit=${update.canSubmitTasks}")
                updateConnectionStatus(update)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnKiemHang = view.findViewById(R.id.btnKiemHang)
        btnKiemNhan = view.findViewById(R.id.btnKiemNhan)

        SignalRManager.registerEventListener(signalREventListener)

        btnKiemHang?.setOnClickListener {
            openCreateMasterLabel()
        }

        btnKiemNhan?.setOnClickListener {
            ToastManager.info(requireContext(), "Feature coming soon")
        }

        checkAndUpdateButtonState()
    }

    /**
     * ✅ PUBLIC method để MainActivity gọi
     */
    fun refreshConnectionStatus() {
        Log.d(TAG, "🔄 refreshConnectionStatus() called from MainActivity")
        checkAndUpdateButtonState()
    }

    private fun checkAndUpdateButtonState() {
        val isConnected = SignalRManager.isConnected()

        Log.d(TAG, "🔍 Checking button state: isConnected=$isConnected")

        if (isConnected) {
            btnKiemHang?.isEnabled = true
            btnKiemNhan?.isEnabled = true
            btnKiemHang?.alpha = 1.0f
            btnKiemNhan?.alpha = 1.0f
            Log.d(TAG, "✅ Buttons ENABLED (connected)")
        } else {
            btnKiemHang?.isEnabled = false
            btnKiemNhan?.isEnabled = false
            btnKiemHang?.alpha = 0.5f
            btnKiemNhan?.alpha = 0.5f
            Log.d(TAG, "❌ Buttons DISABLED (not connected)")
        }
    }

    private fun updateConnectionStatus(status: SystemStatusUpdate) {
        val isConnected = SignalRManager.isConnected()
        val canSubmit = status.canSubmitTasks

        Log.d(TAG, "🔄 Status update: connected=$isConnected, canSubmit=$canSubmit")

        val buttonsEnabled = isConnected && canSubmit

        btnKiemHang?.isEnabled = buttonsEnabled
        btnKiemNhan?.isEnabled = buttonsEnabled

        val alpha = if (buttonsEnabled) 1.0f else 0.5f
        btnKiemHang?.alpha = alpha
        btnKiemNhan?.alpha = alpha

        Log.d(TAG, "🎨 Buttons updated: enabled=$buttonsEnabled, alpha=$alpha")

        if (!canSubmit && isConnected) {
            ToastManager.warning(
                requireContext(),
                status.getDisplayMessage(isVietnamese = false)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "📍 onResume - re-checking button state")
        checkAndUpdateButtonState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SignalRManager.unregisterEventListener(signalREventListener)
        btnKiemHang = null
        btnKiemNhan = null
    }

    private fun openCreateMasterLabel() {
        if (!SignalRManager.isConnected()) {
            ToastManager.error(
                requireContext(),
                "Not connected to RPA system. Please wait..."
            )
            return
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CreateMasterLabelFragment())
            .addToBackStack(null)
            .commit()
    }
}