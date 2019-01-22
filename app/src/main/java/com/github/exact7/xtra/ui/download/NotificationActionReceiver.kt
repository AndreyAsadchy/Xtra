package com.github.exact7.xtra.ui.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.exact7.xtra.util.FetchProvider
import dagger.android.AndroidInjection
import javax.inject.Inject

class NotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var fetchProvider: FetchProvider

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotifActionReceiver", "Canceled download")
        AndroidInjection.inject(this, context)
        fetchProvider.get().deleteAll()
    }
}