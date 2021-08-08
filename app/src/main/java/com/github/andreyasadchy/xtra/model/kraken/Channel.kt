package com.github.andreyasadchy.xtra.model.kraken

import android.os.Parcelable

interface Channel : Parcelable {
    val id: String
    val logo: String
    val name: String
    val displayName: String
}