package com.github.andreyasadchy.xtra.model.helix.emote

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.ui.view.chat.emoteQuality
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel

@Entity(tableName = "emotes")
class Emote(
        @PrimaryKey
        val id: Int,
        @field:[SerializedName("code") ColumnInfo(name = "code", collate = ColumnInfo.NOCASE)]
        override val name: String) : Emote() {

        @Ignore
        @IgnoredOnParcel
        override val url: String = "https://static-cdn.jtvnw.net/emoticons/v2/$name/default/dark/$emoteQuality.0"
}