package com.exact.twitch.di

import com.exact.twitch.service.MediaDownloadService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeMediaDownloadService(): MediaDownloadService
}
