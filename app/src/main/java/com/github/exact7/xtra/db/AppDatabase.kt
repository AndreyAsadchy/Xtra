package com.github.exact7.xtra.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.exact7.xtra.model.offline.ClipRequest
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.model.offline.VideoRequest

@Database(entities = [OfflineVideo::class, VideoRequest::class, ClipRequest::class], version = 2, exportSchema = false)
@TypeConverters(com.github.exact7.xtra.db.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun videos(): VideosDao
    abstract fun videoRequests(): VideoRequestsDao
    abstract fun clipRequests(): ClipRequestsDao
}
