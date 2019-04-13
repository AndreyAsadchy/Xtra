package com.github.exact7.xtra.model.kraken.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.exact7.xtra.model.chat.Emote
import com.google.gson.annotations.SerializedName

@Entity(tableName = "emotes")
data class Emote(
        @PrimaryKey
        val id: Int,
        @field:[SerializedName("code") ColumnInfo(name = "code")]
        override val name: String) : Emote
