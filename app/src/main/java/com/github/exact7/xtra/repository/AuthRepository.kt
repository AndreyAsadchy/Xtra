package com.github.exact7.xtra.repository

import com.github.exact7.xtra.api.IdApi
import com.github.exact7.xtra.db.EmotesDao
import com.github.exact7.xtra.model.id.ValidationResponse
import com.github.exact7.xtra.util.TwitchApiHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepository"

@Singleton
class AuthRepository @Inject constructor(
        private val api: IdApi,
        private val emotesDao: EmotesDao) {

    suspend fun validate(token: String): ValidationResponse = api.validateToken("OAuth $token")

    suspend fun revoke(token: String) = api.revokeToken(TwitchApiHelper.CLIENT_ID, token)

    fun deleteAllEmotes() {
        GlobalScope.launch {
            emotesDao.deleteAll()
        }
    }
}
