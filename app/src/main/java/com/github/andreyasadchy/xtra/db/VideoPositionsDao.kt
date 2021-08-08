package com.github.andreyasadchy.xtra.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.andreyasadchy.xtra.model.VideoPosition

@Dao
interface VideoPositionsDao {

    @Query("SELECT * FROM video_positions")
    fun getAll(): LiveData<List<VideoPosition>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(position: VideoPosition)
}