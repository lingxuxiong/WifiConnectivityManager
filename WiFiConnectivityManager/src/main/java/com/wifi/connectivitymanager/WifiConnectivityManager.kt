package com.wifi.connectivitymanager

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.wifi.connectivitymanager.errors.WifiConnectError

/**
 *  Wifi Connectivity Manager defines how the mobile device connects to
 *  a target network. It defines a set of common <code>connect</code>
 *  methods that behave differently on different platforms. It also
 *  defines how the <code>connect</code> result gets notified by regiserting
 *  a connection callback listener [OnConnectResultCallback].
 */
abstract class WifiConnectivityManager(context: Context) {

    val UNKNOWN_SSID = "<unknown ssid>"
    val DEFAULT_CONNECT_TIME_OUT_SECONDS = 30

    private val _ctx: Context
    private val _wm: WifiManager
    private val _cm: ConnectivityManager
    private val _as: APScanner
    private val _connectivityMonitor: ConnectivityMonitor
    private val _callbacks: HashSet<OnConnectResultCallback>
    private var _uiHandler: Handler

    init {
        _ctx = context.applicationContext
        _wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        _cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        _as = APScanner(context)
        _connectivityMonitor = ConnectivityMonitor(context)
        _uiHandler = Handler(Looper.getMainLooper())
        _callbacks = LinkedHashSet()
    }

    constructor(context: Context, handler: Handler) : this(context) {
        _uiHandler = handler
    }

    /**
     * Connects the mobile device to the network with specified SSID. Note that callers
     * should call {@link #registerConnectResultCallback(OnConnectResultCallback)} in
     * order to be able to receive <var>connect</var> result from the provided callback.
     *
     * @param ssid     the name of the network to which the phone is to connect.
     * @param password password of the network, could be null for open network.
     */
    abstract fun connect(ssid: String, password: String?)

    /**
     * Connects the mobile device to the network with specified SSID, will time out
     * if the request can not complete within the specified time. Note that callers
     * should call {@link #registerConnectResultCallback(OnConnectResultCallback)} in
     * order to be able to receive <var>connect</var> result from the provided callback.
     *
     * @param ssid     the name of the network to which the phone is to connect.
     * @param password password of the network, could be null for open network.
     */
    abstract fun connect(ssid: String, password: String?, timeoutInSeconds: Int)

    /**
     * Disconnects the mobile device from the currently connected network，and free up
     * resources that might have been used in the corresponding [connect] request. For
     * example, unregister broadcast receivers that have been registered.
     */
    abstract fun disconnect(): Boolean

    /**
     * Returns if the mobile phone has connected to the specified SSID, true if
     * connected, otherwise, return false.
     */
    protected fun checkHasConnectedTo(ssid: String): Boolean {
        return TextUtils.equals(ssid, getCurrentSSID())
    }

    /**
     * Registers a new callback to be notified when the connect result is available.
     * @see [connect] on how to connect to a new network.
     */
    fun registerConnectResultCallback(callback: OnConnectResultCallback): Boolean {
        return _callbacks.add(callback)
    }

    fun unregisterConnectResultCallback(callback: OnConnectResultCallback): Boolean {
        return _callbacks.remove(callback)
    }

    /**
     * Binds current process to the specified network so that connectivity can be
     * available on Android M+ devices.
     *
     * @param network the network to bind to, pass null to unbind any bound network.
     */
    fun bindProcessToNetwork(network: Network?): Boolean {
        // https://android-developers.googleblog.com/2016/07/
        // connecting-your-app-to-wi-fi-device.html
        return if (shouldBindProcessToNetwork() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getConnectivityManager().bindProcessToNetwork(network)
        } else {
            false
        }
    }

    /**
     * Returns true if current process should be bound to the target network.
     */
    open fun shouldBindProcessToNetwork(): Boolean {
        return true
    }

    /**
     * Returns the application context initialized in the constructor.
     */
    fun getContext(): Context  = _ctx

    /**
     * Returns the Wifi manager associated with this manager.
     */
    fun getWifiManager(): WifiManager = _wm

    /**
     * Returns the system-wide connectivity manager associated with this manager.
     */
    fun getConnectivityManager(): ConnectivityManager = _cm

    /**
     * Returns the connectivity monitor that can be used to detect the connectivity
     * changed events.
     */
    fun getConnectivityMonitor(): ConnectivityMonitor = _connectivityMonitor

    /**
     * Returns the Access Point scanner that can be used to scan APs nearby.
     */
    fun getApScanner(): APScanner = _as

    /**
     * Returns the name of the Wi-Fi network the device is currently connected to, or
     * returns [UNKNOWN_SSID] if otherwise not available, most probably, due to missing
     * network permission.
     */
    fun getCurrentSSID(): String {
        return unquote(getWifiManager().connectionInfo.ssid)
    }

    /**
     * Returns the new string with surrounded with double-quotes ("), if any, striped off.
     * @param quotedString String surround with quotes
     * @return new string with surrounded with the double-quotes striped off.
     */
    fun unquote(quotedString: String): String {
        return quotedString.replace("\"", "")
    }

    /**
     * Returns the provided string surrounded with double-quotes (").
     * @param string String to surround with quotes
     * @return the provided string surrounded with double-quotes
     */
    fun quote(string: String?): String {
        return "\"" + string + "\""
    }

    fun getUiHandler(): Handler = _uiHandler

    fun getConnectivityChangedCallbacks(): Set<OnConnectResultCallback> = _callbacks

    protected fun notifyConnectivityAvailable(ssid: String) {
        getUiHandler().post {
            for (callback in getConnectivityChangedCallbacks()) {
                callback.onAvailable(ssid)
            }
        }
    }

    protected fun notifyConnectivityUnavailable(error: WifiConnectError) {
        getUiHandler().post {
            for (callback in getConnectivityChangedCallbacks()) {
                callback.onUnavailable(error)
            }
        }
    }

}