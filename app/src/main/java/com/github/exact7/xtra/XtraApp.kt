package com.github.exact7.xtra

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.Worker
import com.github.exact7.xtra.di.AppInjector
import com.github.exact7.xtra.di.HasWorkerInjector
import com.github.exact7.xtra.service.DaggerWorkerFactory
import com.github.exact7.xtra.util.AppLifecycleObserver
import com.github.exact7.xtra.util.LifecycleListener
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class XtraApp : Application(), HasActivityInjector, HasWorkerInjector {

    @Inject lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>
    @Inject lateinit var workerInjector: DispatchingAndroidInjector<Worker>
    @Inject lateinit var daggerWorkerFactory: DaggerWorkerFactory
    private val appLifecycleObserver = AppLifecycleObserver()

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        initWorkManager()
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingActivityInjector
    }

    override fun workerInjector(): AndroidInjector<Worker> {
        return workerInjector
    }

    fun setLifecycleListener(listener: LifecycleListener?) {
        appLifecycleObserver.setLifecycleListener(listener)
    }

    private fun initWorkManager() {
        val config = Configuration.Builder()
                .setWorkerFactory(daggerWorkerFactory)
                .build()
        WorkManager.initialize(this, config)
    }
}
