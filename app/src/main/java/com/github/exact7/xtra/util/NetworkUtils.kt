package com.github.exact7.xtra.util

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtils {

    const val STATUS_NOT_CONNECTED = 0
    const val STATUS_WIFI = 1
    const val STATUS_MOBILE = 2

    fun isConnected(context: Context): Boolean =
            getConnectivityManager(context).activeNetworkInfo?.isConnectedOrConnecting == true

    fun getConnectivityStatus(context: Context): Int {
        val networkInfo = getConnectivityManager(context).activeNetworkInfo
        networkInfo?.let {
            return if (it.type == ConnectivityManager.TYPE_WIFI)
                STATUS_WIFI
            else
                STATUS_MOBILE
        }
        return STATUS_NOT_CONNECTED
    }

    private fun getConnectivityManager(context: Context) =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}