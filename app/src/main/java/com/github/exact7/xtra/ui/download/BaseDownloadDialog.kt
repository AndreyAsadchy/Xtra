package com.github.exact7.xtra.ui.download

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.util.DownloadUtils

abstract class BaseDownloadDialog : DialogFragment(), Injectable {

    protected lateinit var prefs: SharedPreferences
    protected val sdCardPresent = DownloadUtils.isSdCardPresent

    abstract fun download()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults.indexOf(PackageManager.PERMISSION_DENIED) == -1) {
            download()
        } else {
            Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
        }
    }
}