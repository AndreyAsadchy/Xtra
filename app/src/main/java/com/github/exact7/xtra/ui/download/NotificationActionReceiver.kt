package com.github.exact7.xtra.ui.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.exact7.xtra.util.FetchProvider
import dagger.android.AndroidInjection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var fetchProvider: FetchProvider

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotifActionReceiver", "Canceled download")
        AndroidInjection.inject(this, context)
        GlobalScope.launch {
            with(fetchProvider.get()) {
                cancelAll()
                deleteAll()
            }
        }
    }
}