package com.github.exact7.xtra.di

import androidx.work.Worker
import com.github.exact7.xtra.service.DownloadService
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class WorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(DownloadService::class)
    abstract fun bindDownloadWorker(downloadWorker: DownloadService): Worker
}