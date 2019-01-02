package com.github.exact7.xtra.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.github.exact7.xtra.model.offline.VideoRequest


@Dao
interface VideoRequestsDao {

    @Query("SELECT * FROM video_requests WHERE id = :id")
    fun get(id: Int): VideoRequest

    @Insert
    fun insert(request: VideoRequest): Long

    @Delete
    fun delete(request: VideoRequest)
}