package com.github.exact7.xtra.di

import com.github.exact7.xtra.ui.download.CancelActionReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BroadcastReceiverBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeCancelActionReceiver(): CancelActionReceiver
}
