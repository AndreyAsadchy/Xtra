package com.exact.xtra.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.exact.xtra.XtraApp
import com.exact.xtra.ui.login.LoginActivity
import com.exact.xtra.util.LifecycleListener
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector

object AppInjector {

    fun init(xtraApp: XtraApp) {
        DaggerXtraComponent.builder().application(xtraApp).build().inject(xtraApp)
        xtraApp.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                handleActivity(activity, xtraApp)
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

    private fun handleActivity(activity: Activity, xtraApp: XtraApp) {
        if (activity is HasSupportFragmentInjector || activity is LoginActivity) { //TODO change
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
}
