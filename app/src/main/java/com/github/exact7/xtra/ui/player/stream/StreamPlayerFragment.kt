package com.github.exact7.xtra.ui.player.stream

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.common.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.FragmentUtils
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import kotlinx.android.synthetic.main.fragment_player_stream.*
import kotlinx.android.synthetic.main.player_stream.*

@Suppress("PLUGIN_WARNING")
class StreamPlayerFragment : BasePlayerFragment(), RadioButtonDialogFragment.OnSortOptionChanged {

    private companion object {
        const val TAG = "StreamPlayer"
        const val CHAT_OPENED = "ChatOpened"
    }

    private lateinit var prefs: SharedPreferences

    override lateinit var viewModel: StreamPlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = requireActivity().getSharedPreferences(C.USER_PREFS, Context.MODE_PRIVATE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_stream, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isPortraitOrientation) {
            if (prefs.getBoolean(CHAT_OPENED, true)) showChat() else hideChat()
            maximizePlayer.setOnClickListener {
                hideChat()
                prefs.edit { putBoolean(CHAT_OPENED, false) }
            }
            minimizePlayer.setOnClickListener {
                showChat()
                prefs.edit { putBoolean(CHAT_OPENED, true) }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StreamPlayerViewModel::class.java)
        playerView.player = viewModel.player
        settings.isEnabled = false
        settings.setColorFilter(Color.GRAY)
        viewModel.loaded.observe(this, Observer {
            settings.isEnabled = true
            settings.setColorFilter(Color.WHITE) //TODO
        })
        viewModel.chatMessages.observe(viewLifecycleOwner, Observer(chatView::submitList))
        viewModel.newMessage.observe(viewLifecycleOwner, Observer { chatView.notifyAdapter() })
        viewModel.chat.observe(viewLifecycleOwner, Observer(messageView::setCallback))
        settings.setOnClickListener {
            FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, viewModel.qualities, viewModel.selectedQualityIndex)
        }
    }

    override fun initialize() {
        val mainViewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
        mainViewModel.user.observe(viewLifecycleOwner, Observer {
            messageView.visibility = if (it != null) View.VISIBLE else View.GONE
            viewModel.startStream(arguments!!.getParcelable("stream")!!, it)
        })
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
//            if (index >= viewModel.helper.urls.value!!.lastIndex) {
//                TODO hide player
//            }
    }

    private fun hideChat() {
        maximizePlayer.visibility = View.GONE
        minimizePlayer.visibility = View.VISIBLE
        chatContainer.visibility = View.GONE
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
    }

    private fun showChat() {
        maximizePlayer.visibility = View.VISIBLE
        minimizePlayer.visibility = View.GONE
        chatContainer.visibility = View.VISIBLE
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
    }
}
