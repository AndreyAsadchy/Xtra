package com.exact.xtra.ui.player.video

import android.app.Application
import android.content.Context
import com.exact.xtra.GlideApp
import com.exact.xtra.db.VideosDao
import com.exact.xtra.model.OfflineVideo
import com.exact.xtra.model.VideoInfo
import com.exact.xtra.model.video.Video
import com.exact.xtra.repository.PlayerRepository
import com.exact.xtra.ui.player.HlsPlayerViewModel
import com.exact.xtra.util.DownloadUtils
import com.exact.xtra.util.TwitchApiHelper
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.iheartradio.m3u8.Encoding
import com.iheartradio.m3u8.Format
import com.iheartradio.m3u8.PlaylistWriter
import com.iheartradio.m3u8.data.MediaPlaylist
import com.iheartradio.m3u8.data.Playlist
import com.iheartradio.m3u8.data.TrackData
import com.iheartradio.m3u8.data.TrackInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import javax.inject.Inject

class VideoPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository,
        private val dao: VideosDao) : HlsPlayerViewModel(context) {

    lateinit var video: Video
    private val cacheDataSourceFactory = CacheDataSourceFactory(DownloadUtils.getCache(context), dataSourceFactory)
    private var playbackProgress: Long = 0
    private val mediaPlaylist by lazy { (player.currentManifest as HlsManifest).mediaPlaylist }
    val videoInfo: VideoInfo
        get() = VideoInfo(helper.qualities.value!!, mediaPlaylist.segments, toSeconds(mediaPlaylist.durationUs), toSeconds(mediaPlaylist.targetDurationUs))

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
        val directory = context.getDir(video.id + quality, Context.MODE_PRIVATE)
        var totalDuration = 0L
        val tracks = sortedSetOf<TrackData>(Comparator { o1, o2 ->
            fun parse(trackData: TrackData) = trackData.uri.substring(trackData.uri.lastIndexOf('/') + 1, trackData.uri.lastIndexOf('.')).toInt()

            val index1 = parse(o1)
            val index2 = parse(o2)
            when {
                index1 > index2 -> 1
                index1 < index2 -> -1
                else -> 0
            }
        })
        val download = async {
            mediaPlaylist.segments.subList(segmentFrom, segmentTo).forEach {
                val request = Request.Builder().url(url + it.url).build()
                val response = okHttpClient.newCall(request).execute()
                response.body()!!.byteStream()!!.run {
                    val file = File(directory, it.url)
                    val out = FileOutputStream(file)
                    copyTo(out)
                    close()
                    out.close()
                    val trackDuration = toSeconds(it.durationUs).toFloat()
                    totalDuration += trackDuration.toLong()
                    tracks.add(TrackData.Builder()
                            .withUri(file.absolutePath)
                            .withTrackInfo(TrackInfo(trackDuration, it.url))
                            .build())
                }
            }
        }
        launch {
            download.await()
            println("Downloading done. Creating playlist")
            val mediaPlaylist = MediaPlaylist.Builder()
                    .withTargetDuration((toSeconds(mediaPlaylist.targetDurationUs)).toInt())
                    .withTracks(tracks.toList())
                    .build()
            val playlist = Playlist.Builder()
                    .withMediaPlaylist(mediaPlaylist)
                    .build()
            val playlistPath = directory.absolutePath + "/${System.currentTimeMillis()}.m3u8"
            val out = FileOutputStream(playlistPath)
            val writer = PlaylistWriter(out, Format.EXT_M3U, Encoding.UTF_8)
            writer.write(playlist)
            out.close()
            println("Playlist created. Saving video...")
            val currentDate = TwitchApiHelper.getCurrentTimeFormatted(context)
            val glide = GlideApp.with(context)
            val thumbnail = glide.downloadOnly().load(video.preview.medium).submit().get().absolutePath
            val logo = glide.downloadOnly().load(video.channel.logo).submit().get().absolutePath
            dao.insert(OfflineVideo(playlistPath, video.title, video.channel.name, video.game, totalDuration, currentDate, video.createdAt, thumbnail, logo))
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        when (playbackState) {
            Player.STATE_IDLE -> playbackProgress = player.currentPosition
        }
    }

    private fun toSeconds(value: Long) = value / 1000000L
}
