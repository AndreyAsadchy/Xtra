package com.github.exact7.xtra.service

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.github.exact7.xtra.di.WorkerSubcomponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DaggerWorkerFactory @Inject constructor(
        private val workerSubcomponent: WorkerSubcomponent.Builder) : WorkerFactory() {

    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
        return workerSubcomponent.workerParameters(workerParameters).build().run {
            val workers = workers()
            val workerClass = Class.forName(workerClassName).asSubclass(Worker::class.java)
            val creator = workers[workerClass] ?: workers.entries.firstOrNull {
                workerClass.isAssignableFrom((it.key))
            }?.value ?: throw IllegalArgumentException("Unknown worker class $workerClass")
            creator.get()
        }
    }
}