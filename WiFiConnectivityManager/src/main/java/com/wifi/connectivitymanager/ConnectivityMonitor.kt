package com.wifi.connectivitymanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager

class ConnectivityMonitor(private val context: Context) : BroadcastReceiver() {

    private val LOG_TAG = "ConnectivityMonitor"

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
        val wifiEnabled = wifiInfo?.isConnectedOrConnecting ?: false
        val cellEnabled = cellInfo?.isConnectedOrConnecting ?: false
        onConnectivityChangedListener.onConnectivityChanged(wifiEnabled, cellEnabled)
    }

    fun startMonitoring(onConnectivityChangedListener: OnConnectivityChangedListener) {
        this.onConnectivityChangedListener = onConnectivityChangedListener
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        this.context.registerReceiver(this, intentFilter)
    }

    fun stopMonitoring() {
        context.unregisterReceiver(this)
    }

}