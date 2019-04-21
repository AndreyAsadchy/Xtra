package com.github.exact7.xtra.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.github.exact7.xtra.model.offline.OfflineVideo

@Dao
interface VideosDao {

    @Query("SELECT * FROM videos ORDER BY id DESC")
    fun getAll(): LiveData<List<OfflineVideo>>

    @Query("SELECT * FROM videos WHERE id = :id")
    fun getById(id: Int): OfflineVideo?

    @Insert
    fun insert(video: OfflineVideo): Long

    @Delete
    fun delete(video: OfflineVideo)

    @Query("SELECT * FROM videos WHERE downloaded = 0")
    fun getUnfinishedVideos(): List<OfflineVideo>

    @Update
    fun update(video: OfflineVideo)
}
