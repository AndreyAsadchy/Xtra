package com.exact.xtra

import android.app.Activity
import android.app.Application
import android.app.Service
import androidx.lifecycle.ProcessLifecycleOwner
import com.exact.xtra.di.AppInjector
import com.exact.xtra.util.AppLifecycleObserver
import com.exact.xtra.util.LifecycleListener
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import javax.inject.Inject

class XtraApp : Application(), HasActivityInjector, HasServiceInjector {

    @Inject lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>
    @Inject lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>
    private val appLifecycleObserver = AppLifecycleObserver()


    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingActivityInjector
    }

    override fun serviceInjector(): AndroidInjector<Service> {
        return dispatchingServiceInjector
    }

    fun setLifecycleListener(listener: LifecycleListener) {
        appLifecycleObserver.setLifecycleListener(listener)
    }
}
