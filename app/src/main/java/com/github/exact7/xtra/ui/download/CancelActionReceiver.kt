package com.github.exact7.xtra.ui.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.exact7.xtra.util.DownloadUtils

class CancelActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CancelActionReceiver", "Canceled download")
        DownloadUtils.getFetch(context).deleteAll()
    }
}