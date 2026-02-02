package com.example.myapplication.ui.base

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.drawerlayout.widget.DrawerLayout
import com.example.myapplication.R
import com.example.myapplication.service.signalR.ConnectionState
import com.example.myapplication.service.signalR.SignalRManager
import com.example.myapplication.service.signalR.SimpleConnectionCallback
import com.example.myapplication.ui.fragments.HomeFragment
import com.example.myapplication.ui.utils.ToastManager
import com.example.myapplication.ultis.common.SignalRResponseHandler

class MainActivity : BaseActivity() {

    companion object {
        private var hasSetupConnection = false
    }

    private lateinit var drawerLayout: DrawerLayout
    private var connectingDialog: AlertDialog? = null

    override fun hasDrawer(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        attachHeader()

        if (!hasSetupConnection) {
            setupSignalRConnection()
            hasSetupConnection = true
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }

    private fun setupSignalRConnection() {
        val currentState = SignalRManager.getConnectionState()
        if (currentState == ConnectionState.CONNECTED ||
            currentState == ConnectionState.CONNECTING) {
            Log.d("MainActivity", "⏭️ Already connected/connecting, skip setup")
            return
        }

        SignalRManager.connect(object : SimpleConnectionCallback() {
            override fun onConnecting() {
                runOnUiThread {
                    dismissAllDialogs()
                    connectingDialog = SignalRResponseHandler.showConnectingDialog(this@MainActivity)
                }
            }

            override fun onConnected() {
                runOnUiThread {
                    dismissAllDialogs()

                    ToastManager.success(
                        this@MainActivity,
                        "✅ Connected to RPA Server"
                    )

                    notifyHomeFragmentToUpdate()
                }
            }

            override fun onConnectionFailed(error: String) {
                runOnUiThread {
                    dismissAllDialogs()

                    ToastManager.error(
                        this@MainActivity,
                        "❌ Cannot connect to RPA"
                    )

                    SignalRResponseHandler.showConnectionErrorDialog(
                        this@MainActivity,
                        error,
                        onRetry = {
                            dismissAllDialogs()
                            hasSetupConnection = false
                            setupSignalRConnection()
                        }
                    )
                }
            }

            override fun onDisconnected() {
                runOnUiThread {
                    ToastManager.warning(
                        this@MainActivity,
                        "⚠️ Connection lost. Reconnecting..."
                    )
                }
            }

            override fun onReconnecting(attempt: Int, maxAttempts: Int) {
                runOnUiThread {
                    dismissAllDialogs()
                    connectingDialog = SignalRResponseHandler.showReconnectingDialog(
                        this@MainActivity,
                        attempt,
                        maxAttempts
                    )
                }
            }

            override fun onReconnected() {
                runOnUiThread {
                    // ✅ QUAN TRỌNG: Dismiss tất cả dialog trước
                    dismissAllDialogs()

                    ToastManager.success(
                        this@MainActivity,
                        "✅ Reconnected to RPA"
                    )

                    // ✅ Delay một chút để toast hiển thị rồi mới navigate
                    window.decorView.postDelayed({
                        navigateToHomeIfNeeded()
                    }, 300)
                }
            }
        })
    }

    /**
     * ✅ Dismiss tất cả dialogs
     */
    private fun dismissAllDialogs() {
        try {
            connectingDialog?.dismiss()
            connectingDialog = null

            Log.d("MainActivity", "🗑️ Dialog dismissed")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error dismissing dialog: ${e.message}")
        }
    }

    private fun notifyHomeFragmentToUpdate() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (fragment is HomeFragment) {
            Log.d("MainActivity", "🔔 Calling refreshConnectionStatus()")
            fragment.refreshConnectionStatus()
        }
    }

    /**
     * ✅ Quay về HomeFragment nếu cần
     */
    private fun navigateToHomeIfNeeded() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment !is HomeFragment) {
            Log.d("MainActivity", "🏠 Navigating back to HomeFragment")

            // Clear back stack
            supportFragmentManager.popBackStack(
                null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            )

            // Replace with HomeFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        } else {
            Log.d("MainActivity", "✅ Already on HomeFragment, refreshing...")
            (currentFragment as? HomeFragment)?.refreshConnectionStatus()
        }
    }

    override fun onStart() {
        super.onStart()
        SignalRManager.ensureConnected()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissAllDialogs()
        if (isFinishing) {
            SignalRManager.disconnect()
            hasSetupConnection = false
        }
    }

    override fun onMenuClicked() {
        drawerLayout.open()
    }

    override fun onAfterDrawerNavigate() {
        drawerLayout.closeDrawers()
    }
}