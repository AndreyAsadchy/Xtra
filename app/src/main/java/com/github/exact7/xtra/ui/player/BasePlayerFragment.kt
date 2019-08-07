package com.github.exact7.xtra.ui.player

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.common.follow.FollowFragment
import com.github.exact7.xtra.ui.common.follow.FollowViewModel
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.player.offline.OfflinePlayerFragment
import com.github.exact7.xtra.ui.player.stream.StreamPlayerFragment
import com.github.exact7.xtra.ui.view.SlidingLayout
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.LifecycleListener
import com.github.exact7.xtra.util.disable
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.isInPortraitOrientation
import com.github.exact7.xtra.util.isKeyboardShown
import com.github.exact7.xtra.util.prefs
import com.github.exact7.xtra.util.visible
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView


private const val CHAT_OPENED = "ChatOpened"
private const val WAS_IN_PIP = "wasInPip"

@Suppress("PLUGIN_WARNING")
abstract class BasePlayerFragment : BaseNetworkFragment(), Injectable, LifecycleListener, SlidingLayout.Listener, FollowFragment {

    private lateinit var slidingLayout: SlidingLayout
    private lateinit var playerView: PlayerView
    private lateinit var chatLayout: FrameLayout
    private var secondView: ViewGroup? = null
    private lateinit var showChat: ImageButton
    private lateinit var hideChat: ImageButton

    protected var isPortrait: Boolean = false
        private set
    protected var isInPictureInPictureMode = false
        private set
    protected var wasInPictureInPictureMode = false //TODO refactor to PlayerViewModel?
    private var shouldRecreate = false
    private var isKeyboardShown = false
    abstract val shouldEnterPictureInPicture: Boolean

    private lateinit var prefs: SharedPreferences
    private lateinit var userPrefs: SharedPreferences
    abstract val channel: Channel

    private var playerWidth = 0
    private var playerHeight = 0
    private var chatWidth = 0

    private var systemUiFlags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isInPictureInPictureMode = savedInstanceState?.getBoolean(C.PICTURE_IN_PICTURE) == true
        wasInPictureInPictureMode = savedInstanceState?.getBoolean(WAS_IN_PIP) == true
        prefs = requireContext().prefs()
        userPrefs = requireActivity().getSharedPreferences(C.USER_PREFS, Context.MODE_PRIVATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            systemUiFlags = systemUiFlags or (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.keepScreenOn = true
        val activity = requireActivity() as MainActivity
        isPortrait = activity.isInPortraitOrientation
        slidingLayout = view as SlidingLayout
        slidingLayout.addListener(activity)
        slidingLayout.addListener(this)
        view.findViewById<ImageButton>(R.id.minimize).setOnClickListener { minimize() }
        if (isPortrait) {
            view.findViewById<ImageButton>(R.id.fullscreenEnter).setOnClickListener { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            showStatusBar()
        } else {
            activity.window.decorView.setOnSystemUiVisibilityChangeListener {
                if (!isKeyboardShown && slidingLayout.isMaximized) {
                    hideStatusBar()
                }
            }
            slidingLayout.post {
                if (slidingLayout.isMaximized) {
                    hideStatusBar()
                } else {
                    showStatusBar()
                }
            }
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
            playerWidth = playerView.width
            playerHeight = playerView.height
        }
        if (this !is OfflinePlayerFragment) {
            chatLayout = view.findViewById(R.id.chatFragmentContainer)
            secondView = chatLayout
            if (!isPortrait) {
                chatWidth = prefs.getInt(C.LANDSCAPE_CHAT_WIDTH, 0)
                chatLayout.updateLayoutParams { width = chatWidth }
                hideChat = view.findViewById<ImageButton>(R.id.hideChat).apply {
                    setOnClickListener { hideChat() }
                }
                showChat = view.findViewById<ImageButton>(R.id.showChat).apply {
                    setOnClickListener { showChat() }
                }
                setPreferredChatVisibility()
            }
            view.findViewById<ImageButton>(R.id.settings).disable()
            view.findViewById<TextView>(R.id.channel).apply {
                text = channel.displayName
                setOnClickListener {
                    activity.viewChannel(channel)
                    slidingLayout.minimize()
                }
            }
        } else {
            if (isPortrait) {
                secondView = view.findViewById(R.id.dummyView)
            }
        }
        if (this !is StreamPlayerFragment) {
            val prefs = activity.prefs()
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
                view.findViewById<ImageButton>(R.id.download).disable()
            }
        }
        slidingLayout.viewTreeObserver.addOnGlobalLayoutListener {
            if (slidingLayout.isKeyboardShown) {
                if (!isKeyboardShown) {
                    isKeyboardShown = true
                    if (!isPortrait) {
                        if (this is StreamPlayerFragment) {
                            try {
                                chatLayout.updateLayoutParams { width = (slidingLayout.width / 1.8f).toInt() }
                            } catch (e: UninitializedPropertyAccessException) { //TODO Just in case, remove if not needed
                                Crashlytics.logException(e)
                            }
                        }
                        showStatusBar()
                    }
                }
            } else {
                if (isKeyboardShown) {
                    isKeyboardShown = false
                    secondView?.clearFocus()
                    if (!isPortrait) {
                        if (this is StreamPlayerFragment) {
                            try {
                                chatLayout.updateLayoutParams { width = chatWidth }
                            } catch (e: UninitializedPropertyAccessException) {
                                Crashlytics.logException(e)
                            }
                        }
                        if (slidingLayout.isMaximized) {
                            hideStatusBar()
                        }
                    }
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

    override fun onPause() {
        super.onPause()
        if (requireActivity().isChangingConfigurations) {
            secondView?.clearFocus()
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
                    width = playerWidth
                    height = playerHeight
                }
            }
        }
    }

    protected fun initializeViewModel(viewModel: PlayerViewModel) {
        playerView.player = viewModel.player
        if (this !is OfflinePlayerFragment) {
            getMainViewModel().user.observe(viewLifecycleOwner, Observer {
                if (it is LoggedIn) {
                    if (viewModel is FollowViewModel) {
                        initializeFollow(this, viewModel, requireView().findViewById(R.id.follow), it)
                    }
                }
            })
        }
    }

//    abstract fun play(obj: Parcelable) //TODO instead maybe add livedata in mainactivity and observe it

    fun minimize() {
        slidingLayout.minimize()
    }

    fun maximize() {
        slidingLayout.maximize()
    }


    private fun showStatusBar() {
        if (isAdded) {
            requireActivity().window.decorView.systemUiVisibility = 0
        }
    }

    private fun hideStatusBar() {
        if (isAdded) {
            requireActivity().window.decorView.systemUiVisibility = systemUiFlags
        }
    }

    override fun onMinimize() {
        playerView.useController = false
        if (!isPortrait) {
            showStatusBar()
        }
    }

    override fun onMaximize() {
        playerView.useController = true
        if (!isPortrait) {
            hideStatusBar()
        }
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
        chatLayout.gone()
        userPrefs.edit { putBoolean(CHAT_OPENED, false) }
    }

    private fun showChat() {
        hideChat.visible()
        showChat.gone()
        chatLayout.visible()
        userPrefs.edit { putBoolean(CHAT_OPENED, true) }
    }
}
