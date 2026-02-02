package com.example.myapplication.models

import com.google.gson.annotations.SerializedName

/**
 * Base response từ SignalR khi submit ChiyodaInfo
 */
sealed class SignalRResponse {
    /**
     * Response thành công
     */
    data class Success(
        @SerializedName("success") val success: Boolean = true,
        @SerializedName("jobId") val jobId: String,
        @SerializedName("wono") val wono: String,
        @SerializedName("message") val message: String,
        @SerializedName("queuePosition") val queuePosition: Int,
        @SerializedName("estimatedStartTime") val estimatedStartTime: String
    ) : SignalRResponse()

    /**
     * Response lỗi
     */
    data class Error(
        @SerializedName("success") val success: Boolean = false,
        @SerializedName("error") val error: String,
        @SerializedName("errorCode") val errorCode: String,
        @SerializedName("message") val message: String,
        @SerializedName("wono") val wono: String? = null,
        @SerializedName("systemStatus") val systemStatus: String? = null
    ) : SignalRResponse()
}

/**
 * Submit Response DTO - Nhận trực tiếp từ SignalR
 * Dùng cho cả Success và Error response
 */
data class SubmitResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("jobId") val jobId: String? = null,
    @SerializedName("wono") val wono: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("queuePosition") val queuePosition: Int? = null,
    @SerializedName("estimatedStartTime") val estimatedStartTime: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("errorCode") val errorCode: String? = null,
    @SerializedName("systemStatus") val systemStatus: String? = null
) {
    /**
     * Convert DTO thành SignalRResponse sealed class
     */
    fun toSignalRResponse(): SignalRResponse {
        return if (success && jobId != null && wono != null && message != null) {
            SignalRResponse.Success(
                success = success,
                jobId = jobId,
                wono = wono,
                message = message,
                queuePosition = queuePosition ?: 0,
                estimatedStartTime = estimatedStartTime ?: ""
            )
        } else {
            SignalRResponse.Error(
                success = false,
                error = error ?: "Unknown error",
                errorCode = errorCode ?: "INTERNAL_ERROR",
                message = message ?: "An error occurred",
                wono = wono,
                systemStatus = systemStatus
            )
        }
    }
}

/**
 * Error codes từ server
 */
object SignalRErrorCode {
    const val TOMATO_NOT_RUNNING = "TOMATO_NOT_RUNNING"
    const val SYSTEM_PAUSED = "SYSTEM_PAUSED"
    const val INVALID_INPUT = "INVALID_INPUT"
    const val INTERNAL_ERROR = "INTERNAL_ERROR"
}

/**
 * System Status Update từ SignalR
 * Thông báo trạng thái hệ thống RPA
 */
data class SystemStatusUpdate(
    @SerializedName("type") val type: String,           // "SystemPaused" | "SystemResumed" | "SystemStopped" | "SystemStarted"
    @SerializedName("message") val message: String,     // User-friendly message
    @SerializedName("timestamp") val timestamp: String, // When the change occurred
    @SerializedName("status") val status: String,       // "active" | "paused" | "stopped"
    @SerializedName("canSubmitTasks") val canSubmitTasks: Boolean, // Can Handy submit new tasks?
    @SerializedName("queueLength") val queueLength: Int // Current queue length
) {
    /**
     * Lấy title hiển thị theo ngôn ngữ
     */
    fun getTitle(isVietnamese: Boolean): String {
        return when (type) {
            "BreakTimeStarted" -> if (isVietnamese) "Thời gian nghỉ bắt đầu" else "Break Time Started"
            "BreakTimeEnd" -> if (isVietnamese) "Thời gian nghỉ kết thúc" else "Break Time Ended"
            "SystemPaused" -> if (isVietnamese) "Hệ thống tạm dừng" else "System Paused"
            "SystemResumed" -> if (isVietnamese) "Hệ thống hoạt động trở lại" else "System Resumed"
            "SystemStopped" -> if (isVietnamese) "Hệ thống dừng" else "System Stopped"
            "SystemStarted" -> if (isVietnamese) "Hệ thống khởi động" else "System Started"
            else -> if (isVietnamese) "Cập nhật hệ thống" else "System Update"
        }
    }
    
    /**
     * Lấy message hiển thị theo ngôn ngữ
     */
    fun getDisplayMessage(isVietnamese: Boolean): String {
        val baseMessage = if (isVietnamese) {
            when (type) {
                "BreakTimeStarted" -> "Hệ thống RPA đã tạm dừng."
                "BreakTimeEnd" -> "Hệ thống RPA đã hoạt động trở lại."
                "SystemPaused" -> "Hệ thống RPA đã tạm dừng."
                "SystemResumed" -> "Hệ thống RPA đã hoạt động trở lại."
                "SystemStopped" -> "Hệ thống RPA đã dừng hoàn toàn."
                "SystemStarted" -> "Hệ thống RPA đã khởi động."
                else -> "Trạng thái hệ thống đã thay đổi."
            }
        } else {
            when (type) {
                "BreakTimeStarted" -> "RPA system has been paused."
                "BreakTimeEnd" -> "RPA system has resumed."
                "SystemPaused" -> "RPA system has been paused."
                "SystemResumed" -> "RPA system has resumed."
                "SystemStopped" -> "RPA system has been stopped."
                "SystemStarted" -> "RPA system has started."
                else -> "System status has changed."
            }
        }
        
        val submitStatus = if (!canSubmitTasks) {
            if (isVietnamese) 
                "\n\n⚠️ Không thể in nhãn mới tại thời điểm này."
            else 
                "\n\n⚠️ Unable to print new labels at this time."
        } else {
            if (isVietnamese)
                "\n\n✅ Có thể in nhãn mới"
            else
                "\n\n✅ New labels can be printed."
        }
//
//        val queueInfo = if (isVietnamese)
//            "\n📋 Số task đang chờ: $queueLength"
//        else
//            "\n📋 Queue length: $queueLength"
        
        return "$baseMessage$submitStatus\n"
       // $message   queueInfo
    }
    
    /**
     * Kiểm tra có phải trạng thái tốt không
     */
    fun isGoodStatus(): Boolean {
        return type == "SystemStarted" || type == "SystemResumed"|| type == "BreakTimeEnd"
    }
}
