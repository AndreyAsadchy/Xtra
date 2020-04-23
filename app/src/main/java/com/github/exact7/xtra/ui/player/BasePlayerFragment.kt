package com.github.exact7.xtra.ui.player

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.view.postDelayed
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.NotLoggedIn
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
import com.github.exact7.xtra.util.hideKeyboard
import com.github.exact7.xtra.util.isInPortraitOrientation
import com.github.exact7.xtra.util.isKeyboardShown
import com.github.exact7.xtra.util.prefs
import com.github.exact7.xtra.util.toast
import com.github.exact7.xtra.util.visible
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView


@Suppress("PLUGIN_WARNING")
abstract class BasePlayerFragment : BaseNetworkFragment(), RadioButtonDialogFragment.OnSortOptionChanged, Injectable, LifecycleListener, SlidingLayout.Listener, FollowFragment, SleepTimerDialog.OnSleepTimerStartedListener, AlertDialogFragment.OnDialogResultListener {

    private lateinit var slidingLayout: SlidingLayout
    private lateinit var playerView: PlayerView
    private lateinit var chatLayout: ViewGroup
    private lateinit var fullscreenToggle: ImageButton
    private lateinit var showChat: ImageButton
    private lateinit var hideChat: ImageButton

    protected abstract val layoutId: Int
    protected abstract val chatContainerId: Int

    protected abstract val viewModel: PlayerViewModel

    protected var isPortrait = false
        private set
    private var orientationBeforePictureInPicture = 0
    private var isKeyboardShown = false

    protected abstract val shouldEnterPictureInPicture: Boolean
    open val controllerAutoShow: Boolean = true
    open val controllerShowTimeoutMs: Int = 3000
    private var resizeMode = 0

    private lateinit var prefs: SharedPreferences
    private lateinit var userPrefs: SharedPreferences
    protected abstract val channel: Channel

    var playerWidth = 0
    var playerHeight = 0
    private var playerHeightPortrait = 0
    private var chatWidthLandscape = 0

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            systemUiFlags = systemUiFlags or (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        isPortrait = activity.isInPortraitOrientation
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutId, container, false).also {
            (it as LinearLayout).orientation = if (isPortrait) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.keepScreenOn = true
        val activity = requireActivity() as MainActivity
        slidingLayout = view as SlidingLayout
        slidingLayout.addListener(activity)
        slidingLayout.addListener(this)
        slidingLayout.maximizedSecondViewVisibility = if (userPrefs.getBoolean(KEY_CHAT_OPENED, true)) View.VISIBLE else View.GONE //TODO
        playerView = view.findViewById(R.id.playerView)
        chatLayout = view.findViewById(chatContainerId)
        playerView.postDelayed(750L) {
            playerWidth = playerView.width
            playerHeight = playerView.height
        }
        playerHeightPortrait = prefs.getInt(C.PORTRAIT_PLAYER_HEIGHT, 0)
        chatWidthLandscape = prefs.getInt(C.LANDSCAPE_CHAT_WIDTH, 0)
        hideChat = view.findViewById<ImageButton>(R.id.hideChat).apply {
            setOnClickListener { hideChat() }
        }
        showChat = view.findViewById<ImageButton>(R.id.showChat).apply {
            setOnClickListener { showChat() }
        }
        fullscreenToggle = view.findViewById<ImageButton>(R.id.fullscreenToggle).apply {
            setOnClickListener {
                activity.apply {
                    if (isPortrait) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    } else {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }
            }
        }
        initLayout()
        if (this !is OfflinePlayerFragment) {
            view.findViewById<ImageButton>(R.id.settings).disable()
            view.findViewById<TextView>(R.id.channel).apply {
                text = channel.displayName
                setOnClickListener {
                    activity.viewChannel(channel)
                    slidingLayout.minimize()
                }
            }
        }

        playerView.controllerAutoShow = controllerAutoShow
        view.findViewById<ImageButton>(R.id.aspectRatio).setOnClickListener {
            resizeMode = (resizeMode + 1).let { if (it < 5) it else 0 }
            playerView.resizeMode = resizeMode
            prefs.edit { putInt(if (isPortrait) C.ASPECT_RATIO_PORTRAIT else C.ASPECT_RATIO_LANDSCAPE, resizeMode) }
        }
        view.findViewById<ImageButton>(R.id.minimize).setOnClickListener { minimize() }
        if (this is StreamPlayerFragment) {
            if (User.get(activity) !is NotLoggedIn) {
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
                            chatLayout.clearFocus()
                            if (!isPortrait) {
                                chatLayout.updateLayoutParams { width = chatWidthLandscape }
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !requireActivity().isInPictureInPictureMode) {
            chatLayout.hideKeyboard()
            initLayout()
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
            chatLayout.gone()
        } else {
            playerView.useController = true
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
            context.toast(when {
                hours == 0 -> getString(R.string.playback_will_stop, resources.getQuantityString(R.plurals.minutes, minutes, minutes))
                minutes == 0 -> getString(R.string.playback_will_stop, resources.getQuantityString(R.plurals.hours, hours, hours))
                else -> getString(R.string.playback_will_stop_hours_minutes, resources.getQuantityString(R.plurals.hours, hours, hours), resources.getQuantityString(R.plurals.minutes, minutes, minutes))
            })
        } else if (viewModel.timerTimeLeft > 0L) {
            context.toast(R.string.timer_canceled)
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
            orientationBeforePictureInPicture = resources.configuration.orientation
            true
        } else {
            false
        }
    }

    private fun initLayout() {
        if (isPortrait) {
            requireActivity().window.decorView.setOnSystemUiVisibilityChangeListener(null)
            playerView.updateLayoutParams<LinearLayout.LayoutParams> {
                height = playerHeightPortrait
                width = LinearLayout.LayoutParams.MATCH_PARENT
                weight = 0f
            }
            chatLayout.updateLayoutParams<LinearLayout.LayoutParams> {
                width = LinearLayout.LayoutParams.MATCH_PARENT
                height = LinearLayout.LayoutParams.MATCH_PARENT
                weight = 1f
            }
            chatLayout.visible()
            fullscreenToggle.setImageResource(R.drawable.baseline_fullscreen_black_24)
            hideChat.gone()
            showChat.gone()
            showStatusBar()
            resizeMode = prefs.getInt(C.ASPECT_RATIO_PORTRAIT, AspectRatioFrameLayout.RESIZE_MODE_FILL)
        } else {
            requireActivity().window.decorView.setOnSystemUiVisibilityChangeListener {
                if (!isKeyboardShown && slidingLayout.isMaximized) {
                    hideStatusBar()
                }
            }
            playerView.updateLayoutParams<LinearLayout.LayoutParams> {
                height = LinearLayout.LayoutParams.MATCH_PARENT
                width = LinearLayout.LayoutParams.MATCH_PARENT
                weight = 1f
            }
            if (this !is OfflinePlayerFragment) {
                chatLayout.updateLayoutParams<LinearLayout.LayoutParams> {
                    width = chatWidthLandscape
                    height = LinearLayout.LayoutParams.MATCH_PARENT
                    weight = 0f
                }
                setPreferredChatVisibility()
            } else {
                chatLayout.gone()
            }
            fullscreenToggle.setImageResource(R.drawable.baseline_fullscreen_exit_black_24)
            slidingLayout.post {
                if (slidingLayout.isMaximized) {
                    hideStatusBar()
                } else {
                    showStatusBar()
                }
            }
            resizeMode = prefs.getInt(C.ASPECT_RATIO_LANDSCAPE, AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH)
        }
        playerView.resizeMode = resizeMode
    }

    private fun setPreferredChatVisibility() {
        if (userPrefs.getBoolean(KEY_CHAT_OPENED, true)) showChat() else hideChat()
    }

    private fun hideChat() {
        hideChat.gone()
        showChat.visible()
        chatLayout.gone()
        userPrefs.edit { putBoolean(KEY_CHAT_OPENED, false) }
    }

    private fun showChat() {
        hideChat.visible()
        showChat.gone()
        chatLayout.visible()
        userPrefs.edit { putBoolean(KEY_CHAT_OPENED, true) }
    }

    private fun showStatusBar() {
        if (isAdded) { //TODO this check might not be needed anymore AND ANDROID 5
            requireActivity().window.decorView.systemUiVisibility = 0
        }
    }

    private fun hideStatusBar() {
        if (isAdded) {
            requireActivity().window.decorView.systemUiVisibility = systemUiFlags
        }
    }

    private companion object {
        const val KEY_CHAT_OPENED = "ChatOpened"

        const val REQUEST_FOLLOW = 0
    }
}
