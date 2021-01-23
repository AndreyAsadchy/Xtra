package com.github.exact7.xtra.repository

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.github.exact7.xtra.api.ApiService
import com.github.exact7.xtra.api.GraphQLApi
import com.github.exact7.xtra.api.MiscApi
import com.github.exact7.xtra.api.UsherApi
import com.github.exact7.xtra.db.EmotesDao
import com.github.exact7.xtra.db.RecentEmotesDao
import com.github.exact7.xtra.db.VideoPositionsDao
import com.github.exact7.xtra.model.VideoPosition
import com.github.exact7.xtra.model.chat.BttvEmotesResponse
import com.github.exact7.xtra.model.chat.FfzEmotesResponse
import com.github.exact7.xtra.model.chat.RecentEmote
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.util.TwitchApiHelper
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.set
import kotlin.random.Random

private const val TAG = "PlayerRepository"

@Singleton
class PlayerRepository @Inject constructor(
        private val api: ApiService,
        private val usher: UsherApi,
        private val misc: MiscApi,
        private val graphQL: GraphQLApi,
        private val emotes: EmotesDao,
        private val recentEmotes: RecentEmotesDao,
        private val videoPositions: VideoPositionsDao) {

    suspend fun loadStreamPlaylist(channelName: String, clientId: String, tokenList: String, playerType: String): Uri = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting stream playlist for channel $channelName. Client id: $clientId. Player type: $playerType")

        //removes "commercial break in progress"
//        val uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 16)
//        val apiToken = UUID.randomUUID().toString().replace("-", "").substring(0, 32)
//        val serverSessionId = UUID.randomUUID().toString().replace("-", "").substring(0, 32)
//        val cookie = "unique_id=$uniqueId; unique_id_durable=$uniqueId; twitch.lohp.countryCode=BY; api_token=twilight.$apiToken; server_session_id=$serverSessionId"

//        val accessToken = api.getStreamAccessToken(clientId, cookie, channelName, token, playerType)
        val array = JsonArray(1)
        val streamAccessTokenOperation = JsonObject().apply {
            addProperty("operationName", "PlaybackAccessToken")
            add("variables", JsonObject().apply {
                addProperty("isLive", true)
                addProperty("isVod", false)
                addProperty("login", channelName)
                addProperty("playerType", playerType)
                addProperty("vodID", "")
            })
            add("extensions", JsonObject().apply {
                add("persistedQuery", JsonObject().apply {
                    addProperty("version", 1)
                    addProperty("sha256Hash", "0828119ded1c13477966434e15800ff57ddacf13ba1911c129dc2200705b0712")
                })
            })
        }
        array.add(streamAccessTokenOperation)

        val shuffled = tokenList.split(",").shuffled()
        for (token in shuffled) {
            try {
                val accessToken = graphQL.getStreamAccessToken(TwitchApiHelper.addTokenPrefix(token), array)
                val options = HashMap<String, String>()
//                options["token"] = accessToken.token
//                options["sig"] = accessToken.sig
                options["token"] = accessToken.token
                options["sig"] = accessToken.signature
                options["allow_source"] = "true"
                options["allow_audio_only"] = "true"
                options["type"] = "any"
                options["p"] = Random.nextInt(999999).toString()
                options["fast_bread"] = "true" //low latency

                //not working anyway
//                options["server_ads"] = "false"
//                options["show_ads"] = "false"
                val playlist = usher.getStreamPlaylist(channelName, options)
                return@withContext playlist.raw().request().url().toString().toUri()
            } catch (e: HttpException) {
                if (e.code() != 401) throw e
                Log.e(TAG, "Token $token is expired")
            }
        }
        throw Exception("Unable to load stream")
    }

    suspend fun loadVideoPlaylist(videoId: String, clientId: String, tokenList: String): Response<ResponseBody> = withContext(Dispatchers.IO) {
        val id = videoId.substring(1) //substring 1 to remove v, should be removed when upgraded to new api
        Log.d(TAG, "Getting video playlist for video $id. Client id: $clientId")

//        val accessToken = api.getVideoAccessToken(clientId, id, token)
        val array = JsonArray(1)
        val videoAccessTokenOperation = JsonObject().apply {
            addProperty("operationName", "PlaybackAccessToken")
            add("variables", JsonObject().apply {
                addProperty("isLive", false)
                addProperty("isVod", true)
                addProperty("login", "")
                addProperty("playerType", "channel_home_live")
                addProperty("vodID", id)
            })
            add("extensions", JsonObject().apply {
                add("persistedQuery", JsonObject().apply {
                    addProperty("version", 1)
                    addProperty("sha256Hash", "0828119ded1c13477966434e15800ff57ddacf13ba1911c129dc2200705b0712")
                })
            })
        }
        array.add(videoAccessTokenOperation)

        val shuffled = tokenList.split(",").shuffled()
        for (token in shuffled) {
            try {
                val accessToken = graphQL.getVideoAccessToken(TwitchApiHelper.addTokenPrefix(token), array)
                val options = HashMap<String, String>()
//                options["token"] = accessToken.token
//                options["sig"] = accessToken.sig
                options["token"] = accessToken.token
                options["sig"] = accessToken.signature
                options["allow_source"] = "true"
                options["allow_audio_only"] = "true"
                options["type"] = "any"
                options["p"] = Random.nextInt(999999).toString()
                return@withContext usher.getVideoPlaylist(id, options)
            } catch (e: HttpException) {
                if (e.code() != 401) throw e
                Log.e(TAG, "Token $token is expired")
            }
        }
        throw Exception("Unable to load video")
    }

    suspend fun loadSubscriberBadges(channelId: String): SubscriberBadgesResponse = withContext(Dispatchers.IO) {
        misc.getSubscriberBadges(channelId)
    }

    suspend fun loadGlobalBttvEmotes(): Response<BttvEmotesResponse> = withContext(Dispatchers.IO) {
        misc.getGlobalBttvEmotes()
    }

    suspend fun loadGlobalFfzEmotes(): Response<FfzEmotesResponse> = withContext(Dispatchers.IO) {
        misc.getGlobalFfzEmotes()
    }

    suspend fun loadBttvEmotes(channel: String): Response<BttvEmotesResponse> = withContext(Dispatchers.IO) {
        misc.getBttvEmotes(channel)
    }

    suspend fun loadFfzEmotes(channel: String): Response<FfzEmotesResponse> = withContext(Dispatchers.IO) {
        misc.getFfzEmotes(channel)
    }

    fun loadEmotes() = emotes.getAll()

    fun loadRecentEmotes() = recentEmotes.getAll()

    fun insertRecentEmotes(emotes: Collection<RecentEmote>) {
        GlobalScope.launch {
            val listSize = emotes.size
            val list = if (listSize <= RecentEmote.MAX_SIZE) {
                emotes
            } else {
                emotes.toList().subList(listSize - RecentEmote.MAX_SIZE, listSize)
            }
            recentEmotes.ensureMaxSizeAndInsert(list)
        }
    }

    fun loadVideoPositions(): LiveData<Map<Long, Long>> = Transformations.map(videoPositions.getAll()) { list ->
        list.associate { it.id to it.position }
    }

    fun saveVideoPosition(position: VideoPosition) {
        GlobalScope.launch {
            videoPositions.insert(position)
        }
    }
}
