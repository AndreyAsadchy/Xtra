package com.github.exact7.xtra.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import com.github.exact7.xtra.ui.view.chat.ChatView
import com.github.exact7.xtra.ui.view.chat.MessageClickedDialog
import com.github.exact7.xtra.util.LifecycleListener
import com.github.exact7.xtra.util.hideKeyboard

class ChatFragment : BaseNetworkFragment(), LifecycleListener, MessageClickedDialog.OnButtonClickListener {

    companion object {
        private const val KEY_IS_LIVE = "isLive"
        private const val KEY_CHANNEL_ID = "channelId"
        private const val KEY_CHANNEL_NAME = "channelName"
        private const val KEY_VIDEO_ID = "videoId"
        private const val KEY_START_TIME = "startTime"

        fun newInstance(channelId: String?, channelName: String) = ChatFragment().apply {
            arguments = bundleOf(KEY_IS_LIVE to true, KEY_CHANNEL_ID to channelId, KEY_CHANNEL_NAME to channelName)
        }

        fun newInstance(channelId: String?, channelName: String, videoId: String, startTime: Double) = ChatFragment().apply {
            arguments = bundleOf(KEY_IS_LIVE to false, KEY_CHANNEL_ID to channelId, KEY_CHANNEL_NAME to channelName, KEY_VIDEO_ID to videoId, KEY_START_TIME to startTime)
        }
    }

    override lateinit var viewModel: ChatViewModel
    private lateinit var chatView: ChatView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false).also { chatView = it as ChatView }
    }

    override fun initialize() {
        viewModel = createViewModel(ChatViewModel::class.java)
        val args = requireArguments()
        val channelId = args.getString(KEY_CHANNEL_ID)
        val channelName = args.getString(KEY_CHANNEL_NAME)!!
        val enableChat = if (args.getBoolean(KEY_IS_LIVE)) {
            val user = User.get(requireContext())
            viewModel.startLive(user, channelId, channelName)
            viewModel.chat.observe(viewLifecycleOwner, Observer(chatView::setCallback))
            if (user is LoggedIn) {
                chatView.messagingEnabled
                chatView.setUsername(user.name)
                viewModel.emotes.observe(viewLifecycleOwner, Observer(chatView::addEmotes))
            }
            true
        } else {
            val getCurrentPosition = (parentFragment as ChatReplayPlayerFragment)::getCurrentPosition
            args.getString(KEY_VIDEO_ID).let {
                if (it != null) {
                    viewModel.startReplay(channelId, channelName, it, args.getDouble(KEY_START_TIME), getCurrentPosition)
                    true
                } else {
                    false
                }
            }
        }
        if (enableChat) {
            viewModel.chatMessages.observe(viewLifecycleOwner, Observer(chatView::submitList))
            viewModel.newMessage.observe(viewLifecycleOwner, Observer { chatView.notifyMessageAdded() })
            val emotesObserver = Observer(chatView::addEmotes)
            viewModel.bttv.observe(viewLifecycleOwner, emotesObserver)
            viewModel.ffz.observe(viewLifecycleOwner, emotesObserver)
        }
    }

    fun hideKeyboard() {
        chatView.hideKeyboard()
    }

    fun hideEmotesMenu() = chatView.hideEmotesMenu()

    override fun onReplyClicked(userName: String) {
        chatView.reply(userName)
    }

    override fun onCopyMessageClicked(message: String) {
        chatView.setMessage(message)
    }

    override fun onViewProfileClicked(channel: Channel) {
        (requireActivity() as MainActivity).viewChannel(channel)
        (parentFragment as? BasePlayerFragment)?.minimize()
    }

    override fun onNetworkRestored() {
        viewModel.start()
    }

    override fun onMovedToBackground() {
        viewModel.stop()
    }

    override fun onMovedToForeground() {
        viewModel.start()
    }
}