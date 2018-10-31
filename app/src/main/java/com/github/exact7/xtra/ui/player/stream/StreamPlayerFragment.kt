package com.github.exact7.xtra.ui.player.stream

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.fragment.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_player_stream.*
import kotlinx.android.synthetic.main.player_stream.*
import java.util.*

class StreamPlayerFragment : BasePlayerFragment(), RadioButtonDialogFragment.OnSortOptionChanged {

    private companion object {
        const val TAG = "StreamPlayer"
    }

    override lateinit var viewModel: StreamPlayerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_stream, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StreamPlayerViewModel::class.java)
        playerView.player = viewModel.player
        val mainViewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
        mainViewModel.user.observe(this, Observer {
            messageView.visibility = if (it != null) View.VISIBLE else View.GONE
            viewModel.user = it
        })
        if (!viewModel.isInitialized()) {
            settings.isEnabled = false
            viewModel.stream = arguments!!.getParcelable("stream")!!
        }
        viewModel.helper.qualities.observe(this, Observer {
            settings.isEnabled = true
        })
        viewModel.helper.chatMessages.observe(this, Observer(chatView::submitList))
        viewModel.helper.newMessage.observe(this, Observer { chatView.notifyAdapter() })
        viewModel.chatTask.observe(this, Observer(messageView::setCallback))
        settings.setOnClickListener {
            val list = LinkedList(viewModel.helper.qualities.value)
            list.apply {
                addFirst(getString(R.string.auto))
                addLast(getString(R.string.chat_only))
            }
            FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, list, viewModel.helper.selectedQualityIndex)
        }
    }

    override fun onMovedToForeground() {
        super.onMovedToForeground()
        viewModel.startChat()
    }

    override fun onMovedToBackground() {
        super.onMovedToBackground()
        viewModel.stopChat()
    }

    override fun play(obj: Parcelable) {
//        val stream = obj as Stream
//        if (viewModel.stream != stream) {
//            viewModel.player.playWhenReady = false
//            chatView.adapter.submitList(null)
//        }
//        viewModel.stream = stream
//        draggableView?.maximize()
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        viewModel.changeQuality(index)
//            if (index >= viewModel.helper.qualities.value!!.lastIndex) {
//                TODO hide player
//            }
    }
}
