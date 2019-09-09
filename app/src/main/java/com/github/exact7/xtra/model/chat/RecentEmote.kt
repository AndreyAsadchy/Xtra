package com.github.exact7.xtra.model.chat

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "recent_emotes")
@Parcelize
data class RecentEmote(
        @PrimaryKey
        override val name: String,
        override val url: String,
        @ColumnInfo(name = "used_at")
        val usedAt: Long) : Emote(), Parcelable {

    override fun equals(other: Any?): Boolean = super.equals(other)
    override fun hashCode(): Int = super.hashCode()

    companion object {
        const val MAX_SIZE = 50
    }
}
