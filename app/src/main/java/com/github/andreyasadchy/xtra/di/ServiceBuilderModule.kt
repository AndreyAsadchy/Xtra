package com.github.andreyasadchy.xtra.di

import com.github.andreyasadchy.xtra.ui.download.DownloadService
import com.github.andreyasadchy.xtra.ui.player.AudioPlayerService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeDownloadService(): DownloadService

    @ContributesAndroidInjector
    abstract fun contributeAudioPlayerService(): AudioPlayerService
}
