package com.github.exact7.xtra.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.NotLoggedIn
import com.github.exact7.xtra.model.NotValidated
import com.github.exact7.xtra.model.User

object Prefs {

    fun get(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getUser(context: Context): User {
        return with(get(context)) {
            val id = getString(C.USER_ID, null)
            if (id != null) {
                val name = getString(C.USERNAME, null)!!
                val token = getString(C.TOKEN, null)!!
                if (TwitchApiHelper.validated) {
                    LoggedIn(id, name, token)
                } else {
                    NotValidated(id, name, token)
                }
            } else {
                NotLoggedIn()
            }
        }
    }

    fun setUser(context: Context, user: User?) {
        get(context).edit {
            putString(C.USER_ID, user?.id)
            putString(C.USERNAME, user?.name)
            putString(C.TOKEN, user?.token)
        }
    }
}