package com.wifi.connectivitymanager

import android.content.Context

/**
 * Wifi Connectivity Manager implementation that relies on the user
 * to manually choose the target network. Once received a request to
 * connect to a new network, it tries to launch the system Wi-Fi settings
 * screen and let the user to choose the target network to connect to.
 */
class WifiConnectivityManagerInteractiveImp(context: Context) : WifiConnectivityManager(context) {

    override fun connect(ssid: String, password: String?) {
        TODO("Not yet implemented")
    }

    override fun connect(ssid: String, password: String?, timeoutInSeconds: Int) {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }
}