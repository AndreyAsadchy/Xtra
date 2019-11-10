package com.github.exact7.xtra.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.github.exact7.xtra.XtraApp
import com.github.exact7.xtra.api.ApiService
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

@Singleton
class PlayerRepository @Inject constructor(
        private val api: ApiService,
        private val usher: UsherApi,
        private val misc: MiscApi,
        private val emotes: EmotesDao,
        private val recentEmotes: RecentEmotesDao,
        private val videoPositions: VideoPositionsDao) {

    fun loadStreamPlaylist(channelName: String): Single<Uri> {
        Log.d(TAG, "Getting stream playlist for channel $channelName")
        val options = HashMap<String, String>()
        options["allow_source"] = "true"
        options["allow_audio_only"] = "true"
        options["type"] = "any"
        options["p"] = Random().nextInt(999999).toString()
        return api.getStreamAccessToken(channelName)
                .flatMap {
                    options["nauth"] = it.token
                    options["nauthsig"] = it.sig
//                    options["fast_bread"] = "true" //low latency
                    usher.getStreamPlaylist(channelName, options)
                }
                .map { Uri.parse(it.raw().request().url().toString()) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    fun loadVideoPlaylist(videoId: String): Single<Response<ResponseBody>> {
        Log.d(TAG, "Getting video playlist for video $videoId")
        val options = HashMap<String, String>()
        options["allow_source"] = "true"
        options["allow_audio_only"] = "true"
        options["type"] = "any"
        options["p"] = Random().nextInt(999999).toString()
        val tokenHeader = User.get(XtraApp.INSTANCE).let { if (it is LoggedIn) "OAuth ${it.token}" else null }
//        val tokenHeader = "gaijrtcbb1anjc1agcbpvuwnbezlhk"
//        val tokenHeader = User.get(XtraApp.INSTANCE).token
        return api.getVideoAccessToken(tokenHeader, videoId)
                .flatMap {
                    options["nauth"] = it.token
                    options["nauthsig"] = it.sig
                    usher.getVideoPlaylist(tokenHeader, videoId.substring(1), options) //substring 1 to remove v, should be removed when upgraded to new api
                }
    }

    fun loadClipQualities(slug: String): Single<Map<String, String>> {
        return misc.getClipStatus(slug)
                .map { response ->
                    response.qualityOptions.associateBy({ if (it.frameRate == 60) "${it.quality}p${it.frameRate}" else it.quality + "p" }, { it.source })
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    fun loadSubscriberBadges(channelId: String): Single<SubscriberBadgesResponse> {
        return misc.getSubscriberBadges(channelId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    fun loadBttvEmotes(channel: String): Single<Response<BttvEmotesResponse>> {
        return misc.getBttvEmotes(channel)
    }

    fun loadFfzEmotes(channel: String): Single<Response<FfzRoomResponse>> {
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
