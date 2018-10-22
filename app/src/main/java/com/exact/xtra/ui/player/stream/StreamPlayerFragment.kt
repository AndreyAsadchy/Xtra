package com.exact.xtra.ui.player.stream

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProviders
import com.exact.xtra.R
import com.exact.xtra.model.stream.Stream
import com.exact.xtra.ui.fragment.RadioButtonDialogFragment
import com.exact.xtra.ui.main.MainViewModel
import com.exact.xtra.ui.player.BasePlayerFragment
import com.exact.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_player_stream.*
import kotlinx.android.synthetic.main.player_stream.*
import kotlinx.android.synthetic.main.view_chat_message.view.*
import java.util.*

class StreamPlayerFragment : BasePlayerFragment(), RadioButtonDialogFragment.OnSortOptionChanged, LifecycleObserver {

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
        val mainViewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
        mainViewModel.user.observe(this, Observer {
            messageView.visibility = if (it != null) View.VISIBLE else View.GONE
            viewModel.user = it
        })
        if (!viewModel.isInitialized()) {
            settings.isEnabled = false
            viewModel.stream = arguments!!.getParcelable("stream")!!
        }
        val qualities = viewModel.helper.qualities
        qualities.observe(this, Observer { settings.isEnabled = it != null })
        viewModel.helper.chatMessages.observe(this, Observer(chatView::submitList))
        viewModel.helper.newMessage.observe(this, Observer { chatView.notifyAdapter() })
        viewModel.chatTask.observe(this, Observer(messageView::setCallback))
        settings.setOnClickListener {
            val list = LinkedList(qualities.value).apply {
                addFirst(getString(R.string.auto))
                addLast(getString(R.string.chat_only))
            }
            FragmentUtils.showRadioButtonDialogFragment(requireActivity(), childFragmentManager, list, TAG)
        }
    }

    override fun onMoveToForeground() {
        super.onMoveToForeground()
        if (messageView?.editText?.length() == 0) {
            messageView.editText.clearFocus()
        }
        playerView?.player = viewModel.player
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun onMoveToBackground() {
        super.onMoveToBackground()
        viewModel.stopChat()
        playerView?.player = null
    }

    override fun play(obj: Parcelable) {
        val stream = obj as Stream
        if (viewModel.stream != stream) {
            viewModel.player.playWhenReady = false
            chatView.adapter.submitList(null)
        }
        viewModel.stream = stream
        draggableView?.maximize()
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        if (viewModel.helper.selectedQualityIndex != index) {
            viewModel.changeQuality(index, TAG)
            if (index >= viewModel.helper.qualities.value!!.lastIndex) {
                //TODO hide init
            }
        }
    }
}
