package com.exact.xtra.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.exact.xtra.model.OfflineVideo

@Dao
interface VideosDao {

    @Query("SELECT * FROM videos ORDER BY download_date DESC")
    fun getAll(): DataSource.Factory<Int, OfflineVideo>

//    @Query("SELECT * FROM videos WHERE id > :offset ORDER BY download_date LIMIT :limit")
//    fun getAfter(offset: Int, limit: Int): Single<List<OfflineVideo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(video: OfflineVideo)

    @Delete
    fun delete(video: OfflineVideo)
}
