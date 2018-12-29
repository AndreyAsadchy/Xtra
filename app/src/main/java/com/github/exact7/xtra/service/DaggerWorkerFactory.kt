package com.github.exact7.xtra.service

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.github.exact7.xtra.di.AppInjector
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class DaggerWorkerFactory(private val creators: Map<Class<out Worker>, @JvmSuppressWildcards Provider<Worker>>) : WorkerFactory() {

    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
        creators[Class.forName(workerClassName)]?.get()
        val constructor =
                .asSubclass(Worker::class.java)
                .getDeclaredConstructor(Context::class.java, WorkerParameters::class.java)
        return constructor.newInstance(appContext, workerParameters).apply { AppInjector.inject(this) }
    }
}