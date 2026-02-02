package com.example.myapplication.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.myapplication.service.signalR.ConnectionState
import com.example.myapplication.R

/**
 * Connection Status View
 *
 * Hiển thị trạng thái kết nối SignalR trong header
 *
 * States:
 * - 🟢 CONNECTED - Đã kết nối
 * - 🟡 CONNECTING - Đang kết nối
 * - 🔴 DISCONNECTED - Mất kết nối
 * - 🟡 RECONNECTING - Đang reconnect
 * - 🟠 SYSTEM_PAUSED - Connected nhưng system paused
 */
class ConnectionStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val statusIcon: ImageView
    private val statusText: TextView

    init {
        orientation = HORIZONTAL

        // Inflate layout
        LayoutInflater.from(context).inflate(
            R.layout.view_connection_status,
            this,
            true
        )

        statusIcon = findViewById(R.id.statusIcon)
        statusText = findViewById(R.id.statusText)

        // Default state
        setStatus(ConnectionState.DISCONNECTED)
    }

    /**
     * Set connection status
     *
     * @param state Connection state
     * @param message Optional custom message
     */
    fun setStatus(state: ConnectionState, message: String? = null) {
        when (state) {
            ConnectionState.CONNECTED -> {
                statusIcon.setImageResource(R.drawable.ic_status_connected)
                statusIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.green),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                statusText.text = message ?: "Connected"
                statusText.setTextColor(ContextCompat.getColor(context, R.color.green))
            }

            ConnectionState.CONNECTING -> {
                statusIcon.setImageResource(R.drawable.ic_status_connecting)
                statusIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.yellow),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                statusText.text = message ?: "Connecting..."
                statusText.setTextColor(ContextCompat.getColor(context, R.color.yellow))
            }

            ConnectionState.DISCONNECTED -> {
                statusIcon.setImageResource(R.drawable.ic_status_disconnected)
                statusIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.red),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                statusText.text = message ?: "Disconnected"
                statusText.setTextColor(ContextCompat.getColor(context, R.color.red))
            }

            ConnectionState.RECONNECTING -> {
                statusIcon.setImageResource(R.drawable.ic_status_connecting)
                statusIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.orange),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                statusText.text = message ?: "Reconnecting..."
                statusText.setTextColor(ContextCompat.getColor(context, R.color.orange))
            }

            ConnectionState.FAILED -> {
                statusIcon.setImageResource(R.drawable.ic_status_error)
                statusIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.red),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                statusText.text = message ?: "Connection Failed"
                statusText.setTextColor(ContextCompat.getColor(context, R.color.red))
            }
        }
    }

    /**
     * Set system paused status
     * Hiển thị khi connected nhưng RPA system paused
     */
    fun setSystemPaused(isPaused: Boolean, message: String? = null) {
        if (isPaused) {
            statusIcon.setImageResource(R.drawable.ic_status_warning)
            statusIcon.setColorFilter(
                ContextCompat.getColor(context, R.color.orange),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            statusText.text = message ?: "System Paused"
            statusText.setTextColor(ContextCompat.getColor(context, R.color.orange))
        } else {
            // Back to connected state
            setStatus(ConnectionState.CONNECTED)
        }
    }
}