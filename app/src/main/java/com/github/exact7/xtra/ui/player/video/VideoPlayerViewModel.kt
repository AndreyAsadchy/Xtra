package com.github.exact7.xtra.ui.player.video

import android.app.Application
import android.os.Bundle
import android.os.Parcel
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.github.exact7.xtra.model.VideoInfo
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.service.DownloadWorker
import com.github.exact7.xtra.ui.player.HlsPlayerViewModel
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject


class VideoPlayerViewModel @Inject constructor(
        context: Application,
        private val playerRepository: PlayerRepository) : HlsPlayerViewModel(context) {

    private val _video = MutableLiveData<Video>()
    val video: LiveData<Video>
        get() = _video
    private var playbackProgress: Long = 0
    private val mediaPlaylist by lazy { (player.currentManifest as HlsManifest).mediaPlaylist } //it gets current playlist and not the one to download
    val videoInfo: VideoInfo
        get() = VideoInfo(helper.qualities.value!!, mediaPlaylist.segments.map { it.relativeStartTimeUs }, toSeconds(mediaPlaylist.durationUs), toSeconds(mediaPlaylist.targetDurationUs), player.currentPosition / 1000)

    fun setVideo(video: Video) {
        if (_video.value != video) {
            _video.value = video
            playerRepository.fetchVideoPlaylist(video.id)
                    .subscribe({
                        mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(it)
                        play()
                    }, {

                    })
                    .addTo(compositeDisposable)
        }
    }

    override fun onResume() {
        super.onResume()
        player.seekTo(playbackProgress)
    }

    override fun onPause() {
        super.onPause()
        playbackProgress = player.currentPosition
    }

    fun getBundleSizeInBytes(bundle: Bundle): Int {
        val parcel = Parcel.obtain()
        parcel.writeValue(bundle)
        val bytes = parcel.marshall()
        parcel.recycle()
        return bytes.size
    }

    fun download(quality: String, segmentFrom: Int, segmentTo: Int) {
//        DownloadWorker.download(getApplication(), VideoRequest(
//                        video.value!!,
//                        quality,
//                        helper.urls[quality]!!.substringBeforeLast('/') + "/",
//                        ArrayList(mediaPlaylist.segments.subList(segmentFrom, segmentTo).map { it.url to toSeconds(it.durationUs) }),
//                        toSeconds(mediaPlaylist.targetDurationUs).toInt()))

        println(getBundleSizeInBytes(bundleOf()))
//        val request = VideoRequest(video.value!!,
//                        quality,
//                        helper.urls[quality]!!.substringBeforeLast('/') + "/",
//                        ArrayList(mediaPlaylist.segments.subList(segmentFrom, segmentTo).map { it.url to toSeconds(it.durationUs) }),
//                        toSeconds(mediaPlaylist.targetDurationUs).toInt())

        val data1 = Data.Builder()
                .putInt("key", 1)
                .build()
        val downloadWork1 = OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                .setInputData(data1)
                .build()
        val data2 = Data.Builder()
                .putInt("key", 2)
                .build()
        val downloadWork2 = OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                .setInputData(data2)
                .build()
        val workManager = WorkManager.getInstance()
        workManager.enqueue(downloadWork1)
        workManager.enqueue(downloadWork2)

//        val context = getApplication<Application>()
//        val notificationManager = DefaultFetchNotificationManager(context)
//        val fetchConfiguration = FetchConfiguration.Builder(context)
//                .setNotificationManager(notificationManager)
//                .setDownloadConcurrentLimit(3)
//                .enableLogging(true)
//                .enableRetryOnNetworkGain(true)
//                .build()
//        val fetch = Fetch.getInstance(fetchConfiguration)
//        var downloaded = 0
//        fetch.addListener(object : FetchListener {
//            override fun onAdded(download: Download) {
//
//            }
//
//            override fun onCancelled(download: Download) {
//
//            }
//
//            override fun onCompleted(download: Download) {
//                println(++downloaded)
//            }
//
//            override fun onDeleted(download: Download) {
//
//            }
//
//            override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {
//
//            }
//
//            override fun onError(download: Download, error: Error, throwable: Throwable?) {
//
//            }
//
//            override fun onPaused(download: Download) {
//
//            }
//
//            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
//            }
//
//            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
//            }
//
//            override fun onRemoved(download: Download) {
//            }
//
//            override fun onResumed(download: Download) {
//            }
//
//            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
//            }
//
//            override fun onWaitingNetwork(download: Download) {
//            }
//
//        })
//        val list = mutableListOf<Request>()
//        for (i in 0..5) {
//        val request = Request(helper.urls[quality]!!.substringBeforeLast('/') + "/$i.ts", context.getExternalFilesDir(".downloads" + File.separator + video.value!!.id + quality)!!.absolutePath + File.separator + "$i.ts")
//            request.groupId = 123
//            list.add(request)
//        }
//        fetch.enqueue(list, Func {
//
//        })

    }

    private fun toSeconds(value: Long) = value / 1000000L
}
