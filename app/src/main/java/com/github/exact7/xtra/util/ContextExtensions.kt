package com.github.exact7.xtra.util

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.util.TypedValue
import androidx.preference.PreferenceManager

val Context.isNetworkAvailable get() = getConnectivityManager(this).activeNetworkInfo?.isConnectedOrConnecting == true

val Context.networkStatus: NetworkStatus
    get() {
        return getConnectivityManager(this).activeNetworkInfo?.let {
            if (it.type == ConnectivityManager.TYPE_WIFI) NetworkStatus.STATUS_WIFI else NetworkStatus.STATUS_MOBILE
        } ?: NetworkStatus.STATUS_NOT_CONNECTED
    }

private fun getConnectivityManager(context: Context) = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

fun Context.prefs(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

fun Context.convertDpToPixels(dp: Float) =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.resources.displayMetrics).toInt()

fun Context.convertPixelsToDp(pixels: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pixels, this.resources.displayMetrics).toInt()

val Context.displayDensity
    get() = this.resources.displayMetrics.density