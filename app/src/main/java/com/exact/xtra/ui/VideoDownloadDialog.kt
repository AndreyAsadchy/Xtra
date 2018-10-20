package com.exact.xtra.ui

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
import com.exact.xtra.R
import com.exact.xtra.model.VideoInfo
import kotlinx.android.synthetic.main.dialog_video_download.*

class VideoDownloadDialog : DialogFragment() {

    interface OnDownloadClickListener {
        fun onClick(quality: String, segmentFrom: Int, segmentTo: Int)
    }

    companion object {
        private const val VIDEO_INFO = "video_info"

        fun newInstance(videoInfo: VideoInfo): VideoDownloadDialog {
            return VideoDownloadDialog().apply {
                arguments = bundleOf(VIDEO_INFO to videoInfo)
            }
        }
    }

    private lateinit var listener: OnDownloadClickListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = parentFragment as OnDownloadClickListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_video_download, container, false)
    }

    //TODO maybe move to data binding?
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = requireActivity()
        val videoInfo = arguments!!.getParcelable(VIDEO_INFO) as VideoInfo
        spinner.adapter = ArrayAdapter(context, R.layout.spinner_quality_item, videoInfo.qualities)
        val duration = DateUtils.formatElapsedTime(videoInfo.totalDuration)
        length.text = context.getString(R.string.length, duration)
        timeFrom.hint = DateUtils.formatElapsedTime(videoInfo.currentPosition).let {
            if (it.length == 5) {
                "00:$it"
            } else {
                it
            }
        }
        timeTo.hint = duration
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
                            val fromIndex = if (from == 0L) {
                                0
                            } else {
                                val min = from - videoInfo.targetDuration
                                videoInfo.relativeStartTimes.binarySearch(comparison = { time ->
                                    val offset = time / 1000000L
                                    when {
                                        offset > from -> 1
                                        offset < min -> -1
                                        else -> 0
                                    }
                                })
                            }
                            val toIndex = if (to_ == videoInfo.totalDuration) {
                                videoInfo.relativeStartTimes.size - 1
                            } else {
                                val max = to_ + videoInfo.targetDuration
                                videoInfo.relativeStartTimes.binarySearch(comparison = { time ->
                                    val offset = time / 1000000L
                                    when {
                                        offset > max -> 1
                                        offset < to_ -> -1
                                        else -> 0
                                    }
                                }) + 1
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
}
