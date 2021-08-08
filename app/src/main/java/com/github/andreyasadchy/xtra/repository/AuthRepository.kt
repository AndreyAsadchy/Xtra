package com.github.andreyasadchy.xtra.repository

import com.github.andreyasadchy.xtra.api.IdApi
import com.github.andreyasadchy.xtra.db.EmotesDao
import com.github.andreyasadchy.xtra.model.id.ValidationResponse
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepository"

@Singleton
class AuthRepository @Inject constructor(
        private val api: IdApi,
        private val emotesDao: EmotesDao) {

    suspend fun validate(token: String): ValidationResponse? = withContext(Dispatchers.IO) {
        api.validateToken(TwitchApiHelper.addTokenPrefix(token))
    }

    suspend fun revoke(token: String) = withContext(Dispatchers.IO) {
        api.revokeToken(TwitchApiHelper.CLIENT_ID, token)
    }

    fun deleteAllEmotes() {
        GlobalScope.launch {
            emotesDao.deleteAll()
        }
    }
}
