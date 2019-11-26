package com.github.exact7.xtra.repository

import com.github.exact7.xtra.api.IdApi
import com.github.exact7.xtra.db.EmotesDao
import com.github.exact7.xtra.model.id.ValidationResponse
import com.github.exact7.xtra.util.TwitchApiHelper
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepository"

@Singleton
class AuthRepository @Inject constructor(
        private val api: IdApi,
        private val emotesDao: EmotesDao) {

    fun validate(token: String): Single<ValidationResponse> {
        return api.validateToken("OAuth $token")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun revoke(token: String): Completable {
        return api.revokeToken(TwitchApiHelper.CLIENT_ID, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun deleteAllEmotes() {
        GlobalScope.launch {
            emotesDao.deleteAll()
        }
    }
}
