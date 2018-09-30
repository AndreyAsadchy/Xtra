package com.exact.twitch.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.exact.twitch.model.OfflineVideo

@Database(entities = [(OfflineVideo::class)], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun videos(): VideosDao
}
