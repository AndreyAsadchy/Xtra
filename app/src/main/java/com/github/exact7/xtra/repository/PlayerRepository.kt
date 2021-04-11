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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.HashMap
import kotlin.collections.set
import kotlin.random.Random

private const val TAG = "PlayerRepository"
private const val UNDEFINED = "undefined"

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

        val accessTokenJson = getAccessTokenJson(isLive = true, isVod = false, login = channelName, playerType = playerType, vodId = "")
        val accessTokenHeaders = getAccessTokenHeaders()

        suspend fun loadStream(token: String): Uri {
            accessTokenHeaders["Authorization"] = token
            val accessToken = graphQL.getStreamAccessToken(accessTokenHeaders, accessTokenJson)
            val playlistQueryOptions = HashMap<String, String>()
//            playlistQueryOptions["token"] = accessToken.token
//            playlistQueryOptions["sig"] = accessToken.sig
            playlistQueryOptions["token"] = accessToken.token
            playlistQueryOptions["sig"] = accessToken.signature
            playlistQueryOptions["allow_source"] = "true"
            playlistQueryOptions["allow_audio_only"] = "true"
            playlistQueryOptions["type"] = "any"
            playlistQueryOptions["p"] = Random.nextInt(999999).toString()
            playlistQueryOptions["fast_bread"] = "true" //low latency

            //not working anyway
//            playlistQueryOptions["server_ads"] = "false"
//            playlistQueryOptions["show_ads"] = "false"
            val playlist = usher.getStreamPlaylist(channelName, playlistQueryOptions)
            return playlist.raw().request().url().toString().toUri()
        }

//        val shuffled = tokenList.split(",").shuffled()
//        for (token in shuffled) {
//            try {
//                return@withContext loadStream(TwitchApiHelper.addTokenPrefix(token))
//            } catch (e: HttpException) {
//                if (e.code() != 401) throw e
//                Log.e(TAG, "Token $token is expired")
//            }
//        }
        loadStream(UNDEFINED)
    }

    suspend fun loadVideoPlaylist(videoId: String, clientId: String, tokenList: String): Response<ResponseBody> = withContext(Dispatchers.IO) {
        val id = videoId.substring(1) //substring 1 to remove v, should be removed when upgraded to new api
        Log.d(TAG, "Getting video playlist for video $id. Client id: $clientId")

//        val accessToken = api.getVideoAccessToken(clientId, id, token)
        val accessTokenJson = getAccessTokenJson(isLive = false, isVod = true, login = "", playerType = "channel_home_live", vodId = id)
        val accessTokenHeaders = getAccessTokenHeaders()

        suspend fun loadVideo(token: String): Response<ResponseBody> {
            accessTokenHeaders["Authorization"] = token
            val accessToken = graphQL.getVideoAccessToken(accessTokenHeaders, accessTokenJson)
            val playlistQueryOptions = HashMap<String, String>()
//            options["token"] = accessToken.token
//            options["sig"] = accessToken.sig
            playlistQueryOptions["token"] = accessToken.token
            playlistQueryOptions["sig"] = accessToken.signature
            playlistQueryOptions["allow_source"] = "true"
            playlistQueryOptions["allow_audio_only"] = "true"
            playlistQueryOptions["type"] = "any"
            playlistQueryOptions["p"] = Random.nextInt(999999).toString()
            return usher.getVideoPlaylist(id, playlistQueryOptions)
        }

//        val shuffled = tokenList.split(",").shuffled()
//        for (token in shuffled) {
//            try {
//                return@withContext loadVideo(TwitchApiHelper.addTokenPrefix(token))
//            } catch (e: HttpException) {
//                if (e.code() != 401) throw e
//                Log.e(TAG, "Token $token is expired")
//            }
//        }
        loadVideo(UNDEFINED)
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

    private fun getAccessTokenJson(isLive: Boolean, isVod: Boolean, login: String, playerType: String, vodId: String): JsonArray {
        val jsonArray = JsonArray(1)
        val operation = JsonObject().apply {
            addProperty("operationName", "PlaybackAccessToken")
            add("variables", JsonObject().apply {
                addProperty("isLive", isLive)
                addProperty("isVod", isVod)
                addProperty("login", login)
                addProperty("playerType", playerType)
                addProperty("vodID", vodId)
            })
            add("extensions", JsonObject().apply {
                add("persistedQuery", JsonObject().apply {
                    addProperty("version", 1)
                    addProperty("sha256Hash", "0828119ded1c13477966434e15800ff57ddacf13ba1911c129dc2200705b0712")
                })
            })
        }
        jsonArray.add(operation)
        return jsonArray
    }

    private fun getAccessTokenHeaders(): MutableMap<String, String> {
        return HashMap<String, String>().apply {
            put("X-Device-Id", UUID.randomUUID().toString().replace("-", "").substring(0, 32)) //X-Device-Id or Device-ID removes "commercial break in progress" (length 16 or 32)
            put("Accept", "*/*")
            put("Accept-Encoding", "gzip, deflate, br")
            put("Accept-Language", "ru-RU")
            put("Connection", "keep-alive")
            put("Content-Type", "text/plain;charset=UTF-8")
            put("Host", "gql.twitch.tv")
            put("Origin", "https://www.twitch.tv")
            put("Referer", "https://www.twitch.tv/")
            put("sec-ch-ua", "\"Google Chrome\";v=\"87\", \" Not;A Brand\";v=\"99\", \"Chromium\";v=\"87\"")
            put("sec-ch-ua-mobile", "?0")
            put("Sec-Fetch-Dest", "empty")
            put("Sec-Fetch-Mode", "cors")
            put("Sec-Fetch-Site", "same-site")
            put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36")
        }
    }
}