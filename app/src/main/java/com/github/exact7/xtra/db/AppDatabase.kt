package com.github.exact7.xtra.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.exact7.xtra.model.offline.OfflineVideo

@Database(entities = [OfflineVideo::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun videos(): VideosDao
}
