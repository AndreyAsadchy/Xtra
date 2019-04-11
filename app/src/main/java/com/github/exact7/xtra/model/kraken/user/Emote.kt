package com.github.exact7.xtra.model.kraken.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emotes")
data class Emote(
        @PrimaryKey
        val id: Int,
        val code: String)
