package com.github.exact7.xtra.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.work.Worker
import com.github.exact7.xtra.XtraApp
import com.github.exact7.xtra.util.LifecycleListener
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection

object AppInjector {

    fun init(xtraApp: XtraApp) {
        DaggerXtraComponent.builder().application(xtraApp).build().inject(xtraApp)
        xtraApp.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is Injectable) {
                    AndroidInjection.inject(activity)
                }
                if (activity is FragmentActivity) {
                    activity.supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                            if (f is Injectable) {
                                AndroidSupportInjection.inject(f)
                            }
                            if (f is LifecycleListener) {
                                xtraApp.setLifecycleListener(f)
                            }
                        }

                        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                            if (f is LifecycleListener) {
                                xtraApp.setLifecycleListener(null)
                            }
                        }
                    }, true)
                }
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }
        })
    }

    fun inject(worker: Worker) {
        val application = worker.applicationContext
        val workerInjector = (application as HasWorkerInjector).workerInjector()
        workerInjector.inject(worker)
    }
}
