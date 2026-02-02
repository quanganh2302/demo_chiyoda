package com.example.myapplication.service.signalR

data class SignalRConfig (
    // URL của SignalR Hub trên server (.NET)
    val serverUrl: String = "http://192.168.103.30:5000/chiyodahub",

    // Name of Hub
    val hubName: String = "ChiyodaHub",

    /**
     * Connection timeout (milliseconds)
     * Default: 30 seconds
     */
    val connectionTimeout: Long = 30_000L,

    /**
     * Turn on/off auto-reconnection
     * Default: true
     */

    val enableAutoReconnect: Boolean = true,

    /**
     * Maximum number of reconnection attempts
     * Default: 10
     */
    val maxReconnectAttempts: Int = 10,

    /**
     * Initial delay for reconnect (milliseconds)
     * Default: 1000 milliseconds
     *
     * Delay will increase follow exponential backoff:
     * 1000, 2000, 4000, 8000, 16000, 30000 (capped)
     */

    val initialReconnectDelay: Long = 1_000L,

    /**
     * Max delay for reconnect (milliseconds)
     * Default: 30000 milliseconds
     */
    val maxReconnectDelay: Long = 30_000L,

    /**
     * Enable logging for debugging
     * Default: true (disable in production)
     */
    val enableLogging: Boolean = true

){
    companion object{
        /**
         * Default configuration for SignalR
         */

        fun development(serverUrl: String) : SignalRConfig{
            return SignalRConfig(
                serverUrl = serverUrl,
                enableLogging = true,
                enableAutoReconnect = true,
                maxReconnectAttempts = 10,
            )
        }
        /**
         * Config for production
         */
        fun production(serverUrl: String) : SignalRConfig{
            return SignalRConfig(
                serverUrl = serverUrl,
                enableLogging = false,
                enableAutoReconnect = true,
                maxReconnectAttempts = 20,
                connectionTimeout = 60_000L,
            )
        }
    }
}


