package com.github.exact7.xtra

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.github.exact7.xtra.di.AppInjector
import com.github.exact7.xtra.util.AppLifecycleObserver
import com.github.exact7.xtra.util.LifecycleListener
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class XtraApp : Application(), HasActivityInjector {

    @Inject lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>
    private val appLifecycleObserver = AppLifecycleObserver()

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingActivityInjector
    }

    fun setLifecycleListener(listener: LifecycleListener?) {
        appLifecycleObserver.setLifecycleListener(listener)
    }
}
