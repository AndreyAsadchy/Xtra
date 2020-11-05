package com.github.exact7.xtra.repository

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.github.exact7.xtra.api.ApiService
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
import com.github.exact7.xtra.util.TwitchApiHelper.TWITCH_CLIENT_ID
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

private const val TAG = "PlayerRepository"
private const val UNDEFINED = "undefined"

@Singleton
class PlayerRepository @Inject constructor(
        private val api: ApiService,
        private val usher: UsherApi,
        private val misc: MiscApi,
        private val emotes: EmotesDao,
        private val recentEmotes: RecentEmotesDao,
        private val videoPositions: VideoPositionsDao) {

    suspend fun loadStreamPlaylist(channelName: String): Uri = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting stream playlist for channel $channelName")

        //removes "commercial break in progress"
        val uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 16)
        val apiToken = UUID.randomUUID().toString().replace("-", "").substring(0, 32)
        val serverSessionId = UUID.randomUUID().toString().replace("-", "").substring(0, 32)
        val cookie = "unique_id=$uniqueId; unique_id_durable=$uniqueId; twitch.lohp.countryCode=BY; api_token=twilight.$apiToken; server_session_id=$serverSessionId"

        val accessToken = api.getStreamAccessToken(TWITCH_CLIENT_ID, cookie, channelName, UNDEFINED, "animated_thumbnails" /* blocks ads */)
        val options = HashMap<String, String>()
        options["token"] = accessToken.token
        options["sig"] = accessToken.sig
        options["allow_source"] = "true"
        options["allow_audio_only"] = "true"
        options["type"] = "any"
        options["p"] = Random().nextInt(999999).toString()
        options["fast_bread"] = "true" //low latency

        //not working anyway
//        options["server_ads"] = "false"
//        options["show_ads"] = "false"
        val playlist = usher.getStreamPlaylist(channelName, options)
        playlist.raw().request().url().toString().toUri()
    }

    suspend fun loadVideoPlaylist(videoId: String): Response<ResponseBody> = withContext(Dispatchers.IO) {
        val id = videoId.substring(1) //substring 1 to remove v, should be removed when upgraded to new api
        Log.d(TAG, "Getting video playlist for video $id")
        val accessToken = api.getVideoAccessToken(TWITCH_CLIENT_ID, id, UNDEFINED)
        val options = HashMap<String, String>()
        options["token"] = accessToken.token
        options["sig"] = accessToken.sig
        options["allow_source"] = "true"
        options["allow_audio_only"] = "true"
        options["type"] = "any"
        options["p"] = Random().nextInt(999999).toString()
        usher.getVideoPlaylist(id, options)
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
