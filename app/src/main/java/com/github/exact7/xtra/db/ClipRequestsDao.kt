package com.github.exact7.xtra.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.github.exact7.xtra.model.offline.ClipRequest

@Dao
interface ClipRequestsDao {

    @Query("SELECT * FROM clip_requests WHERE id = :id")
    fun get(id: Int): ClipRequest

    @Insert
    fun insert(request: ClipRequest): Long

    @Delete
    fun delete(request: ClipRequest)
}