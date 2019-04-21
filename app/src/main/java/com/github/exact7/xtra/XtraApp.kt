package com.github.exact7.xtra

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import androidx.core.content.edit
import androidx.lifecycle.ProcessLifecycleOwner
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.di.AppInjector
import com.github.exact7.xtra.util.AppLifecycleObserver
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.DisplayUtils
import com.github.exact7.xtra.util.LifecycleListener
import com.github.exact7.xtra.util.Prefs
import com.google.android.gms.ads.MobileAds
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasServiceInjector
import io.fabric.sdk.android.Fabric
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject

class XtraApp : Application(), HasActivityInjector, HasServiceInjector, HasBroadcastReceiverInjector {

    companion object {
        var INSTANCE: Application? = null
    }

    @Inject lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>
    @Inject lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>
    @Inject lateinit var dispatchingBroadcastReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>
    private val appLifecycleObserver = AppLifecycleObserver()

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        AppInjector.init(this)
        Fabric.with(this, Crashlytics())
        MobileAds.initialize(this, "ca-app-pub-1890646946349307~2827896321")
        RxJavaPlugins.setErrorHandler { Crashlytics.logException(it) }
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        val prefs = Prefs.get(this)
        val all = prefs.all
        if (all["chatWidth"] == null) {
            val chatWidth = DisplayUtils.calculateLandscapeWidthByPercent(25)
            prefs.edit {
                putInt("chatWidth", 25)
                putInt(C.LANDSCAPE_CHAT_WIDTH, chatWidth)
            }
        } else if (all["chatWidth"] is String) { //TODO remove after all devices updated to 1.1.9
            prefs.edit {
                remove("chatWidth")
                putInt("chatWidth", prefs.getInt(C.LANDSCAPE_CHAT_WIDTH, -1))
            }
        }
        if (all[C.DOWNLOAD_STORAGE] is Boolean) { //TODO remove after all devices updated to 1.1.12
            prefs.edit {
                remove(C.DOWNLOAD_STORAGE)
                putInt(C.DOWNLOAD_STORAGE, if (all[C.DOWNLOAD_STORAGE] == true) 0 else 1)
            }
        }
        if (all[C.PORTRAIT_PLAYER_HEIGHT] == null) {
            prefs.edit {
                putInt(C.PORTRAIT_PLAYER_HEIGHT, DisplayUtils.calculatePortraitHeightByPercent(33))
            }
        }
    }

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingActivityInjector
    override fun serviceInjector(): AndroidInjector<Service> = dispatchingServiceInjector
    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> = dispatchingBroadcastReceiverInjector

    fun setLifecycleListener(listener: LifecycleListener?) {
        appLifecycleObserver.setLifecycleListener(listener)
    }
}
