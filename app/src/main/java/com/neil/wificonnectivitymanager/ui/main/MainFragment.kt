package com.neil.wificonnectivitymanager.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.neil.wificonnectivitymanager.R
import com.wifi.connectivitymanager.OnConnectResultCallback
import com.wifi.connectivitymanager.WifiConnectivityManager
import com.wifi.connectivitymanager.WifiConnectivityManagerAndroid10Imp
import com.wifi.connectivitymanager.errors.WifiConnectError

@RequiresApi(Build.VERSION_CODES.Q)
class MainFragment : Fragment(), OnConnectResultCallback {

    private val LOG_TAG = "MainFragment"

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var wifiConnManager: WifiConnectivityManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.connect).setOnClickListener {connectTo()}
        view.findViewById<Button>(R.id.disconnect).setOnClickListener {disconnectFrom()}
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        wifiConnManager = WifiConnectivityManagerAndroid10Imp(context!!)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(LOG_TAG, "onRequestPermissionsResult");
        for ((i, value) in permissions.withIndex()) {
            val granted = grantResults[i] == PackageManager.PERMISSION_GRANTED
            Log.d(LOG_TAG, "has permission:" + permissions[i] + ":" + granted)
            if (!granted) {
                return
            }
        }
        connectTo()
    }

    private fun connectTo() {
        val items = arrayOf<CharSequence>(
            "Ayla-DevKit;",
            "QA-TPLINK;@ayla123",
            "SUNSEAAIOT-Office;sunseaaiot"
        )

        AlertDialog.Builder(context!!)
            .setTitle(R.string.app_name)
            .setSingleChoiceItems(
                items, 0
            ) { dialog, which ->
                val item = items[which].split(";")
                val ssid = item[0]
                val pwd = item[1]
                wifiConnManager.connect(ssid, pwd)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun disconnectFrom() {
        wifiConnManager.disconnect()
    }

    override fun onResume() {
        super.onResume()
        wifiConnManager.registerConnectResultCallback(this)
        if (missingPermissions().isNotEmpty()) {
            requestPermissions(missingPermissions())
        }
    }

    override fun onStop() {
        super.onStop()
        wifiConnManager.unregisterConnectResultCallback(this)
    }

    private fun missingPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context!!, permission)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(permission)
            }
        }

        return permissions
    }

    private fun requestPermissions(missingPermissions: List<String>) {
        if (missingPermissions.isEmpty()) {
            return
        }

        val permission = missingPermissions[0]
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission)) {
            Toast.makeText(context!!, "missing permission:$permission", Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(activity!!, missingPermissions.toTypedArray(), 0)
        }
    }

    override fun onAvailable(ssid: String) {
        Toast.makeText(context!!, "connected to $ssid", Toast.LENGTH_LONG).show()
    }

    override fun onUnavailable(error: WifiConnectError?) {
        Toast.makeText(context!!, "unable to connect due to error:$error", Toast.LENGTH_LONG).show()
    }
}