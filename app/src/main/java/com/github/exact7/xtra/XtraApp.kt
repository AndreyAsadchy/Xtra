package com.github.exact7.xtra

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.di.AppInjector
import com.github.exact7.xtra.util.AppLifecycleObserver
import com.github.exact7.xtra.util.LifecycleListener
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.fabric.sdk.android.Fabric
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
