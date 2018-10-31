package com.github.exact7.xtra.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
        val id: String,
        val name: String,
        val token: String) : Parcelable