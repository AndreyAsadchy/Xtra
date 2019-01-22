package com.github.exact7.xtra.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.github.exact7.xtra.model.NotValidated
import com.github.exact7.xtra.model.User

object Prefs {

    fun userPrefs(context: Context) = get(context, C.USER_PREFS)
    fun authPrefs(context: Context) = get(context, C.AUTH_PREFS)

    fun getUser(context: Context): User? {
        return with(userPrefs(context)) {
            val id = getString(C.USER_ID, null)
            if (id != null) {
                NotValidated(id, getString(C.USERNAME, null)!!, getString(C.TOKEN, null)!!)
            } else {
                null
            }
        }
    }

    fun saveUser(context: Context, id: String, name: String, token: String) {
        userPrefs(context).edit {
            putString(C.USER_ID, id)
            putString(C.USERNAME, name)
            putString(C.TOKEN, token)
        }
    }

    private fun get(context: Context, name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
}