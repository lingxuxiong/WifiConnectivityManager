package com.wifi.connectivitymanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log

class ConnectivityMonitor(private val context: Context) : BroadcastReceiver() {

    private val LOG_TAG = "ConnectivityMonitor"

    private var monitor: ConnectivityMonitor? = null
    private lateinit var onConnectivityChangedListener: OnConnectivityChangedListener

    /**
     * Listener interface to receive events when network connectivity changes
     */
    interface OnConnectivityChangedListener {
        fun onConnectivityChanged(wifiEnabled: Boolean, cellularEnabled: Boolean)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val cm = this.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val cellInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        var wifiEnabled = wifiInfo?.isConnectedOrConnecting ?: false
        var cellEnabled = cellInfo?.isConnectedOrConnecting ?: false
        onConnectivityChangedListener.onConnectivityChanged(wifiEnabled, cellEnabled)
    }

    fun startMonitoring(
        context: Context,
        onConnectivityChangedListener: OnConnectivityChangedListener
    ) {
        if (monitor == null) {
            this.monitor = ConnectivityMonitor(context)
            this.onConnectivityChangedListener = onConnectivityChangedListener
            this.context.registerReceiver(monitor, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        } else {
            Log.d(LOG_TAG, "monitoring in progress")
        }
    }

    fun stopMonitoring() {
        context.unregisterReceiver(monitor)
    }

}