package com.example.myapplication.service.signalR

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.myapplication.helper.ChiyodaInfo
import com.example.myapplication.models.SubmitResponseDto
import com.example.myapplication.models.SystemStatusUpdate
import com.example.myapplication.manager.RpaErrorManager
import com.example.myapplication.service.signalR.SignalRManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * SignalR Service
 * Wrapper cho Microsoft SignalR HubConnection
 *
 * ⚠️ IMPORTANT: Class này không nên được dùng trực tiếp
 * Dùng SignalRManager singleton thay thế
 */
class SignalRService(context: Context) {

    private val TAG = "SignalRService"

    // Store ApplicationContext để tránh memory leak
    private val appContext: Context = context.applicationContext

    private lateinit var hubConnection: HubConnection

    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()
    }

    // Callback cho submit response - nhận object thay vì String
    private var onSubmitResponseCallback: ((SubmitResponseDto) -> Unit)? = null

    // Callback cho connection state changes (internal use)
    private var onConnectionStateChanged: ((HubConnectionState) -> Unit)? = null

    /**
     * Start SignalR connection
     * ✅ FIXED: Không dùng blockingAwait() nữa
     */
    fun startConnection() {
        try {
            hubConnection = HubConnectionBuilder
                .create("http://192.168.103.30:5000/chiyodahub")
                .build()

            setupEventHandlers()
            setupConnectionCallbacks()

            hubConnection.start()
                .timeout(30, TimeUnit.SECONDS)
                .blockingAwait()  // Block và wait cho kết nối

            Log.d(TAG, "✅ Connected to SignalR hub!")
            onConnectionStateChanged?.invoke(HubConnectionState.CONNECTED)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Connection failed: ${e.message}", e)
            onConnectionStateChanged?.invoke(HubConnectionState.DISCONNECTED)
            SignalRManager.onDisconnected()
        }
    }

    /**
     * Setup all event handlers
     */
    private fun setupEventHandlers() {
        // Basic message handler
        hubConnection.on("ReceiveMessage", { user: String, message: String ->
            Log.d(TAG, "[$user]: $message")
        }, String::class.java, String::class.java)

        // ⚡ System Status Update handler
        hubConnection.on("SystemStatusUpdate", { status: SystemStatusUpdate ->
            handleSystemStatusUpdate(status)
        }, SystemStatusUpdate::class.java)

        // Submit result handlers
        hubConnection.on("SubmitResult", { response: SubmitResponseDto ->
            handleSubmitResponse(response)
        }, SubmitResponseDto::class.java)

        hubConnection.on("ChiyodaSubmitResponse", { response: SubmitResponseDto ->
            handleSubmitResponse(response)
        }, SubmitResponseDto::class.java)
    }

    /**
     * Setup connection lifecycle callbacks
     */
    private fun setupConnectionCallbacks() {
        hubConnection.onClosed { error ->
            Log.w(TAG, "⚠️ Connection closed: ${error?.message ?: "Unknown reason"}")
            onConnectionStateChanged?.invoke(HubConnectionState.DISCONNECTED)

            // Notify SignalRManager về disconnection
            SignalRManager.onDisconnected()
        }
    }

    /**
     * ⚡ XỬ LÝ SYSTEM STATUS UPDATE TỪ SERVER
     */
    private fun handleSystemStatusUpdate(statusUpdate: SystemStatusUpdate) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "📡 SYSTEM STATUS UPDATE RECEIVED")
        Log.d(TAG, "Type: ${statusUpdate.type}")
        Log.d(TAG, "Status: ${statusUpdate.status}")
        Log.d(TAG, "Message: ${statusUpdate.message}")
        Log.d(TAG, "Timestamp: ${statusUpdate.timestamp}")
        Log.d(TAG, "Can Submit: ${statusUpdate.canSubmitTasks}")
        Log.d(TAG, "═══════════════════════════════════════")

        try {
            Thread.sleep(100)

            RpaErrorManager.updateSystemStatus(statusUpdate, null)

            // Broadcast qua SignalRManager
            SignalRManager.notifySystemStatusUpdate(statusUpdate)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling SystemStatusUpdate: ${e.message}", e)
        }
    }

    /**
     * Xử lý khi nhận được response từ SubmitChiyodaInfo
     */
    private fun handleSubmitResponse(response: SubmitResponseDto) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "📥 SUBMIT RESPONSE RECEIVED")
        Log.d(TAG, "Success: ${response.success}")
        Log.d(TAG, "JobId: ${response.jobId}")
        Log.d(TAG, "WoNo: ${response.wono}")
        Log.d(TAG, "Message: ${response.message}")
        if (!response.success) {
            Log.e(TAG, "Error: ${response.error}")
            Log.e(TAG, "ErrorCode: ${response.errorCode}")
        }
        Log.d(TAG, "═══════════════════════════════════════")

        // Gọi callback
        onSubmitResponseCallback?.invoke(response)
    }

    /**
     * Đăng ký callback để nhận response từ server
     * @param callback Nhận SubmitResponseDto object
     */
    fun setOnSubmitResponseListener(callback: (SubmitResponseDto) -> Unit) {
        onSubmitResponseCallback = callback
    }

    /**
     * Xóa callback
     */
    fun clearOnSubmitResponseListener() {
        onSubmitResponseCallback = null
    }

    /**
     * Set connection state change callback (internal use)
     */
    fun setOnConnectionStateChanged(callback: (HubConnectionState) -> Unit) {
        onConnectionStateChanged = callback
    }

    /**
     * Send basic message
     */
    fun sendMessage(user: String, message: String) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.send("SendMessage", user, message)
        } else {
            Log.w(TAG, "⚠️ Cannot send message - not connected")
        }
    }

    /**
     * Gửi ChiyodaInfo object trực tiếp qua SignalR
     *
     * @param chiyodaInfo Thông tin Chiyoda cần gửi
     * @param methodName Tên method trên SignalR Hub (mặc định: "SubmitChiyodaInfo")
     */
    @SuppressLint("CheckResult")
    fun sendChiyodaInfo(chiyodaInfo: ChiyodaInfo, methodName: String = "SubmitChiyodaInfo") {
        if (hubConnection.connectionState != HubConnectionState.CONNECTED) {
            Log.w(TAG, "⚠️ Cannot send: SignalR connection is not established")
            throw IllegalStateException("Not connected to SignalR")
        }

        try {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "📤 Sending ChiyodaInfo via SignalR:")
            Log.d(TAG, "Method: $methodName")
            Log.d(TAG, "WoNo: ${chiyodaInfo.wono}")
            Log.d(TAG, "═══════════════════════════════════════")

            // Gửi object trực tiếp - SignalR tự động serialize
            hubConnection.invoke(Void::class.java, methodName, chiyodaInfo)
                .timeout(30, TimeUnit.SECONDS)

            Log.d(TAG, "✅ ChiyodaInfo sent successfully!")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error sending ChiyodaInfo: ${e.message}", e)
            throw e
        }
    }

    /**
     * Check if connected
     */
    fun isConnected(): Boolean {
        return ::hubConnection.isInitialized &&
                hubConnection.connectionState == HubConnectionState.CONNECTED
    }

    /**
     * Get current connection state
     */
    fun getConnectionState(): HubConnectionState {
        return if (::hubConnection.isInitialized) {
            hubConnection.connectionState
        } else {
            HubConnectionState.DISCONNECTED
        }
    }

    /**
     * Stop connection
     */
    fun stopConnection() {
        if (::hubConnection.isInitialized) {
            try {
                hubConnection.stop()
                Log.d(TAG, "✅ Connection stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping connection: ${e.message}", e)
            }
        }
    }

    /**
     * Cleanup
     */
    fun cleanup() {
        clearOnSubmitResponseListener()
        onConnectionStateChanged = null
        stopConnection()
    }
}

/**
 * Adapter để serialize/deserialize LocalDate cho Gson
 */
private class LocalDateAdapter : com.google.gson.JsonSerializer<LocalDate>,
    com.google.gson.JsonDeserializer<LocalDate> {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun serialize(
        src: LocalDate?,
        typeOfSrc: java.lang.reflect.Type?,
        context: com.google.gson.JsonSerializationContext?
    ): com.google.gson.JsonElement {
        return com.google.gson.JsonPrimitive(src?.format(formatter))
    }

    override fun deserialize(
        json: com.google.gson.JsonElement?,
        typeOfT: java.lang.reflect.Type?,
        context: com.google.gson.JsonDeserializationContext?
    ): LocalDate {
        return LocalDate.parse(json?.asString, formatter)
    }
}