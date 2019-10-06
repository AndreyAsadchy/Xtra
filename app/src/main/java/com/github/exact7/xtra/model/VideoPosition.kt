package com.github.exact7.xtra.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_positions")
data class VideoPosition(
        @PrimaryKey
        val id: Long,
        val position: Long)