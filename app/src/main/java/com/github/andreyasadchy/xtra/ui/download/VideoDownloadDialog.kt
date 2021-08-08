package com.github.andreyasadchy.xtra.ui.download

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.VideoDownloadInfo
import com.github.andreyasadchy.xtra.model.kraken.video.Video
import com.github.andreyasadchy.xtra.util.C
import kotlinx.android.synthetic.main.dialog_video_download.*
import javax.inject.Inject


class VideoDownloadDialog : BaseDownloadDialog() {

    companion object {
        private const val KEY_VIDEO_INFO = "videoInfo"
        private const val KEY_VIDEO = "video"

        fun newInstance(videoInfo: VideoDownloadInfo): VideoDownloadDialog {
            return VideoDownloadDialog().apply { arguments = bundleOf(KEY_VIDEO_INFO to videoInfo) }
        }

        fun newInstance(video: Video): VideoDownloadDialog {
            return VideoDownloadDialog().apply { arguments = bundleOf(KEY_VIDEO to video) }
        }
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<VideoDownloadViewModel> { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?  =
            inflater.inflate(R.layout.dialog_video_download, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.videoInfo.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                (requireView() as ConstraintLayout).children.forEach { v -> v.isVisible = v.id != R.id.progressBar && v.id != R.id.storageSelectionContainer }
                init(it)
            } else {
                dismiss()
            }
        })
        requireArguments().getParcelable<VideoDownloadInfo?>(KEY_VIDEO_INFO).let {
            if (it == null) {
                viewModel.setVideo(requireArguments().getParcelable(KEY_VIDEO)!!)
            } else {
                viewModel.setVideoInfo(it)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun init(videoInfo: VideoDownloadInfo) {
        val context = requireContext()
        init(context)
        with(videoInfo) {
            spinner.adapter = ArrayAdapter(context, R.layout.spinner_quality_item, qualities.keys.toTypedArray())
            with(DateUtils.formatElapsedTime(totalDuration / 1000L)) {
                duration.text = context.getString(R.string.duration, this)
                timeTo.hint = this.let { if (it.length != 5) it else "00:$it" }
            }
            timeFrom.hint = DateUtils.formatElapsedTime(currentPosition / 1000L).let { if (it.length == 5) "00:$it" else it }
            timeFrom.doOnTextChanged { text, _, _, _ -> if (text?.length == 8) timeTo.requestFocus() }
            addTextChangeListener(timeFrom)
            addTextChangeListener(timeTo)
            cancel.setOnClickListener { dismiss() }

            fun download() {
                val from = parseTime(timeFrom) ?: return
                val to = parseTime(timeTo) ?: return
                when {
                    to > totalDuration -> {
                        timeTo.requestFocus()
                        timeTo.error = getString(R.string.to_is_longer)
                    }
                    from < to -> {
                        val fromIndex = if (from == 0L) {
                            0
                        } else {
                            val min = from - targetDuration
                            relativeStartTimes.binarySearch(comparison = { time ->
                                when {
                                    time > from -> 1
                                    time < min -> -1
                                    else -> 0
                                }
                            })
                        }
                        val toIndex = if (to in relativeStartTimes.last()..totalDuration) {
                            relativeStartTimes.lastIndex
                        } else {
                            val max = to + targetDuration
                            relativeStartTimes.binarySearch(comparison = { time ->
                                when {
                                    time > max -> 1
                                    time < to -> -1
                                    else -> 0
                                }
                            })
                        }

                        val preference = prefs.getString(C.DOWNLOAD_NETWORK_PREFERENCE, "3")
                        var wifiOnly = preference == "2"

                        fun startDownload() {
                            val quality = spinner.selectedItem.toString()
                            val url = videoInfo.qualities.getValue(quality).substringBeforeLast('/') + "/"
                            viewModel.download(url, downloadPath, quality, fromIndex, toIndex, wifiOnly)
                            dismiss()
                        }

                        if (preference != "3") {
                            startDownload()
                        } else {
                            wifiOnly = true
                            AlertDialog.Builder(context)
                                    .setMultiChoiceItems(arrayOf(getString(R.string.wifi_only)), BooleanArray(1) { true }) { _, _, isChecked -> wifiOnly = isChecked }
                                    .setPositiveButton(getString(R.string.always)) { _, _ ->
                                        prefs.edit { putString(C.DOWNLOAD_NETWORK_PREFERENCE, if (wifiOnly) "2" else "1") }
                                        startDownload()
                                    }
                                    .setNegativeButton(getString(R.string.just_once)) { _, _ -> startDownload() }
                                    .setNeutralButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                                    .setCustomTitle(LayoutInflater.from(context).inflate(R.layout.view_download_warning, null))
                                    .show()
                        }
                    }
                    from >= to -> {
                        timeFrom.requestFocus()
                        timeFrom.error = getString(R.string.from_is_greater)
                    }
                    else -> {
                        timeTo.requestFocus()
                        timeTo.error = getString(R.string.to_is_lesser)
                    }
                }
            }
            timeTo.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    download()
                    true
                } else {
                    false
                }
            }
            download.setOnClickListener { download() }
        }
    }

    private fun parseTime(textView: TextView): Long? {
        with(textView) {
            val value = if (text.isEmpty()) hint else text
            val time = value.split(':')
            try {
                if (time.size != 3) throw IllegalArgumentException()
                val hours = time[0].toLong()
                val minutes = time[1].toLong().also { if (it > 59) throw IllegalArgumentException()}
                val seconds = time[2].toLong().also { if (it > 59) throw IllegalArgumentException()}
                return ((hours * 3600) + (minutes * 60) + seconds) * 1000
            } catch (ex: Exception) {
                requestFocus()
                error = getString(R.string.invalid_time)
            }
        }
        return null
    }

    private fun addTextChangeListener(textView: TextView) {
        textView.addTextChangedListener(object : TextWatcher {
            private var lengthBeforeEdit = 0

            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                textView.error = null
                val length = s.length
                if (length == 2 || length == 5) {
                    if (lengthBeforeEdit < length) {
                        textView.append(":")
                    } else {
                        textView.editableText.delete(length - 1, length)
                    }
                }
                lengthBeforeEdit = length
            }
        })
    }
}
