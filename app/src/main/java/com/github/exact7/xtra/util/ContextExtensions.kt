package com.github.exact7.xtra.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Build
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.github.exact7.xtra.R

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

fun Activity.applyTheme(): String {
    val theme = prefs().getString(C.THEME, "0")!!
    setTheme(when(theme) {
        "0" -> R.style.DarkTheme
        "1" -> R.style.AmoledTheme
        else -> R.style.LightTheme
    })
    return theme
}

val Context.isInPortraitOrientation
    get() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

val Context.isInLandscapeOrientation
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

val Context.isActivityResumed
    get() = this !is Activity || !((Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN && isDestroyed) || isFinishing)

fun Context.toast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}

fun Context.shortToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun Context.toast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun Context.shortToast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}