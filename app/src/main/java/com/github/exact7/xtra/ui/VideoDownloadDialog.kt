package com.github.exact7.xtra.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.VideoInfo
import com.github.exact7.xtra.repository.PlayerRepository
import com.iheartradio.m3u8.Encoding
import com.iheartradio.m3u8.Format
import com.iheartradio.m3u8.ParsingMode
import com.iheartradio.m3u8.PlaylistParser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_video_download.*
import java.net.URL
import javax.inject.Inject

class VideoDownloadDialog : DialogFragment(), Injectable {

    interface OnDownloadClickListener {
        fun onClick(quality: String, segmentFrom: Int, segmentTo: Int)
    }

    companion object {
        private const val KEY_VIDEO_INFO = "videoInfo"
        private const val KEY_VIDEO_ID = "videoId"

        fun newInstance(videoInfo: VideoInfo? = null, videoId: String? = null): VideoDownloadDialog {
            return VideoDownloadDialog().apply {
                arguments = bundleOf(KEY_VIDEO_INFO to videoInfo, KEY_VIDEO_ID to videoId)
            }
        }
    }

    @Inject
    lateinit var repository: PlayerRepository
    private lateinit var listener: OnDownloadClickListener
    private var compositeDisposable: CompositeDisposable? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnDownloadClickListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_video_download, container, false)
    }

    //TODO maybe move to data binding?
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val videoInfo = arguments!!.getParcelable(KEY_VIDEO_INFO) as VideoInfo?
        if (videoInfo == null) { //
            compositeDisposable = CompositeDisposable()
            repository.fetchVideoPlaylist(arguments!!.getString(KEY_VIDEO_ID)!!)
                    .map { response ->
                        val string = response.body()!!.string()
                        val qualities = "NAME=\"(.*)\"".toRegex().findAll(string).map { it.groupValues[1] }.toMutableList()
                        val urls = "https://.*\\.m3u8".toRegex().findAll(string).map(MatchResult::value).toMutableList()
                        val audioIndex = qualities.indexOf("Audio Only")
                        qualities.add(qualities.removeAt(audioIndex))
                        urls.add(urls.removeAt(audioIndex))
                        val map = qualities.zip(urls).toMap()
                        val mediaPlaylist = PlaylistParser(URL(urls[0]).openStream(), Format.EXT_M3U, Encoding.UTF_8, ParsingMode.LENIENT).parse().mediaPlaylist
                        var totalDuration = 0L
                        val relativeTimes = mutableListOf<Long>()
                        var time = 0L
                        mediaPlaylist.tracks.forEach {
                            val duration = it.trackInfo.duration.toLong()
                            totalDuration += duration
                            relativeTimes.add(time)
                            time += duration
                        }
                        VideoInfo(qualities, relativeTimes, totalDuration, mediaPlaylist.targetDuration.toLong(), 0)
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

    private fun init(videoInfo: VideoInfo) {
        val context = requireContext()
        spinner.adapter = ArrayAdapter(context, R.layout.spinner_quality_item, videoInfo.qualities)
        val duration = DateUtils.formatElapsedTime(videoInfo.totalDuration)
        this.duration.text = context.getString(R.string.length, duration)
        timeFrom.hint = DateUtils.formatElapsedTime(videoInfo.currentPosition).let {
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
                            if (to_ > videoInfo.totalDuration) {
                                timeTo.error = context.getString(R.string.to_is_longer)
                                return@setOnClickListener
                            }
                            val times = videoInfo.relativeStartTimes
                            val fromIndex = if (from == 0L) {
                                0
                            } else {
                                val min = from - videoInfo.targetDuration
                                times.binarySearch(comparison = { time ->
                                    val offset = time / 1000000L
                                    when {
                                        offset > from -> 1
                                        offset < min -> -1
                                        else -> 0
                                    }
                                })
                            }
                            val toIndex = if (to_ in times.last() / 1000000L..videoInfo.totalDuration) {
                                times.size - 1
                            } else {
                                val max = to_ + videoInfo.targetDuration
                                times.binarySearch(comparison = { time ->
                                    val offset = time / 1000000L
                                    when {
                                        offset > max -> 1
                                        offset < to_ -> -1
                                        else -> 0
                                    }
                                }) //todo if length is good don't add
                            }
                            listener.onClick(spinner.selectedItem.toString(), fromIndex, toIndex)
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
