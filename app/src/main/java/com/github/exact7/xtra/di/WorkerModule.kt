package com.github.exact7.xtra.di

import androidx.work.Worker
import androidx.work.WorkerFactory
import com.github.exact7.xtra.service.DaggerWorkerFactory
import com.github.exact7.xtra.service.DownloadWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class WorkerModule {

    @Binds
    abstract fun bindDaggerWorkerFactory(daggerWorkerFactory: DaggerWorkerFactory): WorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(DownloadWorker::class)
    abstract fun bindDownloadWorker(downloadWorker: DownloadWorker): Worker
}