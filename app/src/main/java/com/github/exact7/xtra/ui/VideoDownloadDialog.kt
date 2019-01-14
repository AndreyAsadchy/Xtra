package com.github.exact7.xtra.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.work.WorkManager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.VideoDownloadInfo
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.model.offline.VideoRequest
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.service.DownloadService
import com.github.exact7.xtra.util.DownloadUtils
import com.iheartradio.m3u8.Encoding
import com.iheartradio.m3u8.Format
import com.iheartradio.m3u8.ParsingMode
import com.iheartradio.m3u8.PlaylistParser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_video_download.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL
import javax.inject.Inject

class VideoDownloadDialog : DialogFragment(), Injectable {

    companion object {
        private const val KEY_VIDEO_INFO = "videoInfo"
        private const val KEY_VIDEO = "video"

        fun newInstance(videoInfo: VideoDownloadInfo? = null, video: Video? = null): VideoDownloadDialog {
            return VideoDownloadDialog().apply {
                arguments = bundleOf(KEY_VIDEO_INFO to videoInfo, KEY_VIDEO to video)
            }
        }
    }

    @Inject lateinit var playerRepository: PlayerRepository
    @Inject lateinit var offlineRepository: OfflineRepository
    private var compositeDisposable: CompositeDisposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_video_download, container, false)
    }

    //TODO maybe move to data binding?
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val videoInfo = arguments!!.getParcelable(KEY_VIDEO_INFO) as VideoDownloadInfo?
        if (videoInfo == null) {
            compositeDisposable = CompositeDisposable()
            val video = arguments!!.getParcelable<Video>(KEY_VIDEO)!!
            playerRepository.fetchVideoPlaylist(video.id)
                    .map { response ->
                        val playlist = response.body()!!.string()
                        val qualities = "NAME=\"(.*)\"".toRegex().findAll(playlist).map { it.groupValues[1] }.toMutableList()
                        val urls = "https://.*\\.m3u8".toRegex().findAll(playlist).map(MatchResult::value).toMutableList()
                        var audioIndex = 0
                        qualities.forEachIndexed { i, s ->
                            if (s.equals("Audio Only", true)) {
                                audioIndex = i
                                return@forEachIndexed
                            }
                        }
                        qualities.removeAt(audioIndex)
                        qualities.add(audioIndex, requireContext().getString(R.string.audio_only))
                        urls.add(urls.removeAt(audioIndex))
                        val map = qualities.zip(urls).toMap()
                        val mediaPlaylist = URL(map.values.elementAt(0)).openStream().use {
                            PlaylistParser(it, Format.EXT_M3U, Encoding.UTF_8, ParsingMode.LENIENT).parse().mediaPlaylist
                        }
                        var totalDuration = 0L
                        val relativeTimes = mutableListOf<Long>()
                        var time = 0L
                        mediaPlaylist.tracks.forEach {
                            val duration = it.trackInfo.duration.toLong()
                            totalDuration += duration
                            relativeTimes.add(time)
                            time += duration
                        }
                        VideoDownloadInfo(video, map, relativeTimes, totalDuration, mediaPlaylist.targetDuration.toLong(), 0)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        init(it)
                    }, {
                    })
                    .addTo(compositeDisposable!!)
        } else {
            init(videoInfo)
        }

    }

    override fun onDestroy() {
        compositeDisposable?.clear()
        super.onDestroy()
    }

    private fun parseTime(textView: TextView): Long? {
        with (textView) {
            val value = if (text.isEmpty()) hint else text
            val time = value.split(":")
            try {
                if (time.size != 3) throw IllegalArgumentException()
                val hours = time[0].toLong()
                val minutes = time[1].toLong().also { if (it > 59) throw IllegalArgumentException()}
                val seconds = time[2].toLong().also { if (it > 59) throw IllegalArgumentException()}
                return (hours * 3600) + (minutes * 60) + seconds
            } catch (ex: Exception) {
                error = context.getString(R.string.invalid_time)
            }
        }
        return null
    }

    private fun init(videoInfo: VideoDownloadInfo) {
        container.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        val context = requireContext()
        with(videoInfo) {
            spinner.adapter = ArrayAdapter(context, R.layout.spinner_quality_item, qualities.keys.toTypedArray())
            val duration = DateUtils.formatElapsedTime(totalDuration)
            this@VideoDownloadDialog.duration.text = context.getString(R.string.length, duration)
            timeFrom.hint = DateUtils.formatElapsedTime(currentPosition).let {
                if (it.length == 5) {
                    "00:$it"
                } else {
                    it
                }
            }
            timeTo.hint = if (duration.length != 5) {
                duration
            } else {
                "00:$duration"
            }
            timeFrom.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    timeFrom.error = null
                }
            })
            timeTo.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    timeTo.error = null
                }
            })
            cancel.setOnClickListener { dismiss() }
            download.setOnClickListener {
                parseTime(timeFrom)?.let { from ->
                    parseTime(timeTo)?.let { to_ ->
                        when {
                            from < to_ -> {
                                if (to_ > totalDuration) {
                                    timeTo.error = context.getString(R.string.to_is_longer)
                                    return@setOnClickListener
                                }
                                val times = relativeStartTimes
                                val fromIndex = if (from == 0L) {
                                    0
                                } else {
                                    val min = from - targetDuration
                                    times.binarySearch(comparison = { time ->
                                        val offset = time / 1000000L
                                        when {
                                            offset > from -> 1
                                            offset < min -> -1
                                            else -> 0
                                        }
                                    })
                                }
                                val toIndex = if (to_ in times.last() / 1000000L..totalDuration) {
                                    times.size - 1
                                } else {
                                    val max = to_ + targetDuration
                                    times.binarySearch(comparison = { time ->
                                        val offset = time / 1000000L
                                        when {
                                            offset > max -> 1
                                            offset < to_ -> -1
                                            else -> 0
                                        }
                                    }) //todo if length is good don't add
                                }
                                val quality = spinner.selectedItem.toString()
                                val videoDuration = (relativeStartTimes[toIndex] - relativeStartTimes[fromIndex]) / 1000000L
                                val url = qualities[quality]!!.substringBeforeLast('/') + "/"
                                GlobalScope.launch {
                                    val path = context.getExternalFilesDir(".downloads${File.separator}${video.id}$quality")!!
                                    val offlineVideo = DownloadUtils.prepareDownload(context, video, path.absolutePath, videoDuration)
                                    val videoId = offlineRepository.saveVideo(offlineVideo)
                                    val request = VideoRequest(videoId.toInt(), video.id, url, path.toUri(), fromIndex, toIndex)
                                    val workId = DownloadService.download(request)
                                    WorkManager.getInstance().getWorkInfoByIdLiveData(workId).observe(requireActivity(), Observer { info ->
                                        if (info.state.isFinished) {

                                        }
                                    })
                                }
                                dismiss()
                            }
                            from >= to_ -> {
                                timeFrom.error = context.getString(R.string.from_is_greater)
                            }
                            else -> {
                                timeTo.error = context.getString(R.string.to_is_lesser)
                            }
                        }
                    }
                }
            }
        }
    }
}
