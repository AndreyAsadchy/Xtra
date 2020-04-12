package com.github.exact7.xtra.ui.player.clip

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.ui.chat.ChatFragment
import com.github.exact7.xtra.ui.chat.ChatReplayPlayerFragment
import com.github.exact7.xtra.ui.download.ClipDownloadDialog
import com.github.exact7.xtra.ui.download.HasDownloadDialog
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import com.github.exact7.xtra.util.DownloadUtils
import com.github.exact7.xtra.util.FragmentUtils
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.enable
import com.github.exact7.xtra.util.gone
import kotlinx.android.synthetic.main.fragment_player_clip.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class ClipPlayerFragment : BasePlayerFragment(), HasDownloadDialog, ChatReplayPlayerFragment {
//    override fun play(obj: Parcelable) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    override val viewModel by viewModels<ClipPlayerViewModel> { viewModelFactory }
    private lateinit var clip: Clip
    override val channel: Channel
        get() = clip.broadcaster

    override val layoutId: Int
        get() = R.layout.fragment_player_clip
    override val chatContainerId: Int
        get() = R.id.clipChatContainer

    override val shouldEnterPictureInPicture: Boolean
        get() = true

    override val controllerShowTimeoutMs: Int = 2500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clip = requireArguments().getParcelable(KEY_CLIP)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (childFragmentManager.findFragmentById(R.id.chatFragmentContainer) == null) {
            var videoId: String? = null
            var startTime: Double? = null
            clip.vod?.let {
                videoId = "v${it.id}"
                startTime = TwitchApiHelper.parseClipOffset(it.url)
            }
            childFragmentManager.beginTransaction().replace(R.id.chatFragmentContainer, ChatFragment.newInstance(channel, videoId, startTime)).commit()
        }
        if (clip.vod == null) {
            watchVideo.gone()
        }
    }

    override fun initialize() {
        viewModel.setClip(clip)
        super.initialize()
        val view = requireView()
        val settings = view.findViewById<ImageButton>(R.id.settings)
        val download = view.findViewById<ImageButton>(R.id.download)
        viewModel.loaded.observe(this, Observer {
            settings.enable()
            download.enable()
        })
        settings.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, viewModel.qualities.keys, viewModel.qualityIndex) }
        download.setOnClickListener { showDownloadDialog() }
        clip.vod?.let { vod ->
            viewModel.video.observe(viewLifecycleOwner, Observer {
                (requireActivity() as MainActivity).startVideo(it, TwitchApiHelper.parseClipOffset(vod.url) * 1000.0 + viewModel.player.currentPosition)
            })
            watchVideo.setOnClickListener { viewModel.loadVideo() }
        }
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        viewModel.changeQuality(index)
    }

    override fun showDownloadDialog() {
        if (DownloadUtils.hasStoragePermission(requireActivity())) {
            ClipDownloadDialog.newInstance(clip, viewModel.qualities).show(childFragmentManager, null)
        }
    }

    override fun onMovedToForeground() {
        viewModel.onResume()
    }

    override fun onMovedToBackground() {
        viewModel.onPause()
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
        private const val KEY_CLIP = "clip"

        fun newInstance(clip: Clip): ClipPlayerFragment {
            return ClipPlayerFragment().apply { arguments = bundleOf(KEY_CLIP to clip) }
        }
    }
}
