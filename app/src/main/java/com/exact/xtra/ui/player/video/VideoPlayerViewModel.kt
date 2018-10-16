package com.exact.xtra.ui.player.video

import android.app.Application
import com.exact.xtra.db.VideosDao
import com.exact.xtra.model.video.Video
import com.exact.xtra.repository.PlayerRepository
import com.exact.xtra.ui.player.HlsPlayerViewModel
import com.exact.xtra.util.DownloadUtils
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class VideoPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository,
        private val dao: VideosDao) : HlsPlayerViewModel(context) {

    lateinit var video: Video
    private val cacheDataSourceFactory = CacheDataSourceFactory(DownloadUtils.getCache(context), dataSourceFactory)
    private var playbackProgress: Long = 0
    private val mediaPlaylist by lazy { (player.currentManifest as HlsManifest).mediaPlaylist }

    fun init() {
        playerRepository.fetchVideoPlaylist(video.id)
                .subscribe({
                    mediaSource = HlsMediaSource.Factory(cacheDataSourceFactory).createMediaSource(it)
                    startPlayer()
                }, {

                })
                .addTo(compositeDisposable)
    }

    override fun startPlayer() {
        super.startPlayer()
        player.seekTo(playbackProgress)
    }

    override fun changeQuality(index: Int, tag: String) {
        super.changeQuality(index, tag)
        playbackProgress = player.currentPosition
    }

    fun download(quality: String, segmentFrom: Int, segmentTo: Int) {
        val context = getApplication<Application>()
        val url = helper.urls[quality]!!.substringBeforeLast('/') + "/"
        val okHttpClient = OkHttpClient()
        var index = 0
        mediaPlaylist.segments.subList(segmentFrom, segmentTo).map { url + it.url }.toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap {
                    Observable.fromCallable {
                        println(it)
                        val request = Request.Builder().url(it).build()
                        val response = okHttpClient.newCall(request).execute()
                        val file = File(context.getExternalFilesDir(video.id), "${index++}.ts")
                        response.body()?.byteStream()!!.copyTo(FileOutputStream(file))
                    }.subscribeOn(Schedulers.io())
                }
                .subscribeBy(onComplete = {
                    println("done all")
//                    val uploadDate = TwitchApiHelper.parseIso8601Date(context, video.createdAt)
//                    val currentDate = TwitchApiHelper.getCurrentTimeFormatted(context)
//                    TODO maybe add custom name for video
//                    OfflineVideo(url, video.title, video.channel.name, video.game, video.length.toLong(), currentDate, uploadDate, video.preview.medium, video.channel.logo).let {
//                        launch { dao::insert }
//                        PlayerUtils.startDownload(context, hlsDownloadAction, it)
//                    }
                }, onError = {

                })
                .addTo(compositeDisposable)

    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        when (playbackState) {
            Player.STATE_IDLE -> playbackProgress = player.currentPosition
        }
    }

}
