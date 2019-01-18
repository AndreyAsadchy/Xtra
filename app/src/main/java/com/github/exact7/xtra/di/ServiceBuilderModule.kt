package com.github.exact7.xtra.di

import com.github.exact7.xtra.ui.download.DownloadService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeDownloadService(): DownloadService
}
