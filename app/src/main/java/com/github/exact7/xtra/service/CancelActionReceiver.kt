package com.github.exact7.xtra.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.github.exact7.xtra.util.DownloadUtils

class CancelActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CancelActionReceiver", "Canceled download")
        DownloadUtils.getFetch(context).cancelAll()
        NotificationManagerCompat.from(context).cancel(intent.getIntExtra("id", 0))
    }
}