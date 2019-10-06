package com.github.exact7.xtra.ui.player.video

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.ui.chat.ChatFragment
import com.github.exact7.xtra.ui.chat.ChatReplayPlayerFragment
import com.github.exact7.xtra.ui.download.HasDownloadDialog
import com.github.exact7.xtra.ui.download.VideoDownloadDialog
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import com.github.exact7.xtra.ui.player.PlayerMode
import com.github.exact7.xtra.util.DownloadUtils
import com.github.exact7.xtra.util.FragmentUtils
import com.github.exact7.xtra.util.enable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class VideoPlayerFragment : BasePlayerFragment(), HasDownloadDialog, ChatReplayPlayerFragment {
//    override fun play(obj: Parcelable) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    override lateinit var viewModel: VideoPlayerViewModel
    private lateinit var video: Video
    override val channel: Channel
        get() = video.channel

    override val shouldEnterPictureInPicture: Boolean
        get() = viewModel.playerMode.value == PlayerMode.NORMAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        video = requireArguments().getParcelable(KEY_VIDEO)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (childFragmentManager.findFragmentById(R.id.chatFragmentContainer) == null) {
            childFragmentManager.beginTransaction().replace(R.id.chatFragmentContainer, ChatFragment.newInstance(channel, video.id, 0.0)).commit()
        }
        viewModel = getViewModel()
    }

    override fun initialize() {
        super.initialize()
        viewModel.setVideo(video, requireArguments().getDouble(KEY_OFFSET))
        val settings = requireView().findViewById<ImageButton>(R.id.settings)
        val download = requireView().findViewById<ImageButton>(R.id.download)
        viewModel.loaded.observe(viewLifecycleOwner, Observer {
            settings.enable()
            download.enable()
        })
        settings.setOnClickListener {
            FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, viewModel.qualities, viewModel.qualityIndex)
        }
        download.setOnClickListener { showDownloadDialog() }
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        viewModel.changeQuality(index)
    }

    override fun showDownloadDialog() {
        if (DownloadUtils.hasStoragePermission(requireActivity())) {
            viewModel.videoInfo?.let { VideoDownloadDialog.newInstance(it).show(childFragmentManager, null) }
        }
    }

    override fun onMovedToForeground() {
        if (!wasInPictureInPictureMode) {
            viewModel.onResume()
        }
    }

    override fun onMovedToBackground() {
        if (!wasInPictureInPictureMode) {
            viewModel.onPause()
        }
    }

    override fun onNetworkRestored() {
        viewModel.onResume()
    }

    override fun onNetworkLost() {
        viewModel.onPause()
    }

    override fun getCurrentPosition(): Double {
        return runBlocking(Dispatchers.Main) { viewModel.currentPlayer.value!!.currentPosition / 1000.0 }
    }

    companion object {
        private const val KEY_VIDEO = "video"
        private const val KEY_OFFSET = "offset"

        fun newInstance(video: Video, offset: Double? = null): VideoPlayerFragment {
            return VideoPlayerFragment().apply { arguments = bundleOf(KEY_VIDEO to video, KEY_OFFSET to offset) }
        }
    }
}
