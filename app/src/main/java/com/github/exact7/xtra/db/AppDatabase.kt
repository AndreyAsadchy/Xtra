package com.github.exact7.xtra.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.exact7.xtra.model.kraken.user.Emote
import com.github.exact7.xtra.model.offline.OfflineVideo

@Database(entities = [OfflineVideo::class, Emote::class], version = 4)
abstract class AppDatabase : RoomDatabase() {

    abstract fun videos(): VideosDao
    abstract fun emotes(): EmotesDao
}
