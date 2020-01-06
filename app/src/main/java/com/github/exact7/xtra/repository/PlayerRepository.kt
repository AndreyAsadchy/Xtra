package com.github.exact7.xtra.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.github.exact7.xtra.XtraApp
import com.github.exact7.xtra.api.ApiService
import com.github.exact7.xtra.api.GraphQLApi
import com.github.exact7.xtra.api.MiscApi
import com.github.exact7.xtra.api.UsherApi
import com.github.exact7.xtra.db.EmotesDao
import com.github.exact7.xtra.db.RecentEmotesDao
import com.github.exact7.xtra.db.VideoPositionsDao
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.VideoPosition
import com.github.exact7.xtra.model.chat.BttvEmotesResponse
import com.github.exact7.xtra.model.chat.FfzRoomResponse
import com.github.exact7.xtra.model.chat.RecentEmote
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.util.TwitchApiHelper.TWITCH_CLIENT_ID
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
        private val graphQL: GraphQLApi,
        private val emotes: EmotesDao,
        private val recentEmotes: RecentEmotesDao,
        private val videoPositions: VideoPositionsDao) {

    fun loadStreamPlaylist(channelName: String): Single<Uri> {
        Log.d(TAG, "Getting stream playlist for channel $channelName")
//        options["show_ads"] = "false"
//        options["server_ads"] = "false"
        return api.getStreamAccessToken(TWITCH_CLIENT_ID, channelName, User.get(XtraApp.INSTANCE).let { if (it is LoggedIn && it.newToken) it.token else UNDEFINED })
                .flatMap {
                    val options = HashMap<String, String>()
                    options["token"] = it.token
                    options["sig"] = it.sig
                    options["allow_source"] = "true"
                    options["allow_audio_only"] = "true"
                    options["type"] = "any"
                    options["p"] = Random().nextInt(999999).toString()
                    options["fast_bread"] = "true" //low latency
                    usher.getStreamPlaylist(channelName, options)
                }
                .map { Uri.parse(it.raw().request().url().toString()) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    fun loadVideoPlaylist(videoId: String): Single<Response<ResponseBody>> {
        val id = videoId.substring(1) //substring 1 to remove v, should be removed when upgraded to new api
        Log.d(TAG, "Getting video playlist for video $id")
        return api.getVideoAccessToken(TWITCH_CLIENT_ID, id, User.get(XtraApp.INSTANCE).let { if (it is LoggedIn && it.newToken) it.token else UNDEFINED })
                .flatMap {
                    val options = HashMap<String, String>()
                    options["token"] = it.token
                    options["sig"] = it.sig
                    options["allow_source"] = "true"
                    options["allow_audio_only"] = "true"
                    options["type"] = "any"
                    options["p"] = Random().nextInt(999999).toString()
                    usher.getVideoPlaylist(id, options)
                }
    }

    fun loadClipUrls(slug: String): Single<Map<String, String>> {
        val array = JsonArray(1)
        val videoAccessTokenOperation = JsonObject().apply {
            addProperty("operationName", "VideoAccessToken_Clip")
            add("variables", JsonObject().apply {
                addProperty("slug", slug)
            })
            add("extensions", JsonObject().apply {
                add("persistedQuery", JsonObject().apply {
                    addProperty("version", 1)
                    addProperty("sha256Hash", "9bfcc0177bffc730bd5a5a89005869d2773480cf1738c592143b5173634b7d15")
                })
            })
        }
        array.add(videoAccessTokenOperation)
        return graphQL.getClipData(array)
                .map { response ->
                    println("RESPONSE $response")
                    response.qualities.associateBy({ if (it.frameRate == 60) "${it.quality}p${it.frameRate}" else it.quality + "p" }, { it.url }).also { println("RETURN $it") }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    fun loadSubscriberBadges(channelId: String): Single<SubscriberBadgesResponse> {
        return misc.getSubscriberBadges(channelId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    fun loadBttvEmotes(channel: String): Single<BttvEmotesResponse> { //TODO test without Response<>
        return misc.getBttvEmotes(channel)
    }

    fun loadFfzEmotes(channel: String): Single<FfzRoomResponse> {
        return misc.getFfzEmotes(channel)
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

    fun loadVideoPositions(): LiveData<Map<Long, Long>> = Transformations.map(videoPositions.getAll()) { list -> list.associate { it.id to it.position } }

    fun saveVideoPosition(position: VideoPosition) {
        GlobalScope.launch {
            videoPositions.insert(position)
        }
    }
}
