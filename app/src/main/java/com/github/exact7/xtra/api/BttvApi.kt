package com.github.exact7.xtra.api

import com.github.exact7.xtra.model.chat.BttvEmotesResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface BttvApi {

    @GET("channels/{channel}")
    fun getEmotes(@Path("channel") channel: String): Single<BttvEmotesResponse>
}