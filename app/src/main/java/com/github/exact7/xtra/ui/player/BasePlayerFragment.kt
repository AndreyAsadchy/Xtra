package com.github.exact7.xtra.ui.player

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.view.SlidingLayout
import com.github.exact7.xtra.util.LifecycleListener
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
