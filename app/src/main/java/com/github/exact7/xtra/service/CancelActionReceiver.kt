package com.github.exact7.xtra.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CancelActionReceiver : BroadcastReceiver() { //TODO inject offlinerepository

    override fun onReceive(context: Context, intent: Intent) {
        GlobalScope.launch {
//            Log.d(DownloadWorker.TAG, "Canceled download")
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            val request = queue.peek()
//            notificationManager.cancel(request.id)
//            request.canceled = true
//            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//            when (request) {
//                is VideoRequest -> {
//                    val iterator = request.downloadRequestToSegmentMap.keyIterator()
//                    while (iterator.hasNext()) {
//                        downloadManager.remove(iterator.next())
//                    }
//                }
//                is ClipRequest -> downloadManager.remove(request.downloadRequestId)
//            }
        }
    }
}