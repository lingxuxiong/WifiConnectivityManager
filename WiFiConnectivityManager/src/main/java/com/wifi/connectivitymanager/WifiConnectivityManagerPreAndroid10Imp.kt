@file:Suppress("DEPRECATION")

package com.wifi.connectivitymanager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Network
import android.net.wifi.WifiConfiguration
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import com.wifi.connectivitymanager.errors.MissingPermissionError
import com.wifi.connectivitymanager.errors.NetworkNotAvailableError
import com.wifi.connectivitymanager.errors.RequestTimedOutError
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Wifi Connectivity Manager implementation that is designed to connect
 * the mobile device to a specified network on platforms pre Android 10.
 * It relies on [android.net.wifi.WifiConfiguration] to config and
 * connect to the target network.
 */
class WifiConnectivityManagerPreAndroid10Imp(context: Context)
    : WifiConnectivityManager(context) {

    val TAG = "WifiConnMgrPreAndroid10"
    val UNKNOWN_NET_ID = -1

    var _addedNetworkId = UNKNOWN_NET_ID

    enum class SecurityType {
        WEP, WPA, WPA2, NONE, UNKNOWN, PSK, EAP
    }

    override fun connect(ssid: String, password: String?) {
        connect(ssid, password, DEFAULT_CONNECT_TIME_OUT_SECONDS)
    }

    override fun connect(ssid: String, password: String?, timeoutInSeconds: Int) {
        if (checkHasConnectedTo(ssid)) {
            Log.d(TAG, "already connected to $ssid")
            notifyConnectivityAvailable(ssid)
            return
        }

        if (ActivityCompat.checkSelfPermission(
                getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            val msg = "missing permission ${Manifest.permission.ACCESS_FINE_LOCATION}"
            notifyConnectivityUnavailable(MissingPermissionError(msg))
            return
        }

        var config: WifiConfiguration? = null
        val availableConfigs = getWifiManager().configuredNetworks
        for (c in availableConfigs) {
            if (TextUtils.equals(ssid, unquote(c.SSID))) {
                config = c
                break
            }
        }

        if (config == null) {
            config = WifiConfiguration()
            config.SSID = quote(ssid)
            Log.d(TAG, "add new config: $config")
        } else {
            _addedNetworkId = config.networkId
            Log.d(TAG, "reuse existing config: $config")
        }

        when (val securityType = getSecurityType(ssid)) {
            SecurityType.WEP -> {
                if (password?.length == 0) {
                    if (isHexWepKey(password)) {
                        config.wepKeys[0] = password
                    } else {
                        config.wepKeys[0] = quote(password)
                    }
                }

                config.wepTxKeyIndex = 0
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            }

            SecurityType.WPA, SecurityType.WPA2 -> {
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                config.allowedProtocols.set(if (securityType == SecurityType.WPA2)
                    WifiConfiguration.Protocol.RSN else WifiConfiguration.Protocol.WPA)
                config.preSharedKey = quote(password)
            }

            SecurityType.NONE -> {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            }

            SecurityType.UNKNOWN -> {
                throw IllegalStateException("unknown security type found")
            }
        }

        connect(config, timeoutInSeconds)
    }

    private fun connect(config: WifiConfiguration, timeoutInSeconds: Int) {
        val targetSSID = unquote(config.SSID)
        val timeoutHandler = getUiHandler()
        val targetTimeoutTime = SystemClock.uptimeMillis() + TimeUnit.MILLISECONDS.convert(
            timeoutInSeconds.toLong(), TimeUnit.SECONDS)
        val networkChangeToleranceLimit = AtomicInteger(1)
        val connectivityMonitor = getConnectivityMonitor()

        connectivityMonitor.startMonitoring(
            object : ConnectivityMonitor.OnConnectivityChangedListener {
                override fun onConnectivityChanged(wifiEnabled: Boolean, cellularEnabled: Boolean) {
                    Log.d(TAG, "connectivity changed, wifiEnabled: " +
                            "$wifiEnabled, cellularEnabled: $cellularEnabled")
                    if (wifiEnabled) {
                        if (checkHasConnectedTo(targetSSID)) {
                            Log.d(TAG, "has connected to: $targetSSID")
                            connectivityMonitor.stopMonitoring()
                            timeoutHandler.removeCallbacksAndMessages(TAG)

                            val activeNetwork = getActiveNetwork()
                            val bound = bindProcessToNetwork(activeNetwork)
                            Log.d(TAG, "bound to $targetSSID returned $bound")
                            notifyConnectivityAvailable(targetSSID)
                        } else {
                            val msg = "connected to unexpected ${getCurrentSSID()}"
                            Log.d(TAG, msg)
                            if (networkChangeToleranceLimit.decrementAndGet() < 0) {
                                // On some phone models, such as Nexus 5 running Android 6.0.1,
                                // it may first report wifi available event with current network ssid,
                                // then report wifi available event with the target network ssid.
                                // Three consecutive network changed events can be observed during
                                // the network transition process. For example, suppose the device
                                // is now on QA-TPLINK and will connect to TEST-DevKit,
                                // 1. connectivity changed, wifiEnabled: true, cellularEnabled: false  (QA-TPLINK available)
                                // 2. connectivity changed, wifiEnabled: false, cellularEnabled: false (QA-TPLINK lost)
                                // 3. connectivity changed, wifiEnabled: true, cellularEnabled: false  (TEST-DevKit available)
                                connectivityMonitor.stopMonitoring()
                                notifyConnectivityUnavailable(NetworkNotAvailableError(msg))
                            }
                        }
                    }
                }
            })

        timeoutHandler.postAtTime({
            connectivityMonitor.stopMonitoring()
            if (checkHasConnectedTo(targetSSID)) {
                Log.d(TAG, "timed out and connected to: $targetSSID")
                notifyConnectivityAvailable(targetSSID)
            } else {
                val msg = "timed out but connected to ${getCurrentSSID()}"
                notifyConnectivityUnavailable(RequestTimedOutError(msg))
            }
        }, TAG, targetTimeoutTime)
    }

    override fun disconnect(): Boolean {
        getUiHandler().removeCallbacksAndMessages(TAG)
        getWifiManager().disconnect()
        return bindProcessToNetwork(null)
    }

    private fun getSecurityType(ssid: String): SecurityType {
        val lastScanResults = getApScanner().getLastScanResults()
        for (result in lastScanResults) {
            if (TextUtils.equals(ssid, result.SSID)) {
                Log.d(TAG, "$ssid capabilities: ${result.capabilities}")
                return when {
                    result.capabilities.contains("WEP") -> {
                        SecurityType.WEP
                    }
                    result.capabilities.contains("PSK") -> {
                        SecurityType.PSK
                    }
                    result.capabilities.contains("EAP") -> {
                        SecurityType.EAP
                    }
                    else -> SecurityType.UNKNOWN
                }

            }
        }

        return SecurityType.WEP
    }

    private fun isHexWepKey(wepKey: String): Boolean {
        val len = wepKey.length
        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        return if (len != 10 && len != 26 && len != 58) {
            false
        } else {
            isHex(wepKey)
        }
    }

    private fun isHex(key: String): Boolean {
        for (c in key.toCharArray()) {
            if (c in '0'..'9' || c in 'A'..'F' || c in 'a'..'f') {
                return true
            }
        }
        return false
    }

    private fun getActiveNetwork(): Network? {
        val cm = getConnectivityManager()
        val networks = cm.allNetworks
        for (net in networks) {
            val info = cm.getNetworkInfo(net)
            if (info.isConnectedOrConnecting) {
                return net
            }
        }

        return null
    }
}