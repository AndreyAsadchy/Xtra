package com.github.exact7.xtra.di

import com.github.exact7.xtra.ui.download.DownloadService
import com.github.exact7.xtra.ui.player.AudioPlayerService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeDownloadService(): DownloadService

    @ContributesAndroidInjector
    abstract fun contributeAudioPlayerService(): AudioPlayerService
}
