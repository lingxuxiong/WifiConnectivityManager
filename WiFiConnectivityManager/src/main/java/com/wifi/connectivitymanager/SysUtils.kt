package com.wifi.connectivitymanager

import android.content.Context

class SysUtils {

    companion object {

        @JvmStatic
        fun getTargetSdkVersion(context: Context): Int {
            try {
                val pm = context.packageManager
                val ai = pm.getApplicationInfo(context.packageName, 0)
                return ai?.targetSdkVersion ?: 0
            } catch (e: Exception) {
            }

            return 0
        }
    }
}