package com.exact.xtra.repository

import com.exact.xtra.api.IdApi
import com.exact.xtra.model.id.ValidationResponse
import com.exact.xtra.util.TwitchApiHelper
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
        return api.validateToken("OAuth $token")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun revoke(token: String): Single<ResponseBody> {
        return api.revokeToken(TwitchApiHelper.clientId, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}
