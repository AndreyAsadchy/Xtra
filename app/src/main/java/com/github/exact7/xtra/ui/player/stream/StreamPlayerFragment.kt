package com.github.exact7.xtra.ui.player.stream

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.ui.chat.ChatFragment
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import com.github.exact7.xtra.ui.player.PlayerMode
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.FragmentUtils
import com.github.exact7.xtra.util.enable
import kotlinx.android.synthetic.main.player_stream.*

class StreamPlayerFragment : BasePlayerFragment() {

    private lateinit var chatFragment: ChatFragment
    private lateinit var viewModel: StreamPlayerViewModel
    private lateinit var stream: Stream
    override val channel: Channel
        get() = stream.channel

    override val shouldEnterPictureInPicture: Boolean
        get() = viewModel.playerMode.value == PlayerMode.NORMAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stream = requireArguments().getParcelable(C.STREAM)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_stream, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatFragment = childFragmentManager.findFragmentById(R.id.chatFragmentContainer).let {
            if (it == null) {
                val fragment = ChatFragment.newInstance(channel)
                childFragmentManager.beginTransaction().replace(R.id.chatFragmentContainer, fragment).commit()
                fragment
            } else {
                it as ChatFragment
            }
        }
        viewModel = getViewModel()
    }

    override fun initialize() {
        getMainViewModel().user.observe(viewLifecycleOwner, Observer {
            viewModel.startStream(stream)
            initializeViewModel(viewModel)
        })
        val settings = requireView().findViewById<ImageButton>(R.id.settings)
        viewModel.loaded.observe(viewLifecycleOwner, Observer {
            settings.enable()
        })
        settings.setOnClickListener {
            FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, viewModel.qualities, viewModel.qualityIndex)
        }
        resume.setOnClickListener {
            viewModel.restartPlayer()
        }
    }

    fun hideEmotesMenu() = chatFragment.hideEmotesMenu()

    override fun onMinimize() {
        super.onMinimize()
        chatFragment.hideKeyboard()
    }

//    override fun play(obj: Parcelable) {
//        val stream = obj as Stream
//        if (viewModel.stream != stream) {
//            viewModel.player.playWhenReady = false
//            chatView.adapter.submitList(null)
//        }
//        viewModel.stream = stream
//        draggableView?.maximize()
//    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        viewModel.changeQuality(index)
//            if (index >= viewModel.helper.urls.value!!.lastIndex) {
//                TODO hide player
//            }
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
}
