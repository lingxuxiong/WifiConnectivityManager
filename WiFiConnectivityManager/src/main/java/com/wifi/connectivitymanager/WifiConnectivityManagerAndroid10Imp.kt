package com.wifi.connectivitymanager

import android.content.Context
import android.net.*
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.PatternMatcher
import android.util.Log
import androidx.annotation.RequiresApi
import com.wifi.connectivitymanager.errors.NetworkNotAvailableError
import java.util.concurrent.TimeUnit

/**
 * Wifi Connectivity Manager implementation that is designed to connect
 * the mobile device to a specified network on Android 10+. It relies on
 * [android.net.ConnectivityManager.requestNetwork] to connect to the
 * target network.
 */
@RequiresApi(api = Build.VERSION_CODES.Q)
class WifiConnectivityManagerAndroid10Imp(context: Context) : WifiConnectivityManager(context) {

    private val LOG_TAG = "WifiConnMgrOnAndroid10"

    private var _networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun connect(ssid: String, password: String?) {
        connect(ssid, password, DEFAULT_CONNECT_TIME_OUT_SECONDS)
    }

    override fun connect(ssid: String, password: String?, timeoutInSeconds: Int) {
        if (checkHasConnectedTo(ssid)) {
            Log.d(LOG_TAG, "already connected to $ssid")
            notifyConnectivityAvailable(ssid)
            return
        }

        Log.i(LOG_TAG, "connecting to $ssid")

        if (isConnectingOrConnected()) {
            // Disconnect from currently connected network and wait until the system
            // auto-reconnect to a new network, which might or might not be the same
            // network we are connecting to, if it is then we are done, otherwise,
            // send a new request for the target network. This way, the network popup
            // dialog only comes up at most once, which would otherwise may appear
            // multiple times and end up with a "Something came up" failure dialog.
            disconnect()
            getConnectivityMonitor().startMonitoring(
                object : ConnectivityMonitor.OnConnectivityChangedListener {
                    override fun onConnectivityChanged(
                        wifiEnabled: Boolean,
                        cellularEnabled: Boolean
                    ) {
                        Log.d(LOG_TAG, "connectivityChanged, wifiEnabled: $wifiEnabled, " +
                                "cellularEnabled: $cellularEnabled")
                    if (wifiEnabled) {
                        getConnectivityMonitor().stopMonitoring()
                        if (checkHasConnectedTo(ssid)) {
                            Log.d(LOG_TAG, "system auto reconnected to $ssid")
                            notifyConnectivityAvailable(ssid)
                        } else {
                            Log.d(LOG_TAG, "current ssid is ${getCurrentSSID()}")
                            connect(ssid, password, DefaultNetworkCallback(), timeoutInSeconds)
                        }
                    }
                    }
                })
        } else {
            connect(ssid, password, DefaultNetworkCallback(), timeoutInSeconds)
        }
    }

    fun connect(
        ssid: String,
        password: String?,
        callback: ConnectivityManager.NetworkCallback,
        timeoutInSeconds: Int
    ) {
        _networkCallback = callback

        val wifiSpecifierBuilder = WifiNetworkSpecifier.Builder()
        val ssidPattern = PatternMatcher(ssid, PatternMatcher.PATTERN_PREFIX)

        wifiSpecifierBuilder.setSsidPattern(ssidPattern)
        if (password != null) {
            wifiSpecifierBuilder.setWpa2Passphrase(password)
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(wifiSpecifierBuilder.build())
            .build()
        val timeoutMs = TimeUnit.MILLISECONDS.convert(
            timeoutInSeconds.toLong(), TimeUnit.SECONDS
        )

        getConnectivityManager().requestNetwork(request, callback, timeoutMs.toInt())
    }

    override fun disconnect(): Boolean {
        return if (isConnectingOrConnected()) {
            Log.i(LOG_TAG, "disconnecting from current network ${getCurrentSSID()}")
            getConnectivityManager().unregisterNetworkCallback(_networkCallback!!)
            getConnectivityManager().bindProcessToNetwork(null)
            _networkCallback = null
            true
        } else {
            Log.i(LOG_TAG, "not connected before")
            false
        }
    }

    private fun isConnectingOrConnected(): Boolean {
        return _networkCallback != null
    }

    inner class DefaultNetworkCallback : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            val ssid = getCurrentSSID()
            val bound = bindProcessToNetwork(network)
            Log.d(LOG_TAG, "onAvailable:$network with ssid $ssid")
            Log.d(LOG_TAG, "bound to network: $bound")
            notifyConnectivityAvailable(ssid)
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            Log.i(LOG_TAG, "onLosing:$network, maxMsToLive:$maxMsToLive")
        }

        override fun onLost(network: Network) {
            Log.i(LOG_TAG, "onLost:$network")
        }

        override fun onUnavailable() {
            val activeNetwork = getConnectivityManager().activeNetwork
            Log.i(LOG_TAG, "onUnavailable， active network:$activeNetwork")
            notifyConnectivityUnavailable(NetworkNotAvailableError("network unavailable"))

            // explicitly call disconnect() to release resources held in
            // connect() request now that the request network is unavailable
            disconnect()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities) {
            Log.i(LOG_TAG, "onCapabilitiesChanged, network: $network, cap:$networkCapabilities")
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            Log.i(LOG_TAG, "onLinkPropertiesChanged, network:$network, linkProp:$linkProperties")
        }
    }
}