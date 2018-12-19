package com.github.exact7.xtra

import android.app.Activity
import android.app.Service
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.github.exact7.xtra.di.AppInjector
import com.github.exact7.xtra.util.AppLifecycleObserver
import com.github.exact7.xtra.util.LifecycleListener
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import javax.inject.Inject

class XtraApp : MultiDexApplication(), HasActivityInjector, HasServiceInjector {

    @Inject lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>
    @Inject lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>
    private val appLifecycleObserver = AppLifecycleObserver()

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        Thread.setDefaultUncaughtExceptionHandler { t, e -> Log.e("Test", e?.message, e.cause) }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingActivityInjector
    }

    override fun serviceInjector(): AndroidInjector<Service> {
        return dispatchingServiceInjector
    }

    fun setLifecycleListener(listener: LifecycleListener?) {
        appLifecycleObserver.setLifecycleListener(listener)
    }
}
