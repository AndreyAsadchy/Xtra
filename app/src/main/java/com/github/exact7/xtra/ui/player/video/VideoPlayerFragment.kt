package com.github.exact7.xtra.ui.player.video

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.common.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.download.HasDownloadDialog
import com.github.exact7.xtra.ui.download.VideoDownloadDialog
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.DownloadUtils
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_player_video.*
import kotlinx.android.synthetic.main.player_video.*

class VideoPlayerFragment : BasePlayerFragment(), RadioButtonDialogFragment.OnSortOptionChanged, HasDownloadDialog {
//    override fun play(obj: Parcelable) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    private companion object {
        const val TAG = "VideoPlayer"
    }

    private lateinit var viewModel: VideoPlayerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //        channelBtn.setOnClickListener(v -> channelListener.viewChannel(video.getChannelName().getName()));
        //TODO morebtn
        settings.setOnClickListener {
            FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, viewModel.qualities, viewModel.selectedQualityIndex)
        }
        download.setOnClickListener { showDownloadDialog() }
    }

    override fun initialize() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(VideoPlayerViewModel::class.java)
        playerView.player = viewModel.player
        viewModel.loaded.observe(viewLifecycleOwner, Observer {
            settings.isEnabled = true
            download.isEnabled = true
            settings.setColorFilter(Color.WHITE)
            download.setColorFilter(Color.WHITE)
        })
//        viewModel.helper.chatMessages.observe(this, Observer(chatView::submitList))
//        viewModel.helper.newMessage.observe(this, Observer { chatView.notifyAdapter() })
        viewModel.setVideo(arguments!!.getParcelable(C.VIDEO)!!)
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        viewModel.changeQuality(index)
    }

    override fun showDownloadDialog() {
        if (DownloadUtils.hasInternalStoragePermission(requireActivity())) {
            VideoDownloadDialog.newInstance(viewModel.videoInfo).show(childFragmentManager, null)
        }
    }

    override fun onMovedToForeground() {
        if (this::viewModel.isInitialized) {
            viewModel.onResume()
        }
    }

    override fun onMovedToBackground() {
        if (this::viewModel.isInitialized) {
            viewModel.onPause()
        }
    }

    override fun onNetworkRestored() {
        if (this::viewModel.isInitialized) {
            viewModel.onResume()
        }
    }
}
