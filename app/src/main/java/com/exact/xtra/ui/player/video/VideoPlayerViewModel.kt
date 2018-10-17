package com.exact.xtra.ui.player.video

import android.app.Application
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMuxer
import android.os.Build
import androidx.annotation.RequiresApi
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
import java.nio.ByteBuffer
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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun download(quality: String, segmentFrom: Int, segmentTo: Int) {
        val context = getApplication<Application>()
        val url = helper.urls[quality]!!.substringBeforeLast('/') + "/"
        val okHttpClient = OkHttpClient()
        var index = 0
        val directory = context.getExternalFilesDir(video.id)!!


        mediaPlaylist.segments.subList(segmentFrom, segmentTo).map { url + it.url }.toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap {
                    Observable.fromCallable {
                        println(it)
                        val request = Request.Builder().url(it).build()
                        val response = okHttpClient.newCall(request).execute()
                        val file = File(directory, "${index++}.ts")
//                        val file = File(context.getDir(video.id, Context.MODE_PRIVATE), "${index++}.ts")
                        val byteStream = response.body()?.byteStream()!!
                        val out = FileOutputStream(file)
                        byteStream.copyTo(out)
                        byteStream.close()
                        out.close()
                    }.subscribeOn(Schedulers.io())
                }
                .subscribeBy(onComplete = {
                    val muxer = MediaMuxer("${directory.absolutePath}/test.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                    val extractor = MediaExtractor()
                    val path = "${directory.absolutePath}/0.ts"
                    extractor.setDataSource(path)
                    val numTracks = extractor.trackCount
//        for (i in 0 until numTracks) {
                    extractor.selectTrack(0)
                    val ind = muxer.addTrack(extractor.getTrackFormat(0))
//        }
                    val file = File(directory, "0.ts")
                    val inputBuffer = ByteBuffer.allocate(file.length().toInt())
                    val bufferInfo = MediaCodec.BufferInfo()
                    extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                    muxer.start()
                    while (extractor.readSampleData(inputBuffer, 0) >= 0) {
//            val trackIndex = extractor.sampleTrackIndex
                        bufferInfo.presentationTimeUs = extractor.sampleTime
                        bufferInfo.flags = extractor.sampleFlags
                        muxer.writeSampleData(ind, inputBuffer, bufferInfo)
                        extractor.advance()
                    }
                    bufferInfo.size = 0
                    muxer.stop()
                    muxer.release()
                    extractor.release()
                }, onNext = {

                }, onError = {

                })
                .addTo(compositeDisposable)

//        val track = H264TrackImpl(FileDataSourceImpl("$directory/0.ts"))
//        val movie = Movie().apply { addTrack(track) }
//        val mp4file = DefaultMp4Builder().build(movie)
//        val fc = FileOutputStream(File("out.mp4")).channel
//        mp4file.writeContainer(fc)
//        fc.close()
//                    val uploadDate = TwitchApiHelper.parseIso8601Date(context, video.createdAt)
//                    val currentDate = TwitchApiHelper.getCurrentTimeFormatted(context)
//                    TODO maybe add custom name for video
//                    OfflineVideo(url, video.title, video.channel.name, video.game, video.length.toLong(), currentDate, uploadDate, video.preview.medium, video.channel.logo).let {
//                        launch { dao::insert }
//                        PlayerUtils.startDownload(context, hlsDownloadAction, it)
//                    }

    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        when (playbackState) {
            Player.STATE_IDLE -> playbackProgress = player.currentPosition
        }
    }

}
