package com.github.exact7.xtra.ui.player.video

import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.VideoDownloadDialog
import com.github.exact7.xtra.ui.fragment.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_player_video.*
import kotlinx.android.synthetic.main.player_video.*
import java.util.LinkedList

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
        settings.setColorFilter(Color.GRAY) //TODO
        download.setColorFilter(Color.GRAY)
        settings.setOnClickListener {
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
        viewModel.helper.qualities.observe(viewLifecycleOwner, Observer {
            val loaded = it != null
            settings.isEnabled = loaded
            download.isEnabled = loaded
            settings.setColorFilter(Color.WHITE)
            download.setColorFilter(Color.WHITE)
        })
        viewModel.setVideo(arguments!!.getParcelable("video")!!)
//        viewModel.helper.chatMessages.observe(this, Observer(chatView::submitList))
//        viewModel.helper.newMessage.observe(this, Observer { chatView.notifyAdapter() })
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        viewModel.changeQuality(index)
    }

    override fun onClick(quality: String, segmentFrom: Int, segmentTo: Int) {
        viewModel.download(quality, segmentFrom, segmentTo)
    }
}
