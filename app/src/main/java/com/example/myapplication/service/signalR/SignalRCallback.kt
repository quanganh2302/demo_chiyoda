package com.example.myapplication.service.signalR

import com.example.myapplication.models.SubmitResponseDto
import com.example.myapplication.models.SystemStatusUpdate

// Connection State Enum: State of SignalR Connection

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    FAILED
}

// Connection Callback Interface
// MainActivity implement this interface to receive callback

interface SignalRConnectionCallback {
    // Called when start connection
    fun onConnecting()

    // Called when connection is established
    fun onConnected()

    // Called when connection failed
    fun onConnectionFailed(error : String)

    // Called when connection is lost
    fun onDisconnected()

    /**
     *  Call when start reconnecting
     *  @param attempt the number of retry (1,2,3 ...)
     *  @param maxAttempts the number of max retry
     */
    fun onReconnecting(attempt: Int, maxAttempts : Int)

    // Called when reconnect is successful
    fun onReconnected()

}

// Default implementation (empty) to MainActivity only override methods needed
open class SimpleConnectionCallback: SignalRConnectionCallback{
    override fun onConnecting() {}
    override fun onConnected() {}
    override fun onConnectionFailed(error: String) {}
    override fun onDisconnected() {}
    override fun onReconnecting(attempt: Int, maxAttempts : Int) {}
    override fun onReconnected() {}
}

// Event Listener Interface

interface SignalREventListener{
    // Called when receive Submit Response
    fun onSubmitResponse(response: SubmitResponseDto)
    // Called when receive System Status Update
    fun onSystemStatusUpdate(update: SystemStatusUpdate)
    // Called when receive error
    fun onRpaError(error: String)

}

// Default implementation (empty)
open class SimpleEventListener: SignalREventListener{
    override fun onSubmitResponse(response: SubmitResponseDto) {}
    override fun onSystemStatusUpdate(update: SystemStatusUpdate) {}
    override fun onRpaError(error: String) {}
}