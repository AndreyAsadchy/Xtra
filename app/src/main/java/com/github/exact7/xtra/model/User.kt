package com.github.exact7.xtra.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class User(val id: String,
                  val name: String,
                  val token: String) : Parcelable

class LoggedIn(id: String, name: String, token: String) : User(id, name, token) {
    constructor(user: NotValidated) : this(user.id, user.name, user.token) //TODO inherit not validated?
}
class NotValidated(id: String, name: String, token: String) : User(id, name, token)
class NotLoggedIn : User("", "", "")
