package com.github.exact7.xtra.di

import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Provider

@Subcomponent(modules = [WorkerModule::class])
interface WorkerSubcomponent {

    fun workers(): Map<Class<out Worker>, Provider<Worker>>

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun workerParameters(params: WorkerParameters): Builder

        fun build(): WorkerSubcomponent
    }
}