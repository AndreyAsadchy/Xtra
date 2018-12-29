package com.github.exact7.xtra.di

import androidx.work.Worker
import dagger.MapKey
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention
@MapKey
annotation class WorkerKey(val value: KClass<out Worker>)
