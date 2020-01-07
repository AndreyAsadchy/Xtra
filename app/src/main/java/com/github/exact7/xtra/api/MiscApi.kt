package com.github.exact7.xtra.api

import com.github.exact7.xtra.model.chat.BttvEmotesResponse
import com.github.exact7.xtra.model.chat.FfzRoomResponse
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MiscApi {

    @GET("https://badges.twitch.tv/v1/badges/channels/{channelId}/display")
    fun getSubscriberBadges(@Path("channelId") channelId: String): Single<SubscriberBadgesResponse>

    @GET("https://api.betterttv.net/2/channels/{channel}")
    fun getBttvEmotes(@Path("channel") channel: String): Single<Response<BttvEmotesResponse>>

    @GET("https://api.frankerfacez.com/v1/room/{channel}")
    fun getFfzEmotes(@Path("channel") channel: String): Single<Response<FfzRoomResponse>>
}