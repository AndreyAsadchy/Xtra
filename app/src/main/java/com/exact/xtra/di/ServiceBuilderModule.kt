package com.exact.xtra.di

import com.exact.xtra.service.ClipDownloadService
import com.exact.xtra.service.VideoDownloadService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeVideoDownloadService(): VideoDownloadService

    @ContributesAndroidInjector
    abstract fun contributeClipDownloadService(): ClipDownloadService
}
