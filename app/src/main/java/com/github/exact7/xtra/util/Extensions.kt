package com.github.exact7.xtra.util

import android.app.Activity
import android.content.Intent
import android.os.Build
import com.github.exact7.xtra.R
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller

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