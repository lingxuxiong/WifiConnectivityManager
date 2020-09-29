package com.wifi.connectivitymanager

/**
 * Callback used to receive the result of the connect request.
 * Callers of the <var>connect</var> requests should register
 * this callback to be able to receive the connection result.
 */
interface OnConnectResultCallback {

    /**
     * Called when the requested network with the specified SSID name is available.
     *
     * @param ssid the name of the Wi-Fi network the device is currently connected
     * to. It should be the same as the requested network name specified
     * in the `connect` requests.
     */
    fun onAvailable(ssid: String)

    /**
     * Called when the requested network was not available due to some error.
     *
     * @param error the error that caused the network unavailability.
     * Possible errors are:
     *  1. [WifiConnectError.RequestTimedOut] for the connect request eventually timed out.
     *  2. [WifiConnectError.MissingPermission] for lacking of necessary network permission(s).
     */
    fun onUnavailable(error: WifiConnectError?)
}