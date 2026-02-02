package com.example.myapplication.models

import com.google.gson.annotations.SerializedName

// Base request model to submit task to RPA

data class SubmitTaskRequest (
    @SerializedName("taskType") val taskType: String,
    @SerializedName("woNumber") val woNumber: String,
    @SerializedName("partNumber") val partNumber: String? = null,
    @SerializedName("quantity") val quantity: Int? = null,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("deviceId") val deviceId: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("additionalData") val additionalData: Map<String, Any>? = null

){
    companion object {
        const val TYPE_LABEL_PRINTING = "LabelPrinting"
        const val TYPE_INSPECTION ="Inspection"
        const val TYPE_WAREHOUSE = "Warehouse"
        const val TYPE_QUALITY_CHECK = "QualityCheck"
    }
}

// Request for gain system status

data class SystemStatusRequest(
    @SerializedName("requestType") val requestType: String= "GetStatus",
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis(),
)

// Connection info model (for logging/ debugging)

data class ConnectionInfo(
    val state: String,
    val serverUrl : String,
    val connectedAt: Long? = null,
    val lastError : String? = null,
    val reconnectAttempts: Int = 0,

){
    fun isConnected(): Boolean = state == "CONNECTED"

    fun getConnectionDuration(): Long? {
        return connectedAt?.let { System.currentTimeMillis() - it }

    }
}