package com.exact.xtra.ui.player.video

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.exact.xtra.R
import com.exact.xtra.ui.VideoDownloadDialog
import com.exact.xtra.ui.fragment.RadioButtonDialogFragment
import com.exact.xtra.ui.player.BasePlayerFragment
import com.exact.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_player_video.*
import kotlinx.android.synthetic.main.player_video.*
import java.util.*

class VideoPlayerFragment : BasePlayerFragment(), RadioButtonDialogFragment.OnSortOptionChanged, VideoDownloadDialog.OnDownloadClickListener {
    override fun play(obj: Parcelable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private companion object {
        const val TAG = "VideoPlayer"
    }

    override lateinit var viewModel: VideoPlayerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //        channelBtn.setOnClickListener(v -> channelListener.viewChannel(video.getChannel().getName()));
        //TODO morebtn
        settings.isEnabled = false
        download.isEnabled = false
        settings.setOnClickListener {
            println(viewModel.videoInfo.targetDuration)
            LinkedList(viewModel.helper.qualities.value).also { list ->
                list.addFirst(getString(R.string.auto))
                FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, list, viewModel.helper.selectedQualityIndex)
            }
        }
        download.setOnClickListener { VideoDownloadDialog.newInstance(viewModel.videoInfo).show(childFragmentManager, null) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(VideoPlayerViewModel::class.java)
        playerView.player = viewModel.player
        viewModel.helper.qualities.observe(this, Observer {
            val loaded = it != null
            settings.isEnabled = loaded
            download.isEnabled = loaded
        })
//        viewModel.helper.chatMessages.observe(this, Observer(chatView::submitList))
//        viewModel.helper.newMessage.observe(this, Observer { chatView.notifyAdapter() })
        if (!viewModel.isInitialized()) {
            viewModel.video = arguments!!.getParcelable("video")!!
            viewModel.init()
        }
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        viewModel.changeQuality(index)
    }

    override fun onClick(quality: String, segmentFrom: Int, segmentTo: Int) {
        viewModel.download(quality, segmentFrom, segmentTo)
    }
}
