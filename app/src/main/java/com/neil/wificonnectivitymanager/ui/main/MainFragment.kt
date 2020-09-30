package com.neil.wificonnectivitymanager.ui.main

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.neil.wificonnectivitymanager.R
import com.wifi.connectivitymanager.WifiConnectivityManager
import com.wifi.connectivitymanager.WifiConnectivityManagerAndroid10Imp

@RequiresApi(Build.VERSION_CODES.Q)
class MainFragment : DialogFragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var wifiConnManager: WifiConnectivityManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        wifiConnManager = WifiConnectivityManagerAndroid10Imp(context!!)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

}