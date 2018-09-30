package com.exact.twitch.di

import android.app.Application
import com.exact.twitch.TwitchApp
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, TwitchModule::class, ActivityBuilderModule::class, DatabaseModule::class, ServiceBuilderModule::class])
interface TwitchComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): TwitchComponent
    }

    fun inject(twitchApp: TwitchApp)
}
