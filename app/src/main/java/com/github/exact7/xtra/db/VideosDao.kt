package com.github.exact7.xtra.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.github.exact7.xtra.model.OfflineVideo

@Dao
interface VideosDao {

    @Query("SELECT * FROM videos ORDER BY download_date DESC")
    fun getAll(): DataSource.Factory<Int, OfflineVideo>

    @Insert
    fun insert(video: OfflineVideo)

    @Delete
    fun delete(video: OfflineVideo)
}
