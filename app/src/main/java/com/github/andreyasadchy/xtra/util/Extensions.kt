package com.github.andreyasadchy.xtra.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import com.github.andreyasadchy.xtra.R
import com.google.android.gms.security.ProviderInstaller

fun Activity.installPlayServicesIfNeeded() {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
        ProviderInstaller.installIfNeededAsync(this, object : ProviderInstaller.ProviderInstallListener {
            override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
                if (recoveryIntent != null) {
                    try {
                        startActivity(recoveryIntent)
                    } catch (e: ActivityNotFoundException) {
                        toast(R.string.play_services_not_available)
                    }
                } else {
                    toast(R.string.play_services_not_available)
                }
            }

            override fun onProviderInstalled() {}
        })
    }
}