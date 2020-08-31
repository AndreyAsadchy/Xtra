package com.github.exact7.xtra.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import com.github.exact7.xtra.ui.view.chat.ChatView
import com.github.exact7.xtra.ui.view.chat.MessageClickedDialog
import com.github.exact7.xtra.util.LifecycleListener
import com.github.exact7.xtra.util.hideKeyboard
import com.github.exact7.xtra.util.visible
import kotlinx.android.synthetic.main.view_chat.view.*

class ChatFragment : BaseNetworkFragment(), LifecycleListener, MessageClickedDialog.OnButtonClickListener {

    private val viewModel by viewModels<ChatViewModel> { viewModelFactory }
    private lateinit var chatView: ChatView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false).also { chatView = it as ChatView }
    }

    override fun initialize() {
        val args = requireArguments()
        val channel = args.getParcelable<Channel>(KEY_CHANNEL)!!
        val user = User.get(requireContext())
        val userIsLoggedIn = user is LoggedIn
        val isLive = args.getBoolean(KEY_IS_LIVE)
        val enableChat = if (isLive) {
            viewModel.startLive(user, channel)
            chatView.init(this)
            chatView.setCallback(viewModel)
            if (userIsLoggedIn) {
                chatView.setUsername(user.name)
                chatView.setChatters(viewModel.chatters)
                val emotesObserver = Observer(chatView::addEmotes)
                viewModel.twitchEmotes.observe(viewLifecycleOwner, emotesObserver)
                viewModel.recentEmotes.observe(viewLifecycleOwner, emotesObserver)
                viewModel.newChatter.observe(viewLifecycleOwner, Observer(chatView::addChatter))
            }
            true
        } else {
            args.getString(KEY_VIDEO_ID).let {
                if (it != null) {
                    chatView.init(this)
                    val getCurrentPosition = (parentFragment as ChatReplayPlayerFragment)::getCurrentPosition
                    viewModel.startReplay(channel, it, args.getDouble(KEY_START_TIME), getCurrentPosition)
                    true
                } else {
                    chatView.chatReplayUnavailable.visible()
                    false
                }
            }
        }
        if (enableChat) {
            chatView.enableChatInteraction(isLive && userIsLoggedIn)
            viewModel.chatMessages.observe(viewLifecycleOwner, Observer(chatView::submitList))
            viewModel.newMessage.observe(viewLifecycleOwner, Observer { chatView.notifyMessageAdded() })
            viewModel.otherEmotes.observe(viewLifecycleOwner, Observer(chatView::addEmotes))
        }
    }

    fun hideKeyboard() {
        chatView.hideKeyboard()
    }

    fun hideEmotesMenu() = chatView.hideEmotesMenu()

    fun appendEmote(emote: Emote) {
        chatView.appendEmote(emote)
    }

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

    companion object {
        private const val KEY_IS_LIVE = "isLive"
        private const val KEY_CHANNEL = "channel"
        private const val KEY_VIDEO_ID = "videoId"
        private const val KEY_START_TIME = "startTime"

        fun newInstance(channel: Channel) = ChatFragment().apply {
            arguments = bundleOf(KEY_IS_LIVE to true, KEY_CHANNEL to channel)
        }

        fun newInstance(channel: Channel, videoId: String?, startTime: Double?) = ChatFragment().apply {
            arguments = bundleOf(KEY_IS_LIVE to false, KEY_CHANNEL to channel, KEY_VIDEO_ID to videoId, KEY_START_TIME to startTime)
        }
    }
}