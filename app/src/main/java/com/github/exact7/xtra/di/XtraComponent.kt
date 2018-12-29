package com.github.exact7.xtra.di

import android.app.Application
import com.github.exact7.xtra.XtraApp
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, XtraModule::class, ActivityBuilderModule::class, DatabaseModule::class, WorkerModule::class])
interface XtraComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): XtraComponent
    }

    fun inject(xtraApp: XtraApp)
}
