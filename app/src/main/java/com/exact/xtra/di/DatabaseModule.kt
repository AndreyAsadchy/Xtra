package com.exact.xtra.di

import android.app.Application
import androidx.room.Room
import com.exact.xtra.db.AppDatabase
import com.exact.xtra.db.VideosDao
import com.exact.xtra.repository.OfflineRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun providesAppDatabase(application: Application): AppDatabase =
        Room.databaseBuilder(application, AppDatabase::class.java, "database").build()

    @Singleton
    @Provides
    fun providesVideosDao(database: AppDatabase): VideosDao = database.videos()

    @Singleton
    @Provides
    fun providesRepository(videosDao: VideosDao): OfflineRepository = OfflineRepository(videosDao)
}
