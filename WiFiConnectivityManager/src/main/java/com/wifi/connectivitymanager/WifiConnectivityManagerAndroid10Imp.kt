package com.wifi.connectivitymanager

import android.content.Context

/**
 * Wifi Connectivity Manager implementation that is designed to connect
 * the mobile device to a specified network on Android 10+. It relies on
 * [android.net.ConnectivityManager.requestNetwork] to connect to the
 * target network.
 */
class WifiConnectivityManagerAndroid10Imp(context: Context)
    : WifiConnectivityManager(context) {

    override fun connect(ssid: String, password: String?) {
        connect(ssid, password, DEFAULT_CONNECT_TIME_OUT_SECONDS)
    }

    override fun connect(ssid: String, password: String?, timeout: Int) {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }
}