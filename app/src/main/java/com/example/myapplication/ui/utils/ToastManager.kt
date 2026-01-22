//package com.example.myapplication.ui.utils
//
//import android.content.Context
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import com.example.myapplication.R
//
//object ToastManager {
//    private var currentToast: Toast? = null
//
//    enum class ToastType {
//        SUCCESS, ERROR, WARNING, INFO, DEFAULT
//    }
//
//    /**
//     * Hiển thị Toast ngắn
//     */
//    fun showShort(context: Context, message: String) {
//        showToast(context, message, Toast.LENGTH_SHORT, ToastType.DEFAULT)
//    }
//
//    /**
//     * Hiển thị Toast dài
//     */
//    fun showLong(context: Context, message: String) {
//        showToast(context, message, Toast.LENGTH_LONG, ToastType.DEFAULT)
//    }
//
//    /**
//     * Hiển thị Toast thành công
//     */
//    fun showSuccess(context: Context, message: String) {
//        showToast(context, message, Toast.LENGTH_SHORT, ToastType.SUCCESS)
//    }
//
//    /**
//     * Hiển thị Toast lỗi
//     */
//    fun showError(context: Context, message: String) {
//        showToast(context, message, Toast.LENGTH_LONG, ToastType.ERROR)
//    }
//
//    /**
//     * Hiển thị Toast cảnh báo
//     */
//    fun showWarning(context: Context, message: String) {
//        showToast(context, message, Toast.LENGTH_SHORT, ToastType.WARNING)
//    }
//
//    /**
//     * Hiển thị Toast thông tin
//     */
//    fun showInfo(context: Context, message: String) {
//        showToast(context, message, Toast.LENGTH_SHORT, ToastType.INFO)
//    }
//
//    /**
//     * Hiển thị toast với resource string
//     */
//    fun showShort(context: Context, messageResId: Int) {
//        showShort(context, context.getString(messageResId))
//    }
//
//    fun showLong(context: Context, messageResId: Int) {
//        showLong(context, context.getString(messageResId))
//    }
//
//    /**
//     * Hàm chính để hiển thị toast với custom layout
//     */
//    private fun showToast(context: Context, message: String, duration: Int, type: ToastType) {
//        cancelCurrentToast()
//
//        // Tạo custom toast
//        val inflater = LayoutInflater.from(context)
//        val layout = inflater.inflate(R.layout.custom_toast, null)
//
//        val icon = layout.findViewById<ImageView>(R.id.toast_icon)
//        val text = layout.findViewById<TextView>(R.id.toast_message)
//
//        text.text = message
//
//        // Set icon và background theo loại
//        when (type) {
//            ToastType.SUCCESS -> {
//                layout.setBackgroundResource(R.drawable.toast_success_background)
//                icon.setImageResource(android.R.drawable.ic_menu_info_details)
//                icon.setColorFilter(android.graphics.Color.WHITE)
//            }
//            ToastType.ERROR -> {
//                layout.setBackgroundResource(R.drawable.toast_error_background)
//                icon.setImageResource(android.R.drawable.ic_dialog_alert)
//                icon.setColorFilter(android.graphics.Color.WHITE)
//            }
//            ToastType.WARNING -> {
//                layout.setBackgroundResource(R.drawable.toast_warning_background)
//                icon.setImageResource(android.R.drawable.ic_dialog_info)
//                icon.setColorFilter(android.graphics.Color.WHITE)
//            }
//            ToastType.INFO -> {
//                layout.setBackgroundResource(R.drawable.toast_info_background)
//                icon.setImageResource(android.R.drawable.ic_menu_info_details)
//                icon.setColorFilter(android.graphics.Color.WHITE)
//            }
//            ToastType.DEFAULT -> {
//                layout.setBackgroundResource(R.drawable.toast_background)
//                icon.setImageResource(android.R.drawable.ic_menu_info_details)
//                icon.setColorFilter(android.graphics.Color.WHITE)
//            }
//        }
//
//        currentToast = Toast(context).apply {
//            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
//            this.duration = duration
//            view = layout
//        }
//
//        currentToast?.show()
//    }
//
//    private fun cancelCurrentToast() {
//        currentToast?.cancel()
//        currentToast = null
//    }
//}