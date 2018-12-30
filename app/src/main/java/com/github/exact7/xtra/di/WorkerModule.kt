package com.github.exact7.xtra.di

import androidx.work.Worker
import com.github.exact7.xtra.service.DownloadWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class WorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(DownloadWorker::class)
    abstract fun bindDownloadWorker(downloadWorker: DownloadWorker): Worker
}