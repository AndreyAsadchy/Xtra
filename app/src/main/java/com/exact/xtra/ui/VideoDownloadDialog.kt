package com.exact.xtra.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.exact.xtra.R
import com.exact.xtra.model.VideoInfo
import kotlinx.android.synthetic.main.dialog_video_download.*

class VideoDownloadDialog(
        fragment: Fragment,
        private val videoInfo: VideoInfo) : Dialog(fragment.requireActivity()) {

    private val listener = fragment as OnDownloadClickListener

    interface OnDownloadClickListener {
        fun onClick(quality: String, segmentFrom: Int, segmentTo: Int)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_video_download)
        spinner.adapter = ArrayAdapter(context, R.layout.spinner_quality_item, videoInfo.qualities)
        val duration = DateUtils.formatElapsedTime(videoInfo.totalDuration)
        length.text = context.getString(R.string.length, duration) //TODO maybe move to data binding?
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
            validate(timeFrom)?.let { from ->
                validate(timeTo)?.let { to_ ->
                    when {
                        from < to_ -> {
                            val fromIndex = if (from == 0L) {
                                0
                            } else {
                                videoInfo.segments.binarySearch(fromIndex = 1, comparison = { segment ->
                                    (segment.relativeStartTimeUs / 1000000L).coerceIn(from - videoInfo.targetDuration, from).compareTo(from) //TODO change to comparator and if coerce close return true
                                })
                            }
                            val toIndex = if (to_ == videoInfo.totalDuration) {
                                videoInfo.segments.size - 1
                            } else {
                                videoInfo.segments.binarySearch(comparison = { segment ->
                                    (segment.relativeStartTimeUs / 1000000L).coerceIn(to_, to_ + videoInfo.targetDuration).compareTo(to_)
                                })
                            }
                            println("From index $fromIndex To index $toIndex")
                            println("From seg ${videoInfo.segments[fromIndex].relativeStartTimeUs} To seg ${videoInfo.segments[toIndex].relativeStartTimeUs}")
//                            listener.onClick(spinner.selectedItem.toString(), keys)
//                            dismiss()
                        }
                        from >= to_ -> {
                            timeFrom.error = context.getString(R.string.from_is_bigger)
                        }
                        else -> {
                            timeTo.error = context.getString(R.string.to_is_smaller)
                        }
                    }
                }
            }
        }
    }

    private fun validate(textView: TextView): Long? {
        with (textView) {
            val value = if (text.isEmpty()) hint else text
            val time = value.split(":")
            if (time.size != 3) {
                error = context.getString(R.string.invalid_time)
                return null
            }
            try {
                val hours = time[0].toLong()
                val minutes = time[1].toLong().also { if (it > 60) throw IllegalArgumentException()}
                val seconds = time[2].toLong().also { if (it > 60) throw IllegalArgumentException()}
                return (hours * 3600) + (minutes * 60) + seconds
            } catch (ex: IllegalArgumentException) {
                error = context.getString(R.string.invalid_time)
            }
        }
        return null
    }
}
