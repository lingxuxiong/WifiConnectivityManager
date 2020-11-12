package com.wifi.connectivitymanager

import android.content.Context
import android.os.Build

/**
 * A helper class to generate the appropriate wifi connectivity manager instance
 * based on the provided info.
 */
class ConnectivityManagerBuilder(val context: Context) {

    private var interactive = false
    private lateinit var callback: OnConnectResultCallback

    fun build(): WifiConnectivityManager {
        val wcm = if (interactive) {
            WifiConnectivityManagerInteractiveImp(context)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            SysUtils.getTargetSdkVersion(context) >= Build.VERSION_CODES.Q) {
            WifiConnectivityManagerAndroid10Imp(context)
        } else {
            WifiConnectivityManagerPreAndroid10Imp(context)
        }
        wcm.registerConnectResultCallback(callback)
        return wcm
    }

    fun setInteractive(interactive: Boolean): ConnectivityManagerBuilder {
        this.interactive = interactive
        return this
    }

    fun addConnectivityCallback(callback: OnConnectResultCallback): ConnectivityManagerBuilder {
        this.callback = callback
        return this
    }

}