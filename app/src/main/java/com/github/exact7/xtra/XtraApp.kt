package com.github.exact7.xtra

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.di.AppInjector
import com.github.exact7.xtra.util.AppLifecycleObserver
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.DisplayUtils
import com.github.exact7.xtra.util.LifecycleListener
import com.github.exact7.xtra.util.prefs
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.fabric.sdk.android.Fabric
import io.reactivex.plugins.RxJavaPlugins
import java.net.UnknownHostException
import javax.inject.Inject


class XtraApp : Application(), HasAndroidInjector {

    companion object {
        lateinit var INSTANCE: Application
    }

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    private val appLifecycleObserver = AppLifecycleObserver()

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        AppInjector.init(this)
        Fabric.with(this, Crashlytics())
        RxJavaPlugins.setErrorHandler {
            if (it !is UnknownHostException) {
                Crashlytics.logException(it)
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        val prefs = prefs()
        val all = prefs.all
        if (all["chatWidth"] == null) {
            val chatWidth = DisplayUtils.calculateLandscapeWidthByPercent(this, 25)
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
                putInt(C.PORTRAIT_PLAYER_HEIGHT, DisplayUtils.calculatePortraitHeightByPercent(this@XtraApp, 33))
            }
        }
        if (all[C.THEME] is Boolean) { //TODO remove after all devices updated to 1.3.0
            prefs.edit {
                remove(C.THEME)
                putString(C.THEME, if (all[C.THEME] == true) "0" else "2")
            }
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (BuildConfig.DEBUG) {
            MultiDex.install(this)
        }
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    fun addLifecycleListener(listener: LifecycleListener) {
        appLifecycleObserver.addListener(listener)
    }

    fun removeLifecycleListener(listener: LifecycleListener) {
        appLifecycleObserver.removeListener(listener)
    }
}
