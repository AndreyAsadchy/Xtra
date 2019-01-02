package com.github.exact7.xtra.di

import android.app.Application
import androidx.room.Room
import com.github.exact7.xtra.db.AppDatabase
import com.github.exact7.xtra.db.ClipRequestsDao
import com.github.exact7.xtra.db.VideoRequestsDao
import com.github.exact7.xtra.db.VideosDao
import com.github.exact7.xtra.repository.OfflineRepository
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
    fun providesVideoRequestsDao(database: AppDatabase): VideoRequestsDao = database.videoRequests()

    @Singleton
    @Provides
    fun providesClipRequestsDao(database: AppDatabase): ClipRequestsDao = database.clipRequests()

    @Singleton
    @Provides
    fun providesRepository(videosDao: VideosDao, videoRequestsDao: VideoRequestsDao, clipRequestsDao: ClipRequestsDao): OfflineRepository = OfflineRepository(videosDao, videoRequestsDao, clipRequestsDao)
}
