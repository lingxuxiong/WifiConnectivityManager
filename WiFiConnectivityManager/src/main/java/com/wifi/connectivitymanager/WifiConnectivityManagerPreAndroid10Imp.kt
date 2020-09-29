package com.wifi.connectivitymanager

import android.content.Context

/**
 * Wifi Connectivity Manager implementation that is designed to connect
 * the mobile device to a specified network on platforms pre Android 10.
 * It relies on [android.net.wifi.WifiConfiguration] to config and
 * connect to the target network.
 */
class WifiConnectivityManagerPreAndroid10Imp(context: Context)
    : WifiConnectivityManager(context) {

    override fun connect(ssid: String, password: String?) {
        TODO("Not yet implemented")
    }

    override fun connect(ssid: String, password: String?, timeout: Int) {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }
}