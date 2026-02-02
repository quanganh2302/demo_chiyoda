package com.example.myapplication

import android.app.Application
import com.example.myapplication.service.KeyenceScannerService
import com.example.myapplication.service.signalR.SignalRConfig
import com.example.myapplication.service.signalR.SignalRManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val config = SignalRConfig.development(
            serverUrl = "http://192.168.103.30:5000/chiyodahub"
        )
        SignalRManager.initialize(this, config)
    }
}
