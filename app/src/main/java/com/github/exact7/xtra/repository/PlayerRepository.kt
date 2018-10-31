package com.github.exact7.xtra.repository

import android.net.Uri
import android.util.Log
import com.github.exact7.xtra.api.ApiService
import com.github.exact7.xtra.api.MiscApi
import com.github.exact7.xtra.api.UsherApi
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.model.clip.ClipStatusResponse
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(
        private val api: ApiService,
        private val usher: UsherApi,
        private val misc: MiscApi) {

    companion object {
        private const val TAG = "PlayerRepository"
    }

    fun fetchStreamPlaylist(channelName: String): Single<Uri> {
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

    fun fetchVideoPlaylist(videoId: String): Single<Uri> {
        Log.d(TAG, "Getting video playlist for video $videoId")
        val options = HashMap<String, String>()
        options["allow_source"] = "true"
        options["allow_audio_only"] = "true"
        options["type"] = "any"
        options["p"] = Random().nextInt(999999).toString()
        return api.getVideoAccessToken(videoId)
                .flatMap {
                    options["nauth"] = it.token
                    options["nauthsig"] = it.sig
                    usher.getVideoPlaylist(videoId.substring(1), options) //substring 1 to remove v, should be removed when upgraded to new api
                }
                .map { Uri.parse(it.raw().request().url().toString()) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    fun fetchClipQualities(slug: String): Single<List<ClipStatusResponse.QualityOption>> {
        return misc.getClipStatus(slug)
                .map { it.qualityOptions }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    fun fetchSubscriberBadges(channelId: String): Single<SubscriberBadgesResponse> {
        return misc.getSubscriberBadges(channelId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }
}
