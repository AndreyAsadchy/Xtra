package com.github.exact7.xtra.model.kraken

import android.os.Parcelable

interface Channel : Parcelable {
    val id: String
    val logo: String
    val name: String
    val displayName: String
}