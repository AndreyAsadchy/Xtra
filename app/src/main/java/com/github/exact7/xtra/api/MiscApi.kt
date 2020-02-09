package com.github.exact7.xtra.api

import com.github.exact7.xtra.model.chat.BttvEmotesResponse
import com.github.exact7.xtra.model.chat.FfzRoomResponse
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MiscApi {

    @GET("https://badges.twitch.tv/v1/badges/channels/{channelId}/display")
    suspend fun getSubscriberBadges(@Path("channelId") channelId: String): SubscriberBadgesResponse

    @GET("https://api.betterttv.net/2/channels/{channel}")
    suspend fun getBttvEmotes(@Path("channel") channel: String): Response<BttvEmotesResponse>

    @GET("https://api.frankerfacez.com/v1/room/{channel}")
    suspend fun getFfzEmotes(@Path("channel") channel: String): Response<FfzRoomResponse>
}