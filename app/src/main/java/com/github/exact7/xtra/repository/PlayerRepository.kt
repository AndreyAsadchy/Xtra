package com.github.exact7.xtra.repository

import android.net.Uri
import android.util.Log
import com.github.exact7.xtra.XtraApp
import com.github.exact7.xtra.api.ApiService
import com.github.exact7.xtra.api.MiscApi
import com.github.exact7.xtra.api.UsherApi
import com.github.exact7.xtra.db.EmotesDao
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.util.Prefs
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PlayerRepository"

@Singleton
class PlayerRepository @Inject constructor(
        private val api: ApiService,
        private val usher: UsherApi,
        private val misc: MiscApi,
        private val emotes: EmotesDao) {

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
        val tokenHeader = XtraApp.INSTANCE?.let { context -> Prefs.getUser(context).let { if (it is LoggedIn) "OAuth ${it.token}" else null } }
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

    fun loadBttvEmotes(channel: String): Single<List<BttvEmote>> {
        return misc.getBttvEmotes(channel)
                .map { it.emotes }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    fun loadFfzEmotes(channel: String): Single<List<FfzEmote>> {
        return misc.getFfzEmotes(channel)
                .map { it.emotes }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    fun loadEmotes() = emotes.getAll().also { println("GET ${it.value}") }
}
