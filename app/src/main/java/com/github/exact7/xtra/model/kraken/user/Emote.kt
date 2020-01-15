package com.github.exact7.xtra.model.kraken.user

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.util.C.TWITCH_EMOTES_URL
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "emotes")
@Parcelize
class Emote(
        @PrimaryKey
        val id: Int,
        @field:[SerializedName("code") ColumnInfo(name = "code", collate = ColumnInfo.NOCASE)]
        override val name: String) : Emote(), Parcelable {

        @Ignore
        @IgnoredOnParcel
        override val url: String = "$TWITCH_EMOTES_URL$id/2.0"
}