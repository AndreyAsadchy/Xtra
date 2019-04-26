package com.github.exact7.xtra.ui.player

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.common.follow.FollowFragment
import com.github.exact7.xtra.ui.common.follow.FollowViewModel
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.ui.player.offline.OfflinePlayerFragment
import com.github.exact7.xtra.ui.player.stream.StreamPlayerFragment
import com.github.exact7.xtra.ui.view.SlidingLayout
import com.github.exact7.xtra.ui.view.chat.ChatView
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.LifecycleListener
import com.github.exact7.xtra.util.Prefs
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.visible
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView

private const val CHAT_OPENED = "ChatOpened"
private const val WAS_IN_PIP = "wasInPip"

@Suppress("PLUGIN_WARNING")
abstract class BasePlayerFragment : BaseNetworkFragment(), Injectable, LifecycleListener, SlidingLayout.Listener, FollowFragment {

    private lateinit var slidingLayout: SlidingLayout
    private lateinit var playerView: PlayerView
    private lateinit var chatView: ChatView
    private var secondView: View? = null
    private lateinit var showChat: ImageButton
    private lateinit var hideChat: ImageButton
    protected var isPortrait: Boolean = false
        private set
    protected var isInPictureInPictureMode = false
        private set
    protected var wasInPictureInPictureMode = false
    private var shouldRecreate = false

    private lateinit var prefs: SharedPreferences
    private lateinit var userPrefs: SharedPreferences
    abstract val channel: Channel

    private var playerViewWidth = 0
    private var playerViewHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isInPictureInPictureMode = savedInstanceState?.getBoolean(C.PICTURE_IN_PICTURE) == true
        wasInPictureInPictureMode = savedInstanceState?.getBoolean(WAS_IN_PIP) == true
        prefs = Prefs.get(requireContext())
        userPrefs = requireActivity().getSharedPreferences(C.USER_PREFS, Context.MODE_PRIVATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.keepScreenOn = true
        isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val activity = requireActivity() as MainActivity
        slidingLayout = view as SlidingLayout
        slidingLayout.addListener(activity)
        slidingLayout.addListener(this)
        view.findViewById<ImageButton>(R.id.minimize).setOnClickListener { minimize() }
        if (isPortrait) {
            view.findViewById<ImageButton>(R.id.fullscreenEnter).setOnClickListener { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        } else {
            hideStatusBar()
            view.findViewById<ImageButton>(R.id.fullscreenExit).setOnClickListener {
                activity.apply {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        }
        playerView = view.findViewById(R.id.playerView)
        var resizeMode = if (isPortrait) {
            playerView.updateLayoutParams { height = prefs.getInt(C.PORTRAIT_PLAYER_HEIGHT, 0)  }
            prefs.getInt(C.ASPECT_RATIO_PORTRAIT, AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT)
        } else {
            prefs.getInt(C.ASPECT_RATIO_LANDSCAPE, AspectRatioFrameLayout.RESIZE_MODE_FIT)
        }
        playerView.resizeMode = resizeMode
        view.findViewById<ImageButton>(R.id.aspectRatio).setOnClickListener {
            resizeMode = (resizeMode + 1).let { if (it < 5) it else 0 }
            playerView.resizeMode = resizeMode
            prefs.edit { putInt(if (isPortrait) C.ASPECT_RATIO_PORTRAIT else C.ASPECT_RATIO_LANDSCAPE, resizeMode) }
        }
        playerView.post {
            playerViewWidth = playerView.width
            playerViewHeight = playerView.height
        }
        if (this !is OfflinePlayerFragment) {
            chatView = view.findViewById(R.id.chatView)
            secondView = chatView
            if (!isPortrait) {
                chatView.updateLayoutParams { width = prefs.getInt(C.LANDSCAPE_CHAT_WIDTH, 0) }
                hideChat = view.findViewById<ImageButton>(R.id.hideChat).apply {
                    setOnClickListener { hideChat() }
                }
                showChat = view.findViewById<ImageButton>(R.id.showChat).apply {
                    setOnClickListener { showChat() }
                }
                setPreferredChatVisibility()
            }
            view.findViewById<ImageButton>(R.id.settings).apply {
                isEnabled = false
                setColorFilter(Color.GRAY)
            }
            view.findViewById<ImageButton>(R.id.profile).setOnClickListener {
                activity.viewChannel(channel)
                slidingLayout.minimize()
            }
            view.findViewById<TextView>(R.id.channel).text = channel.displayName
        } else {
            if (isPortrait) {
                secondView = view.findViewById(R.id.dummyView)
            }
        }
        if (this !is StreamPlayerFragment) {
            val prefs = Prefs.get(activity)
            val rewind = prefs.getString("playerRewind", "5000")!!.toInt()
            val forward = prefs.getString("playerForward", "5000")!!.toInt()
            val rewindImage = when (rewind) {
                5000 -> R.drawable.baseline_replay_5_black_48
                10000 -> R.drawable.baseline_replay_10_black_48
                else -> R.drawable.baseline_replay_30_black_48
            }
            val forwardImage = when (forward) {
                5000 -> R.drawable.baseline_forward_5_black_48
                10000 -> R.drawable.baseline_forward_10_black_48
                else -> R.drawable.baseline_forward_30_black_48
            }
            playerView.apply {
                setRewindIncrementMs(rewind)
                setFastForwardIncrementMs(forward)
            }
            view.findViewById<ImageButton>(com.google.android.exoplayer2.ui.R.id.exo_rew).setImageResource(rewindImage)
            view.findViewById<ImageButton>(com.google.android.exoplayer2.ui.R.id.exo_ffwd).setImageResource(forwardImage)
            if (this !is OfflinePlayerFragment) {
                view.findViewById<ImageButton>(R.id.download).apply {
                    isEnabled = false
                    setColorFilter(Color.GRAY)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        wasInPictureInPictureMode = false
        if (shouldRecreate) {
            shouldRecreate = false
            requireActivity().supportFragmentManager.beginTransaction().detach(this).attach(this).commit()
        }
    }

    override fun onStop() {
        super.onStop()
        shouldRecreate = isInPictureInPictureMode
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(C.PICTURE_IN_PICTURE, isInPictureInPictureMode)
        outState.putBoolean(WAS_IN_PIP, wasInPictureInPictureMode)
        super.onSaveInstanceState(outState)
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        this.isInPictureInPictureMode = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            playerView.apply {
                useController = false
                updateLayoutParams {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
            secondView?.gone()
        } else if (!shouldRecreate) {
            wasInPictureInPictureMode = true
            if (isPortrait) {
                secondView?.visible()
            } else if (this !is OfflinePlayerFragment) {
                setPreferredChatVisibility()
            }
            playerView.apply {
                useController = true
                updateLayoutParams {
                    width = playerViewWidth
                    height = playerViewHeight
                }
            }
        }
    }

    protected fun initializeViewModel(viewModel: PlayerViewModel, enableChat: Boolean = true) {
        val activity = requireActivity() as MainActivity
        playerView.player = viewModel.player
        if (this !is OfflinePlayerFragment) {
            val mainViewModel = ViewModelProviders.of(activity, viewModelFactory).get(MainViewModel::class.java)
            mainViewModel.user.observe(viewLifecycleOwner, Observer {
                if (it is LoggedIn) {
                    if (enableChat) {
                        chatView.setUsername(it.name)
                    }
                    if (viewModel is FollowViewModel) {
                        initializeFollow(this, viewModel, requireView().findViewById(R.id.follow), it)
                    }
                }
            })
        }
        if (enableChat) {
            viewModel.chatMessages.observe(viewLifecycleOwner, Observer(chatView::submitList))
            viewModel.newMessage.observe(viewLifecycleOwner, Observer { chatView.notifyMessageAdded() })
            val emotesObserver = Observer(chatView::addEmotes)
            viewModel.bttv.observe(viewLifecycleOwner, emotesObserver)
            viewModel.ffz.observe(viewLifecycleOwner, emotesObserver)
        }
    }

//    abstract fun play(obj: Parcelable) //TODO instead maybe add livedata in mainactivity and observe it

    fun minimize() {
        slidingLayout.minimize()
    }

    private fun showStatusBar() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun hideStatusBar() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onMinimize() {
        playerView.hideController()
//        if (!isPortrait) { //TODO fix drag view when show status bar
//            showStatusBar()
//        }
    }

    override fun onMaximize() {
//        if (!isPortrait) {
//            hideStatusBar()
//        }
    }

    override fun onClose() {
        if (!isPortrait) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            showStatusBar()
        }
    }

    private fun setPreferredChatVisibility() {
        if (userPrefs.getBoolean(CHAT_OPENED, true)) showChat() else hideChat()
    }

    private fun hideChat() {
        hideChat.gone()
        showChat.visible()
        chatView.gone()
        userPrefs.edit { putBoolean(CHAT_OPENED, false) }
    }

    private fun showChat() {
        hideChat.visible()
        showChat.gone()
        chatView.visible()
        userPrefs.edit { putBoolean(CHAT_OPENED, true) }
    }
}
