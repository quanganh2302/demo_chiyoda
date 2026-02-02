package com.example.myapplication.manager

import android.content.Context
import com.example.myapplication.R
import com.example.myapplication.models.SystemStatusUpdate
import com.example.myapplication.ui.custom.CommonDialog
import com.example.myapplication.ui.custom.DialogType
import java.util.Locale

/**
 * Singleton Manager để quản lý trạng thái hệ thống RPA
 * Theo dõi SystemStatusUpdate từ SignalR
 */
object RpaErrorManager {
    
    // Trạng thái RPA system (KHÔNG khóa toàn bộ app)
    @Volatile
    private var canSubmitTasks: Boolean = true
    
    // Thông tin status update gần nhất
    @Volatile
    private var latestStatusUpdate: SystemStatusUpdate? = null
    
    // Thông tin lỗi (giữ lại cho backward compatibility)
    @Volatile
    private var hasRpaError: Boolean = false
    
    @Volatile
    private var errorMessage: String? = null
    
    // Listener để notify UI khi có status update
    private val errorListeners = mutableListOf<RpaErrorListener>()
    private val statusListeners = mutableListOf<SystemStatusListener>()
    
    /**
     * Kiểm tra có lỗi RPA không (backward compatibility)
     */
    fun hasError(): Boolean = hasRpaError
    
    /**
     * Lấy message lỗi (backward compatibility)
     */
    fun getErrorMessage(): String? = errorMessage
    
    /**
     * Set trạng thái lỗi RPA cũ (backward compatibility)
     * @param error true nếu có lỗi, false nếu clear lỗi
     * @param message Thông báo lỗi
     */
    fun setRpaError(error: Boolean, message: String? = null) {
        hasRpaError = error
        errorMessage = message
        
        if (error) {
            // Notify tất cả listeners
            notifyErrorListeners(message ?: "RPA Error occurred")
        }
        
        println("═══════════════════════════════")
        println("RpaErrorManager - Error State Changed:")
        println("Has Error: $hasRpaError")
        println("Message: $errorMessage")
        println("═══════════════════════════════")
    }
    
    /**
     * ⚡ XỬ LÝ SYSTEM STATUS UPDATE MỚI
     * @param statusUpdate Thông tin status từ SignalR
     */
    fun updateSystemStatus(statusUpdate: SystemStatusUpdate, context: Context? = null) {
        latestStatusUpdate = statusUpdate
        canSubmitTasks = statusUpdate.canSubmitTasks
        
        println("═══════════════════════════════")
        println("📡 SYSTEM STATUS UPDATE RECEIVED")
        println("Type: ${statusUpdate.type}")
        println("Status: ${statusUpdate.status}")
        println("Can Submit Tasks: ${statusUpdate.canSubmitTasks}")
        println("Queue Length: ${statusUpdate.queueLength}")
        println("Message: ${statusUpdate.message}")
        println("═══════════════════════════════")
        
        // Notify tất cả status listeners
        notifyStatusListeners(statusUpdate)
        
        // Hiển thị thông báo nếu có context
        context?.let {
            showSystemStatusDialog(it, statusUpdate)
        }
    }
    
    /**
     * Kiểm tra có thể submit task không
     */
    fun canSubmitTasks(): Boolean = canSubmitTasks
    
    /**
     * Lấy status update gần nhất
     */
    fun getLatestStatus(): SystemStatusUpdate? = latestStatusUpdate
    
    /**
     * Clear lỗi RPA (nếu cần reset)
     */
    fun clearError() {
        setRpaError(false, null)
    }
    
    /**
     * Đăng ký listener để nhận thông báo khi có lỗi (backward compatibility)
     */
    fun registerErrorListener(listener: RpaErrorListener) {
        if (!errorListeners.contains(listener)) {
            errorListeners.add(listener)
        }
    }
    
    /**
     * Hủy đăng ký listener (backward compatibility)
     */
    fun unregisterErrorListener(listener: RpaErrorListener) {
        errorListeners.remove(listener)
    }
    
    /**
     * Notify tất cả error listeners (backward compatibility)
     */
    private fun notifyErrorListeners(message: String) {
        errorListeners.forEach { listener ->
            listener.onRpaError(message)
        }
    }
    
    /**
     * Đăng ký listener để nhận SystemStatusUpdate
     */
    fun registerStatusListener(listener: SystemStatusListener) {
        if (!statusListeners.contains(listener)) {
            statusListeners.add(listener)
        }
    }
    
    /**
     * Hủy đăng ký status listener
     */
    fun unregisterStatusListener(listener: SystemStatusListener) {
        statusListeners.remove(listener)
    }
    
    /**
     * Notify tất cả status listeners
     */
    private fun notifyStatusListeners(statusUpdate: SystemStatusUpdate) {
        statusListeners.forEach { listener ->
            listener.onSystemStatusUpdate(statusUpdate)
        }
    }
    
    /**
     * Hiển thị dialog lỗi RPA (backward compatibility)
     */
    fun showRpaErrorDialog(context: Context) {
        val message = errorMessage ?: "RPA system error. Please contact administrator."
        
        CommonDialog(
            context = context,
            dialogType = DialogType.WARNING,
            title = "RPA Error",
            message = message,
            onButtonClick = {
                // Không làm gì, chỉ đóng dialog
            }
        ).show()
    }
    
    /**
     * Hiển thị dialog SystemStatusUpdate (song ngữ)
     */
    fun showSystemStatusDialog(context: Context, statusUpdate: SystemStatusUpdate) {
        val isVietnamese = Locale.getDefault().language == "vi"
        
        val dialogType = if (statusUpdate.isGoodStatus()) {
            DialogType.SUCCESS
        } else {
            DialogType.WARNING
        }
        
        CommonDialog(
            context = context,
            dialogType = dialogType,
            title = statusUpdate.getTitle(isVietnamese),
            message = statusUpdate.getDisplayMessage(isVietnamese),
            onButtonClick = {
                // Đóng dialog
            }
        ).show()
    }
    
    /**
     * Kiểm tra có thể truy cập QcChiyodaFragment không
     * Bây giờ chỉ check canSubmitTasks thay vì hasError
     * @return true nếu có thể truy cập, false nếu bị khóa
     */
    fun canAccessQcChiyoda(): Boolean {
        // Check cả 2: backward compatibility (hasRpaError) và logic mới (canSubmitTasks)
        return !hasRpaError && canSubmitTasks
    }
}

/**
 * Interface để listen sự kiện lỗi RPA (backward compatibility)
 */
interface RpaErrorListener {
    fun onRpaError(message: String)
}

/**
 * Interface để listen SystemStatusUpdate
 */
interface SystemStatusListener {
    fun onSystemStatusUpdate(statusUpdate: SystemStatusUpdate)
}
