package com.example.myapplication.service.signalR

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.myapplication.helper.ChiyodaInfo
import com.example.myapplication.service.signalR.SignalRService
import com.example.myapplication.models.*
import kotlinx.coroutines.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.pow

object SignalRManager {

    private const val TAG = "SignalRManager"

    // ═══════════════════════════════════════════════════════
    // PROPERTIES
    // ═══════════════════════════════════════════════════════

    private var appContext: Context? = null
    private var config: SignalRConfig? = null
    private var signalRService: SignalRService? = null

    // Connection state
    private var connectionState: ConnectionState = ConnectionState.DISCONNECTED
    private var connectedAt: Long? = null

    // Callbacks & Listeners
    private var connectionCallback: SignalRConnectionCallback? = null
    private val eventListeners = CopyOnWriteArrayList<SignalREventListener>()

    // Reconnection
    private var reconnectAttempt = 0
    private var reconnectJob: Job? = null
    private val reconnectScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Main thread handler
    private val mainHandler = Handler(Looper.getMainLooper())

    // ═══════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════

    /**
     * Initialize SignalRManager với config
     * PHẢI gọi trong MyApplication.onCreate()
     *
     * @param context Application context
     * @param config SignalR configuration
     */
    fun initialize(context: Context, config: SignalRConfig) {
        log("Initializing SignalRManager...")

        this.appContext = context.applicationContext
        this.config = config

        // Tạo SignalRService instance
        signalRService = SignalRService(context.applicationContext)

        // Setup service callbacks
        setupServiceCallbacks()

        log("✅ SignalRManager initialized (connection NOT started)")
    }

    /**
     * Setup callbacks từ SignalRService
     */
    private fun setupServiceCallbacks() {
        signalRService?.setOnSubmitResponseListener { response ->
            runOnMainThread {
                notifySubmitResponse(response)
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // CONNECTION MANAGEMENT
    // ═══════════════════════════════════════════════════════

    /**
     * Connect to SignalR server
     * Gọi trong MainActivity.onCreate()
     *
     * @param callback Connection lifecycle callbacks
     */
    fun connect(callback: SignalRConnectionCallback) {
        log("connect() called, current state: $connectionState")

        if (connectionState == ConnectionState.CONNECTED) {
            log("Already connected, calling onConnected()")
            runOnMainThread { callback.onConnected() }
            return
        }

        if (connectionState == ConnectionState.CONNECTING) {
            log("Already connecting, ignoring duplicate connect()")
            return
        }

        this.connectionCallback = callback

        // Reset reconnect attempt counter
        reconnectAttempt = 0

        // Start connecting
        startConnection()
    }

    /**
     * Start connection process
     */
    private fun startConnection() {
        if (signalRService == null) {
            logError("SignalRService is null! Did you call initialize()?")
            runOnMainThread {
                connectionCallback?.onConnectionFailed("Service not initialized")
            }
            return
        }

        updateState(ConnectionState.CONNECTING)
        runOnMainThread { connectionCallback?.onConnecting() }

        // Start connection in background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                log("Starting SignalR connection...")
                signalRService?.startConnection()

                // Check if actually connected
                delay(500) // Small delay để đảm bảo connection established

                if (signalRService?.isConnected() == true) {
                    onConnectionSuccess()
                } else {
                    onConnectionFailure("Connection failed - service not connected")
                }

            } catch (e: Exception) {
                logError("Connection error: ${e.message}", e)
                onConnectionFailure(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Callback khi connection thành công
     */
    private fun onConnectionSuccess() {
        log("✅ Connection successful!")

        connectedAt = System.currentTimeMillis()
        reconnectAttempt = 0

        updateState(ConnectionState.CONNECTED)

        runOnMainThread {
            connectionCallback?.onConnected()
        }

        // ✅ Fetch initial system status SAU KHI state đã CONNECTED
        requestSystemStatus()
    }

    /**
     * Callback khi connection thất bại
     */
    private fun onConnectionFailure(error: String) {
        logError("❌ Connection failed: $error")

        updateState(ConnectionState.DISCONNECTED)

        // Parse error message để hiển thị thân thiện hơn
        val userFriendlyMessage = when {
            error.contains("CLEARTEXT") ->
                "Cannot connect to RPA server (Network security policy)"
            error.contains("refused") ->
                "Cannot connect to RPA server (Connection refused)"
            error.contains("timeout") ->
                "Cannot connect to RPA server (Connection timeout)"
            error.contains("UnknownHost") ->
                "Cannot find RPA server at specified address"
            else ->
                "Cannot connect to RPA server: $error"
        }

        runOnMainThread {
            connectionCallback?.onConnectionFailed(userFriendlyMessage)
        }

        // Schedule reconnect nếu enabled
        if (config?.enableAutoReconnect == true) {
            scheduleReconnect()
        } else {
            updateState(ConnectionState.FAILED)
        }
    }

    /**
     * Disconnect from server
     */
    fun disconnect() {
        log("Disconnecting...")

        cancelReconnect()

        signalRService?.stopConnection()

        updateState(ConnectionState.DISCONNECTED)
        connectedAt = null

        log("✅ Disconnected")
    }

    /**
     * Ensure connection is alive
     * Gọi trong MainActivity.onStart()
     */
    fun ensureConnected() {
        log("ensureConnected() - current state: $connectionState")

        when (connectionState) {
            ConnectionState.CONNECTED -> {
                // Check if really connected
                if (signalRService?.isConnected() != true) {
                    log("⚠️ State is CONNECTED but service not connected, reconnecting...")
                    updateState(ConnectionState.DISCONNECTED)
                    connectionCallback?.let { connect(it) }
                }
            }
            ConnectionState.DISCONNECTED, ConnectionState.FAILED -> {
                log("Not connected, reconnecting...")
                connectionCallback?.let { connect(it) }
            }
            else -> {
                log("State is $connectionState, no action needed")
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // AUTO-RECONNECTION
    // ═══════════════════════════════════════════════════════

    /**
     * Schedule reconnection với exponential backoff
     */
    private fun scheduleReconnect() {
        val maxAttempts = config?.maxReconnectAttempts ?: 10

        if (reconnectAttempt >= maxAttempts) {
            logError("❌ Max reconnect attempts ($maxAttempts) reached")
            updateState(ConnectionState.FAILED)
            runOnMainThread {
                connectionCallback?.onConnectionFailed("Max reconnect attempts reached")
            }
            return
        }

        reconnectAttempt++

        val delay = calculateBackoffDelay()
        log("Scheduling reconnect attempt $reconnectAttempt/$maxAttempts in ${delay}ms...")

        updateState(ConnectionState.RECONNECTING)
        runOnMainThread {
            connectionCallback?.onReconnecting(reconnectAttempt, maxAttempts)
        }

        // Cancel existing reconnect job
        reconnectJob?.cancel()

        // Schedule new reconnect
        reconnectJob = reconnectScope.launch {
            delay(delay)

            if (connectionState == ConnectionState.RECONNECTING) {
                log("Attempting reconnect #$reconnectAttempt...")
                startConnection()
            }
        }
    }

    /**
     * Calculate backoff delay using exponential backoff
     */
    private fun calculateBackoffDelay(): Long {
        val initialDelay = config?.initialReconnectDelay ?: 1000L
        val maxDelay = config?.maxReconnectDelay ?: 30000L

        val exponentialDelay = initialDelay * (2.0.pow(reconnectAttempt - 1).toLong())
        return minOf(exponentialDelay, maxDelay)
    }

    /**
     * Cancel scheduled reconnection
     */
    private fun cancelReconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
    }

    /**
     * Handle disconnection (khi detect connection lost)
     */
    fun onDisconnected() {
        log("⚠️ Connection lost!")

        if (connectionState == ConnectionState.CONNECTED) {
            updateState(ConnectionState.DISCONNECTED)

            runOnMainThread {
                connectionCallback?.onDisconnected()
            }

            // Auto-reconnect
            if (config?.enableAutoReconnect == true) {
                scheduleReconnect()
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // SEND METHODS
    // ═══════════════════════════════════════════════════════

    /**
     * Submit ChiyodaInfo task to RPA
     *
     * @param chiyodaInfo ChiyodaInfo object
     * @return true nếu gửi thành công, false nếu không connected
     */
    fun submitTask(chiyodaInfo: ChiyodaInfo): Boolean {
        if (!isConnected()) {
            logError("Cannot submit task - not connected")
            return false
        }

        try {
            signalRService?.sendChiyodaInfo(chiyodaInfo)
            log("✅ Task submitted: ${chiyodaInfo.wono}")
            return true
        } catch (e: Exception) {
            logError("Error submitting task: ${e.message}", e)
            return false
        }
    }

    /**
     * Request current system status from server
     */
    fun requestSystemStatus() {
        if (!isConnected()) {
            log("Cannot request system status - not connected")
            return
        }

        try {
            // Có thể implement method riêng trong SignalRService nếu cần
            // Hoặc server tự động broadcast status khi connect
            log("System status will be received via SystemStatusUpdate event")
        } catch (e: Exception) {
            logError("Error requesting system status: ${e.message}", e)
        }
    }

    // ═══════════════════════════════════════════════════════
    // EVENT LISTENERS MANAGEMENT
    // ═══════════════════════════════════════════════════════

    /**
     * Register event listener
     * Fragments/Activities có thể register để nhận events
     */
    fun registerEventListener(listener: SignalREventListener) {
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener)
            log("Event listener registered: ${listener.javaClass.simpleName}")
        }
    }

    /**
     * Unregister event listener
     * PHẢI gọi trong onDestroy/onDestroyView
     */
    fun unregisterEventListener(listener: SignalREventListener) {
        eventListeners.remove(listener)
        log("Event listener unregistered: ${listener.javaClass.simpleName}")
    }

    /**
     * Notify all listeners về submit response
     */
    private fun notifySubmitResponse(response: SubmitResponseDto) {
        log("Broadcasting submit response to ${eventListeners.size} listeners")
        eventListeners.forEach { listener ->
            try {
                listener.onSubmitResponse(response)
            } catch (e: Exception) {
                logError("Error notifying listener: ${e.message}", e)
            }
        }
    }

    /**
     * Get current system status (synchronous)
     */
    fun getCurrentSystemStatus(): SystemStatusUpdate? {
        // Nếu đã có status từ cache/memory
        return lastSystemStatus
    }

    private var lastSystemStatus: SystemStatusUpdate? = null

    fun notifySystemStatusUpdate(status: SystemStatusUpdate) {
        lastSystemStatus = status  // ✅ Cache status
        log("Broadcasting system status update to ${eventListeners.size} listeners")
        runOnMainThread {
            eventListeners.forEach { listener ->
                try {
                    listener.onSystemStatusUpdate(status)
                } catch (e: Exception) {
                    logError("Error notifying listener: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Notify all listeners về RPA error
     */
    fun notifyRpaError(error: String) {
        log("Broadcasting RPA error to ${eventListeners.size} listeners")
        runOnMainThread {
            eventListeners.forEach { listener ->
                try {
                    listener.onRpaError(error)
                } catch (e: Exception) {
                    logError("Error notifying listener: ${e.message}", e)
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // STATE MANAGEMENT
    // ═══════════════════════════════════════════════════════

    /**
     * Update connection state
     */
    private fun updateState(newState: ConnectionState) {
        val oldState = connectionState
        connectionState = newState
        log("State changed: $oldState → $newState")
    }

    /**
     * Get current connection state
     */
    fun getConnectionState(): ConnectionState = connectionState

    /**
     * Check if connected
     */
    fun isConnected(): Boolean {
        return connectionState == ConnectionState.CONNECTED &&
                signalRService?.isConnected() == true
    }

    /**
     * Get connection info (for debugging)
     */
    fun getConnectionInfo(): ConnectionInfo {
        return ConnectionInfo(
            state = connectionState.name,
            serverUrl = config?.serverUrl ?: "Not configured",
            connectedAt = connectedAt,
            lastError = null,
            reconnectAttempts = reconnectAttempt
        )
    }

    // ═══════════════════════════════════════════════════════
    // UTILITIES
    // ═══════════════════════════════════════════════════════

    /**
     * Run action on main thread
     */
    private fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    /**
     * Log helper
     */
    private fun log(message: String) {
        if (config?.enableLogging == true) {
            Log.d(TAG, message)
        }
    }

    /**
     * Error log helper
     */
    private fun logError(message: String, throwable: Throwable? = null) {
        if (config?.enableLogging == true) {
            if (throwable != null) {
                Log.e(TAG, message, throwable)
            } else {
                Log.e(TAG, message)
            }
        }
    }

    /**
     * Cleanup - gọi khi app bị kill
     */
    fun cleanup() {
        log("Cleaning up SignalRManager...")

        disconnect()
        connectionCallback = null
        eventListeners.clear()
        reconnectScope.cancel()

        log("✅ Cleanup complete")
    }

}












