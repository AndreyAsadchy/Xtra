package com.github.exact7.xtra.util

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.MotionEvent
import com.github.exact7.xtra.R
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller

fun MotionEvent.isClick(outDownLocation: FloatArray): Boolean { //todo move to view package
    return when (actionMasked) {
        MotionEvent.ACTION_DOWN -> {
            outDownLocation[0] = x
            outDownLocation[1] = y
            false
        }
        MotionEvent.ACTION_UP -> {
            outDownLocation[0] in x - 50..x + 50 && outDownLocation[1] in y - 50..y + 50 && eventTime - downTime <= 500
        }
        else -> false
    }
}

fun Activity.installPlayServicesIfNeeded() {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
        ProviderInstaller.installIfNeededAsync(this, object : ProviderInstaller.ProviderInstallListener {
            override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
                GoogleApiAvailability.getInstance().apply {
                    if (isUserResolvableError(errorCode)) {
                        //Prompt the user to install/update/enable Google Play services.
                        showErrorDialogFragment(this@installPlayServicesIfNeeded, errorCode, 0)
                    } else {
                        toast(R.string.play_services_not_available)
                    }
                }
            }

            override fun onProviderInstalled() {}
        })
    }
}