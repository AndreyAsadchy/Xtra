package com.github.exact7.xtra.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CancelActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
//        Log.d(DownloadWorker.TAG, "Canceled download")
//        val requestId = intent.getIntExtra("requestId", 0)
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.cancel(requestId)
//        val request = DownloadWorker.map[requestId]!!
//        request.canceled = true
//        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        when (request) {
//            is VideoRequest -> {
//                with(request) {
//                    val iterator = downloadRequestToSegmentMap.keyIterator()
//                    while (iterator.hasNext()) {
//                        downloadManager.remove(iterator.next())
//                    }
//                }
//            }
//            is ClipRequest -> downloadManager.remove(request.downloadRequestId)
//        }
    }
}