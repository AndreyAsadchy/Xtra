package com.exact.twitch.ui.player.stream

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup

import com.exact.twitch.R
import com.exact.twitch.tasks.LiveChatTask
import com.exact.twitch.ui.fragment.RadioButtonDialogFragment
import com.exact.twitch.ui.player.BasePlayerFragment
import com.exact.twitch.util.C
import com.exact.twitch.util.FragmentUtils
import com.exact.twitch.util.chat.OnChatConnectedListener
import kotlinx.android.synthetic.main.fragment_player_stream.*
import kotlinx.android.synthetic.main.player_stream.*
import java.util.LinkedList

class StreamPlayerFragment : BasePlayerFragment(), RadioButtonDialogFragment.OnOptionSelectedListener {

    private companion object {
        const val TAG = "StreamPlayer"
    }

    private lateinit var viewModel: StreamPlayerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_stream, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StreamPlayerViewModel::class.java)
        playerView.player = viewModel.player
        if (!viewModel.isInitialized()) {
            settings.isEnabled = false
            val prefs = requireActivity().getSharedPreferences(C.AUTH_PREFS, MODE_PRIVATE)
            val userName = prefs.getString(C.USERNAME, null)
            val userToken = prefs.getString(C.TOKEN, null)
            viewModel.userToken = userToken
            viewModel.play(arguments!!.getParcelable("stream")!!, userName, userToken, object: OnChatConnectedListener {
                override fun onConnect(chatTask: LiveChatTask) {
                    if (viewModel.isUserAuthorized()) {
                        messageView.setCallback(chatTask)
                    }
                }
            })
        }
        if (viewModel.isUserAuthorized()) {
            messageView.visibility = VISIBLE
        }
        val qualities = viewModel.helper.qualities
        qualities.observe(this, Observer { settings.isEnabled = it != null })
        viewModel.helper.chatMessages.observe(this, Observer(chatView::submitList))
        viewModel.helper.newMessage.observe(this, Observer { chatView.notifyAdapter() })
        settings.setOnClickListener {
            val list = LinkedList(qualities.value).apply {
                addFirst(getString(R.string.auto))
                addLast(getString(R.string.chat_only))
            }
            FragmentUtils.showRadioButtonDialogFragment(requireActivity(), childFragmentManager, list, TAG)
        }
    }

    override fun onStop() {
        super.onStop()
        if (!requireActivity().isChangingConfigurations) {
            playerView.player = null
        }
    }

    override fun onSelect(index: Int, text: CharSequence, tag: Int?) {
        if (viewModel.helper.selectedQualityIndex != index) {
            viewModel.changeQuality(index, TAG)
            if (index >= viewModel.helper.qualities.value!!.lastIndex) {
                //TODO hide player
            }
        }
    }
}
