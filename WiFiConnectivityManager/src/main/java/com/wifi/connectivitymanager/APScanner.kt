package com.wifi.connectivitymanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log

class APScanner(private val context: Context) : BroadcastReceiver() {

    private val LOG_TAG = "APScanner"
    private lateinit var onScanResultListener: OnScanResultListener

    /**
     * Listener interface to receive events when network connectivity changes
     */
    interface OnScanResultListener {
        fun onScanResult(scanResults: List<ScanResult>)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(LOG_TAG, "onReceive: $intent")
        onScanResultListener.onScanResult(getLastScanResults())
    }

    fun startScanning(onScanResultListener: OnScanResultListener) {
        this.onScanResultListener = onScanResultListener
        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        this.context.registerReceiver(this, intentFilter)
    }

    fun stopScanning() {
        context.unregisterReceiver(this)
    }

    fun getLastScanResults(): List<ScanResult> {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wm.scanResults
    }

}