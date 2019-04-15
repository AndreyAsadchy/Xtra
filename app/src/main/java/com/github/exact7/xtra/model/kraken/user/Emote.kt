package com.github.exact7.xtra.model.kraken.user

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.exact7.xtra.model.chat.Emote
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "emotes")
@Parcelize
data class Emote(
        @PrimaryKey
        val id: Int,
        @field:[SerializedName("code") ColumnInfo(name = "code")]
        override val name: String) : Emote(), Parcelable {

        override fun equals(other: Any?): Boolean = super.equals(other)
        override fun hashCode(): Int = super.hashCode()
}
