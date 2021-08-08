package com.github.andreyasadchy.xtra.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.github.andreyasadchy.xtra.model.offline.Request

@Dao
interface RequestsDao {

    @Query("SELECT * FROM requests")
    suspend fun getAll(): List<Request>

    @Insert
    suspend fun insert(request: Request)

    @Delete
    suspend fun delete(request: Request)
}
