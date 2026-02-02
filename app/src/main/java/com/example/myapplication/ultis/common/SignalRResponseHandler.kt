package com.example.myapplication.ultis.common

import android.app.AlertDialog
import android.content.Context
import com.example.myapplication.R
import com.example.myapplication.models.SignalRErrorCode
import com.example.myapplication.models.SignalRResponse
import com.example.myapplication.models.SubmitResponseDto
import com.example.myapplication.ui.custom.CommonDialog
import com.example.myapplication.ui.custom.DialogType
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


/**
 * Utility class để xử lý và hiển thị SignalR response
 *
 * ✅ UPDATED: Thêm connection dialogs
 */
object SignalRResponseHandler {
    private val gson = Gson()

    // ═══════════════════════════════════════════════════════
    // EXISTING METHODS (giữ nguyên)
    // ═══════════════════════════════════════════════════════

    /**
     * Parse JSON string thành SignalRResponse
     */
    fun parseResponse(jsonString: String): SignalRResponse? {
        return try {
            val map = gson.fromJson(jsonString, Map::class.java)
            val success = map["success"] as? Boolean ?: false

            if (success) {
                gson.fromJson(jsonString, SignalRResponse.Success::class.java)
            } else {
                gson.fromJson(jsonString, SignalRResponse.Error::class.java)
            }
        } catch (e: JsonSyntaxException) {
            println("❌ Error parsing SignalR response: ${e.message}")
            null
        }
    }

    /**
     * Hiển thị dialog thành công
     */
    fun showSuccessDialog(
        context: Context,
        response: SignalRResponse.Success,
        onDismiss: (() -> Unit)? = null
    ) {
        val formattedTime = try {
            val zonedDateTime = ZonedDateTime.parse(response.estimatedStartTime)
            zonedDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        } catch (e: Exception) {
            response.estimatedStartTime
        }

        val title = context.getString(R.string.signalr_submit_success_title)
        val message = context.getString(
            R.string.signalr_submit_success_message,
            response.jobId,
            response.wono,
            response.queuePosition,
            formattedTime
        )

        val dialog = CommonDialog(
            context = context,
            dialogType = DialogType.SUCCESS,
            title = title,
            message = message,
            onButtonClick = { onDismiss?.invoke() }
        )
        dialog.show()

        println("═══════════════════════════════════")
        println("✅ SignalR Submit SUCCESS")
        println("Job ID: ${response.jobId}")
        println("WO No: ${response.wono}")
        println("Queue Position: ${response.queuePosition}")
        println("Estimated Start Time: ${response.estimatedStartTime}")
        println("═══════════════════════════════════")
    }

    /**
     * Hiển thị dialog lỗi
     */
    fun showErrorDialog(
        context: Context,
        response: SignalRResponse.Error,
        onDismiss: (() -> Unit)? = null
    ) {
        val title = context.getString(R.string.signalr_error_title)

        val message = when (response.errorCode) {
            SignalRErrorCode.TOMATO_NOT_RUNNING -> {
                context.getString(R.string.signalr_error_tomato_not_running)
            }
            SignalRErrorCode.SYSTEM_PAUSED -> {
                context.getString(
                    R.string.signalr_error_system_paused,
                    response.systemStatus ?: "Unknown"
                )
            }
            SignalRErrorCode.INVALID_INPUT -> {
                context.getString(
                    R.string.signalr_error_invalid_input,
                    response.message
                )
            }
            SignalRErrorCode.INTERNAL_ERROR -> {
                context.getString(
                    R.string.signalr_error_internal,
                    response.message
                )
            }
            else -> {
                context.getString(
                    R.string.signalr_error_unknown,
                    response.error
                )
            }
        }

        val dialog = CommonDialog(
            context = context,
            dialogType = DialogType.WARNING,
            title = title,
            message = message,
            onButtonClick = { onDismiss?.invoke() }
        )
        dialog.show()

        println("═══════════════════════════════════")
        println("❌ SignalR Submit ERROR")
        println("Error Code: ${response.errorCode}")
        println("Error: ${response.error}")
        println("Message: ${response.message}")
        response.wono?.let { println("WO No: $it") }
        response.systemStatus?.let { println("System Status: $it") }
        println("═══════════════════════════════════")
    }

    /**
     * Hiển thị dialog khi không thể parse response
     */
    fun showParseErrorDialog(
        context: Context,
        rawResponse: String,
        onDismiss: (() -> Unit)? = null
    ) {
        val title = context.getString(R.string.signalr_error_title)
        val message = "Không thể phân tích phản hồi từ server\n\n"

        val dialog = CommonDialog(
            context = context,
            dialogType = DialogType.WARNING,
            title = title,
            message = message,
            onButtonClick = { onDismiss?.invoke() }
        )
        dialog.show()
    }

    /**
     * Xử lý và hiển thị response tự động (nhận SubmitResponseDto object)
     */
    fun handleAndShowResponse(
        context: Context,
        responseDto: SubmitResponseDto,
        onSuccess: ((SignalRResponse.Success) -> Unit)? = null,
        onError: ((SignalRResponse.Error) -> Unit)? = null
    ) {
        when (val response = responseDto.toSignalRResponse()) {
            is SignalRResponse.Success -> {
                showSuccessDialog(context, response) {
                    onSuccess?.invoke(response)
                }
            }
            is SignalRResponse.Error -> {
                showErrorDialog(context, response) {
                    onError?.invoke(response)
                }
            }
        }
    }

    /**
     * Deprecated: Dùng overload nhận SubmitResponseDto thay vì String
     */
    @Deprecated("Use overload with SubmitResponseDto parameter instead",
        ReplaceWith("handleAndShowResponse(context, responseDto, onSuccess, onError)"))
    fun handleAndShowResponse(
        context: Context,
        jsonString: String,
        onSuccess: ((SignalRResponse.Success) -> Unit)? = null,
        onError: ((SignalRResponse.Error) -> Unit)? = null,
        onParseError: (() -> Unit)? = null
    ) {
        when (val response = parseResponse(jsonString)) {
            is SignalRResponse.Success -> {
                showSuccessDialog(context, response) {
                    onSuccess?.invoke(response)
                }
            }
            is SignalRResponse.Error -> {
                showErrorDialog(context, response) {
                    onError?.invoke(response)
                }
            }
            null -> {
                showParseErrorDialog(context, jsonString) {
                    onParseError?.invoke()
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // ➕ NEW: CONNECTION DIALOGS
    // ═══════════════════════════════════════════════════════

    /**
     * ➕ Hiển thị dialog "Connecting..."
     * Gọi trong MainActivity khi bắt đầu connect
     *
     * @return AlertDialog instance để có thể dismiss sau
     */
    fun showConnectingDialog(context: Context): AlertDialog {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Connecting")
            .setMessage("Connecting to RPA system...\nPlease wait.")
            .setCancelable(false)
            .create()

        dialog.show()
        return dialog
    }

    /**
     * ➕ Hiển thị dialog khi connection failed
     *
     * @param error Error message
     * @param onRetry Callback khi user nhấn Retry
     * @param onCancel Callback khi user nhấn Cancel (optional)
     */
    fun showConnectionErrorDialog(
        context: Context,
        error: String,
        onRetry: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        val message = buildString {
            append("Cannot connect to RPA system.\n\n")
            append("Error: $error\n\n")
            append("Please check:\n")
            append("• Your internet connection\n")
            append("• Server status\n")
            append("• Network settings")
        }

        val dialog = CommonDialog(
            context = context,
            dialogType = DialogType.WARNING,
            title = "Connection Failed",
            message = message,
            buttonText = "Retry",
            onButtonClick = onRetry,
            cancelable = true,
            onCancel = onCancel
        )
        dialog.show()
    }

    /**
     * ➕ Hiển thị dialog khi connection bị mất
     *
     * @param isReconnecting True nếu đang auto-reconnect
     */
    fun showConnectionLostDialog(
        context: Context,
        isReconnecting: Boolean = true
    ) {
        val message = if (isReconnecting) {
            "Connection to RPA system was lost.\n\n" +
                    "⏳ Reconnecting automatically...\n\n" +
                    "Please wait."
        } else {
            "Connection to RPA system was lost.\n\n" +
                    "Please check your network and try again."
        }

        val dialog = CommonDialog(
            context = context,
            dialogType = DialogType.WARNING,
            title = "Connection Lost",
            message = message,
            buttonText = "OK",
            onButtonClick = {}
        )
        dialog.show()
    }

    /**
     * ➕ Hiển thị dialog reconnecting progress
     *
     * @param attempt Current attempt number
     * @param maxAttempts Maximum attempts
     */
    fun showReconnectingDialog(
        context: Context,
        attempt: Int,
        maxAttempts: Int
    ): AlertDialog {
        val message = "Connection lost. Reconnecting...\n\n" +
                "Attempt $attempt of $maxAttempts\n\n" +
                "Please wait."

        val dialog = AlertDialog.Builder(context)
            .setTitle("Reconnecting")
            .setMessage(message)
            .setCancelable(false)
            .create()

        dialog.show()
        return dialog
    }

    /**
     * Tạo custom dialog với layout tùy chỉnh (giữ nguyên)
     */
    fun showCustomDialog(
        context: Context,
        title: String,
        message: String,
        isSuccess: Boolean = true,
        onDismiss: (() -> Unit)? = null
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            onDismiss?.invoke()
        }
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()
    }
}
