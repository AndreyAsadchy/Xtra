package com.github.exact7.xtra.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.view.chat.ChatView
import com.github.exact7.xtra.util.LifecycleListener

class ChatFragment : BaseNetworkFragment(), LifecycleListener {

    override lateinit var viewModel: ChatViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        container?.addView(ChatView(requireContext()))
        return container
    }

    override fun initialize() {
        viewModel = createViewModel(ChatViewModel::class.java)
    }

    override fun onNetworkRestored() {
        viewModel.start()
    }

    override fun onMovedToBackground() {
        viewModel.start()
    }

    override fun onMovedToForeground() {
        viewModel.stop()
    }
}