package com.github.exact7.xtra.ui.player

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import androidx.preference.PreferenceManager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.player.offline.OfflinePlayerFragment
import com.github.exact7.xtra.ui.player.stream.StreamPlayerFragment
import com.github.exact7.xtra.ui.view.SlidingLayout
import com.github.exact7.xtra.util.LifecycleListener
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.player_stream.*

@Suppress("PLUGIN_WARNING")
abstract class BasePlayerFragment : BaseNetworkFragment(), Injectable, LifecycleListener, SlidingLayout.Listener {

    private lateinit var slidingLayout: SlidingLayout
    protected var isPortrait: Boolean = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.keepScreenOn = true
        val activity = requireActivity() as MainActivity
        slidingLayout = view as SlidingLayout
        slidingLayout.addListener(activity)
        slidingLayout.addListener(this)
        minimize.setOnClickListener { minimize() }
        if (isPortrait) {
            fullscreenEnter.setOnClickListener { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        } else {
            hideStatusBar()
            fullscreenExit.setOnClickListener {
                activity.apply {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }

            }
        }
        if (this !is OfflinePlayerFragment) {
            view.findViewById<ImageButton>(R.id.settings).apply {
                isEnabled = false
                setColorFilter(Color.GRAY)
            }
        }
        if (this !is StreamPlayerFragment) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
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
            view.findViewById<PlayerView>(R.id.playerView).apply {
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
}
