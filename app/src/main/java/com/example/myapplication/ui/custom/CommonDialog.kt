package com.example.myapplication.ui.custom

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.R

enum class DialogType {
    SUCCESS,
    WARNING
}

class CommonDialog(
    private val context: Context,
    private val dialogType: DialogType,
    private val title: String,
    private val message: String,
    private val buttonText: String? = null,
    private val onButtonClick: (() -> Unit)? = null,
    private val cancelable: Boolean = true,
    private val onCancel: (() -> Unit)? = null
) {
    private var dialog: AlertDialog? = null

    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_common, null)

        val flIconContainer = dialogView.findViewById<FrameLayout>(R.id.fl_icon_container)
        val ivIcon = dialogView.findViewById<ImageView>(R.id.iv_dialog_icon)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_dialog_message)
        val btnAction = dialogView.findViewById<Button>(R.id.btn_dialog_action)

        // Set icon và background theo type
        when (dialogType) {
            DialogType.SUCCESS -> {
                flIconContainer.setBackgroundResource(R.drawable.icon_background_success)
                ivIcon.setImageResource(R.drawable.ic_check_circle)
            }
            DialogType.WARNING -> {
                flIconContainer.setBackgroundResource(R.drawable.icon_background_warning)
                ivIcon.setImageResource(R.drawable.ic_warning_circle)
            }
        }

        tvTitle.text = title
        tvMessage.text = message

        // Set button text (default là "Back" từ string resource)
        buttonText?.let { btnAction.text = it }

        btnAction.setOnClickListener {
            onButtonClick?.invoke()
            dismiss()
        }

        dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(cancelable)
            .setOnCancelListener {
                onCancel?.invoke()
            }
            .create()

        dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    /**
     * Check nếu dialog đang hiển thị
     */
    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }
}