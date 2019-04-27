package com.github.exact7.xtra.util

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.preference.PreferenceManager

fun Context.isNetworkAvailable() = getConnectivityManager(this).activeNetworkInfo?.isConnectedOrConnecting == true

fun Context.getNetworkStatus(): NetworkStatus {
    return getConnectivityManager(this).activeNetworkInfo?.let {
        if (it.type == ConnectivityManager.TYPE_WIFI) NetworkStatus.STATUS_WIFI else NetworkStatus.STATUS_MOBILE
    } ?: NetworkStatus.STATUS_NOT_CONNECTED
}

private fun getConnectivityManager(context: Context) = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

fun Context.prefs(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
