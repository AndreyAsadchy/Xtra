package com.github.exact7.xtra.ui.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.util.FetchProvider
import dagger.android.AndroidInjection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val KEY_VIDEO_ID = "videoId"
    }

    @Inject lateinit var fetchProvider: FetchProvider

    override fun onReceive(context: Context, intent: Intent) {
        val videoId = intent.getIntExtra(KEY_VIDEO_ID, 0)
        Log.d("NotifActionReceiver", "Canceled download. Id: $videoId")
        AndroidInjection.inject(this, context)
        GlobalScope.launch {
            try {
                DownloadService.activeRequests.remove(videoId)
                fetchProvider.get(videoId).deleteAll()
            } catch (e: Exception) {
                Crashlytics.logException(e)
            }
        }
    }
}