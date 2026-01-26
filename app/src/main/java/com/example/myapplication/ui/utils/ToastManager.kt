package com.example.myapplication.ui.utils

import android.content.Context
import com.rejowan.cutetoast.CuteToast

object ToastManager {

    // ===== Base =====
    private fun show(
        context: Context,
        message: String,
        type: Int,
        duration: Int = CuteToast.LENGTH_SHORT,
        withIcon: Boolean = true
    ) {
        CuteToast.ct(
            context.applicationContext, // tránh leak Activity
            message,
            duration,
            type,
            withIcon
        ).show()
    }

    // ===== Public APIs =====

    fun info(context: Context, message: String) {
        show(context, message, CuteToast.INFO)
    }

    fun error(context: Context, message: String) {
        show(context, message, CuteToast.ERROR, CuteToast.LENGTH_LONG)
    }

    fun success(context: Context, message: String) {
        show(context, message, CuteToast.SUCCESS)
    }

    fun warning(context: Context, message: String) {
        show(context, message, CuteToast.WARN)
    }
}