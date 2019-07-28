package com.github.exact7.xtra.model.kraken.game

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GameWrapper(
        val channels: Int,
        val viewers: Int,
        val game: Game) : Parcelable

