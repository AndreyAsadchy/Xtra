package com.github.exact7.xtra.ui.player

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.postDelayed
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.ui.common.AlertDialogFragment
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.common.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.common.follow.FollowFragment
import com.github.exact7.xtra.ui.common.follow.FollowViewModel
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.player.clip.ClipPlayerFragment
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


@Suppress("PLUGIN_WARNING")
abstract class BasePlayerFragment : BaseNetworkFragment(), RadioButtonDialogFragment.OnSortOptionChanged, Injectable, LifecycleListener, SlidingLayout.Listener, FollowFragment, SleepTimerDialog.OnSleepTimerStartedListener, AlertDialogFragment.OnDialogResultListener {

    private lateinit var slidingLayout: SlidingLayout
    private lateinit var playerView: PlayerView
    private lateinit var chatLayout: ViewGroup
    private var secondView: ViewGroup? = null
    private lateinit var showChat: ImageButton
    private lateinit var hideChat: ImageButton

    protected abstract val viewModel: PlayerViewModel

    protected var isPortrait: Boolean = false
        private set
    private var wasInPictureInPicture = false
    private var orientationBeforePictureInPicture = 0
    private var isKeyboardShown = false

    protected abstract val shouldEnterPictureInPicture: Boolean
    open val controllerAutoShow: Boolean = true
    open val controllerShowTimeoutMs: Int = 3000

    private lateinit var prefs: SharedPreferences
    private lateinit var userPrefs: SharedPreferences
    protected abstract val channel: Channel

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
        val activity = requireActivity()
        prefs = activity.prefs()
        userPrefs = activity.getSharedPreferences(C.USER_PREFS, Context.MODE_PRIVATE)
        activity.window.decorView.setOnSystemUiVisibilityChangeListener {
            if (!isPortrait && !isKeyboardShown && slidingLayout.isMaximized) {
                hideStatusBar()
            }
        }
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
            view.findViewById<ImageButton>(R.id.fullscreenEnter).setOnClickListener { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE }
            showStatusBar()
        } else {
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
            playerView.updateLayoutParams { height = prefs.getInt(C.PORTRAIT_PLAYER_HEIGHT, 0) }
            prefs.getInt(C.ASPECT_RATIO_PORTRAIT, AspectRatioFrameLayout.RESIZE_MODE_FILL)
        } else {
            prefs.getInt(C.ASPECT_RATIO_LANDSCAPE, AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH)
        }
        playerView.resizeMode = resizeMode
        playerView.controllerAutoShow = controllerAutoShow
        view.findViewById<ImageButton>(R.id.aspectRatio).setOnClickListener {
            resizeMode = (resizeMode + 1).let { if (it < 5) it else 0 }
            playerView.resizeMode = resizeMode
            prefs.edit { putInt(if (isPortrait) C.ASPECT_RATIO_PORTRAIT else C.ASPECT_RATIO_LANDSCAPE, resizeMode) }
        }
        playerView.postDelayed(750L) {
            playerWidth = playerView.width
            playerHeight = playerView.height
        }
        if (this !is OfflinePlayerFragment) {
            chatLayout = view.findViewById(if (this !is ClipPlayerFragment) R.id.chatFragmentContainer else R.id.clipChatContainer)
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
        if (this is StreamPlayerFragment) {
            if (savedInstanceState == null) {
                slidingLayout.viewTreeObserver.addOnGlobalLayoutListener {
                    if (slidingLayout.isKeyboardShown) {
                        if (!isKeyboardShown) {
                            isKeyboardShown = true
                            if (!isPortrait) {
                                chatLayout.updateLayoutParams { width = (slidingLayout.width / 1.8f).toInt() }
                                showStatusBar()
                            }
                        }
                    } else {
                        if (isKeyboardShown) {
                            isKeyboardShown = false
                            secondView?.clearFocus()
                            if (!isPortrait) {
                                chatLayout.updateLayoutParams { width = chatWidth }
                                if (slidingLayout.isMaximized) {
                                    hideStatusBar()
                                }
                            }
                        }
                    }
                }
            }
        } else {
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
    }

    override fun onPause() {
        super.onPause()
        if (requireActivity().isChangingConfigurations) {
            secondView?.clearFocus()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val activity = requireActivity()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || (!activity.isInPictureInPictureMode && (!wasInPictureInPicture || orientationBeforePictureInPicture != newConfig.orientation.also { wasInPictureInPicture = false }))) {
            activity.supportFragmentManager.beginTransaction().detach(this).attach(this).commit()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        if (isInPictureInPictureMode) {
            playerView.apply {
                useController = false
                updateLayoutParams {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
            secondView?.gone()
        } else {
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

    override fun initialize() {
        val activity = requireActivity() as MainActivity
        val view = requireView()
        viewModel.currentPlayer.observe(viewLifecycleOwner, Observer {
            playerView.player = it
        })
        viewModel.playerMode.observe(viewLifecycleOwner, Observer {
            if (it == PlayerMode.NORMAL) {
                playerView.controllerHideOnTouch = true
                playerView.controllerShowTimeoutMs = controllerShowTimeoutMs
            } else {
                playerView.controllerHideOnTouch = false
                playerView.controllerShowTimeoutMs = -1
                playerView.showController()
            }
        })
        if (this !is OfflinePlayerFragment) {
            User.get(activity).let {
                if (it is LoggedIn) {
                    initializeFollow(this, (viewModel as FollowViewModel), view.findViewById(R.id.follow), it)
                }
            }
        }
        if (this !is ClipPlayerFragment) {
            viewModel.sleepTimer.observe(viewLifecycleOwner, Observer {
                activity.closePlayer()
            })
            view.findViewById<ImageButton>(R.id.sleepTimer).setOnClickListener {
                SleepTimerDialog.show(childFragmentManager, viewModel.timerTimeLeft)
            }
        } else { //TODO
            view.findViewById<ImageButton>(R.id.sleepTimer).gone()
        }
    }

    override fun onMinimize() {
        playerView.useController = false
        if (!isPortrait) {
            showStatusBar()
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override fun onMaximize() {
        playerView.useController = true
        if (!playerView.controllerHideOnTouch) { //TODO
            playerView.showController()
        }
        if (!isPortrait) {
            hideStatusBar()
        }
    }

    override fun onClose() {

    }

    override fun onSleepTimerChanged(durationMs: Long, hours: Int, minutes: Int) {
        val context = requireContext()
        if (durationMs > 0L) {
            Toast.makeText(context, when {
                hours == 0 -> getString(R.string.playback_will_stop, resources.getQuantityString(R.plurals.minutes, minutes, minutes))
                minutes == 0 -> getString(R.string.playback_will_stop, resources.getQuantityString(R.plurals.hours, hours, hours))
                else -> getString(R.string.playback_will_stop_hours_minutes, resources.getQuantityString(R.plurals.hours, hours, hours), resources.getQuantityString(R.plurals.minutes, minutes, minutes))
            }, Toast.LENGTH_LONG).show()
        } else if (viewModel.timerTimeLeft > 0L) {
            Toast.makeText(context, getString(R.string.timer_canceled), Toast.LENGTH_LONG).show()
        }
        viewModel.setTimer(durationMs)
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int) {
        when (requestCode) {
            REQUEST_FOLLOW -> {
                //TODO
            }
        }
    }

    //    abstract fun play(obj: Parcelable) //TODO instead maybe add livedata in mainactivity and observe it

    fun minimize() {
        slidingLayout.minimize()
    }

    fun maximize() {
        slidingLayout.maximize()
    }

    fun enterPictureInPicture(): Boolean {
        return if (slidingLayout.isMaximized && prefs.getBoolean(C.PICTURE_IN_PICTURE, true) && shouldEnterPictureInPicture) {
            wasInPictureInPicture = true
            orientationBeforePictureInPicture = resources.configuration.orientation
            true
        } else {
            false
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

    private companion object {
        const val CHAT_OPENED = "ChatOpened"

        const val REQUEST_FOLLOW = 0
    }
}
