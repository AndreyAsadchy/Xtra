package com.github.exact7.xtra.ui.player

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.WindowManager
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.view.draggableview.DraggableListener
import com.github.exact7.xtra.ui.view.draggableview.DraggableView
import com.github.exact7.xtra.util.LifecycleListener
import kotlinx.android.synthetic.main.player_stream.*

@Suppress("PLUGIN_WARNING")
abstract class BasePlayerFragment : BaseNetworkFragment(), Injectable, LifecycleListener {

//    private var channelListener: OnChannelSelectedListener? = null
    private var dragListener: DraggableListener? = null
    private lateinit var draggableView: DraggableView
    protected abstract val viewModel: PlayerViewModel
    protected var isPortraitOrientation: Boolean = false
        private set

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        if (context is OnChannelSelectedListener) {
//            channelListener = context
//        } else {
//            throw RuntimeException(context.toString() + " must implement OnViewChannelClickedListener")
//        }
        if (context is DraggableListener) {
            dragListener = context
        } else {
            throw RuntimeException("$context must implement DraggableListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPortraitOrientation = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.keepScreenOn = true
        val activity = requireActivity()

        if (isPortraitOrientation) {
            draggableView = view as DraggableView
            draggableView.setDraggableListener(dragListener)
            minimize.setOnClickListener { minimize() }
            fullscreenEnter.setOnClickListener { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        } else {
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
            //            slidingView = view.findViewById(R.id.fragment_player_sv);
            fullscreenExit.setOnClickListener {
                activity.apply {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }

            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (!isPortraitOrientation) {
            val activity = requireActivity()
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
//        channelListener = null
        dragListener = null
    }

    override fun onMovedToForeground() {
        viewModel.onResume()
    }

    override fun onMovedToBackground() {
        viewModel.onPause()
    }

    override fun onNetworkRestored() {
        viewModel.onResume()
    }

    abstract fun play(obj: Parcelable) //TODO instead maybe add livedata in mainactivity and observe it

    fun minimize() {
        if (isPortraitOrientation) {
            draggableView.minimize()
        } else {
//                        slidingView.minimize();
        }
    }
}
