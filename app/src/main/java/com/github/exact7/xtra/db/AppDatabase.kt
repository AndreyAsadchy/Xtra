package com.github.exact7.xtra.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.exact7.xtra.model.chat.RecentEmote
import com.github.exact7.xtra.model.kraken.user.Emote
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.model.offline.Request

@Database(entities = [OfflineVideo::class, Emote::class, Request::class, RecentEmote::class], version = 6)
abstract class AppDatabase : RoomDatabase() {

    abstract fun videos(): VideosDao
    abstract fun emotes(): EmotesDao
    abstract fun requests(): RequestsDao
    abstract fun recentEmotes(): RecentEmotesDao
}
