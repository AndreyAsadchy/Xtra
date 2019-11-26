package com.github.exact7.xtra.model

import android.content.Context
import androidx.core.content.edit
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.prefs

sealed class User(val id: String,
                  val name: String,
                  val token: String,
                  val newToken: Boolean) {

    companion object {
        fun get(context: Context): User {
            return with(context.prefs()) {
                val id = getString(C.USER_ID, null)
                if (id != null) {
                    val name = getString(C.USERNAME, null)!!
                    val token = getString(C.TOKEN, null)!!
                    val newToken = getBoolean(C.NEW_TOKEN, false)
                    if (TwitchApiHelper.validated) {
                        LoggedIn(id, name, token, newToken)
                    } else {
                        NotValidated(id, name, token, newToken)
                    }
                } else {
                    NotLoggedIn()
                }
            }
        }

        fun set(context: Context, user: User?) {
            context.prefs().edit {
                if (user != null) {
                    putString(C.USER_ID, user.id)
                    putString(C.USERNAME, user.name)
                    putString(C.TOKEN, user.token)
                    putBoolean(C.NEW_TOKEN, user.newToken)
                } else {
                    putString(C.USER_ID, null)
                    putString(C.USERNAME, null)
                    putString(C.TOKEN, null)
                    putBoolean(C.NEW_TOKEN, true)
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (name != other.name) return false
        if (token != other.token) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + token.hashCode()
        return result
    }
}

class LoggedIn(id: String, name: String, token: String, newToken: Boolean) : User(id, name, token, newToken) {
    constructor(user: NotValidated) : this(user.id, user.name, user.token, user.newToken)
}
class NotValidated(id: String, name: String, token: String, newToken: Boolean) : User(id, name, token, newToken)
class NotLoggedIn : User("", "", "", false)