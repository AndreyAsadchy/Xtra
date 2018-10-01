package com.exact.twitch.repository

import com.exact.twitch.api.IdApi
import com.exact.twitch.model.id.ValidationResponse
import com.exact.twitch.util.TwitchApiHelper
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
        private val api: IdApi) {

    companion object {
        private const val TAG = "AuthRepository"
    }

    fun validate(token: String): Single<ValidationResponse> {
        return api.validateToken("OAuth $token").observeOn(AndroidSchedulers.mainThread())
    }

    fun revoke(token: String): Single<ResponseBody> {
        return api.revokeToken(TwitchApiHelper.clientId, token).observeOn(AndroidSchedulers.mainThread())
    }
}
