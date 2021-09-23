package com.github.andreyasadchy.xtra.api

import com.github.andreyasadchy.xtra.model.chat.BttvEmotesResponse
import com.github.andreyasadchy.xtra.model.chat.FfzEmotesResponse
import com.github.andreyasadchy.xtra.model.chat.StvEmotesResponse
import com.github.andreyasadchy.xtra.model.chat.SubscriberBadgesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MiscApi {

    @GET("https://badges.twitch.tv/v1/badges/channels/{channelId}/display")
    suspend fun getSubscriberBadges(@Path("channelId") channelId: String): SubscriberBadgesResponse

    @GET("https://api.7tv.app/v2/emotes/global")
    suspend fun getGlobalStvEmotes(): Response<StvEmotesResponse>

    @GET("https://api.7tv.app/v2/users/{channel}/emotes")
    suspend fun getStvEmotes(@Path("channel") channel: String): Response<StvEmotesResponse>

    @GET("https://api.betterttv.net/2/emotes")
    suspend fun getGlobalBttvEmotes(): Response<BttvEmotesResponse>

    @GET("https://api.betterttv.net/2/channels/{channel}")
    suspend fun getBttvEmotes(@Path("channel") channel: String): Response<BttvEmotesResponse>

    @GET("https://api.frankerfacez.com/v1/set/global")
    suspend fun getGlobalFfzEmotes(): Response<FfzEmotesResponse>

    @GET("https://api.frankerfacez.com/v1/room/{channel}")
    suspend fun getFfzEmotes(@Path("channel") channel: String): Response<FfzEmotesResponse>
}