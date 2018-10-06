package com.exact.xtra.di

import com.exact.xtra.service.MediaDownloadService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeMediaDownloadService(): MediaDownloadService
}
