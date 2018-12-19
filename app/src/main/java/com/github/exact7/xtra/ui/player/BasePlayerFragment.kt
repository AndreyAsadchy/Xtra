package com.github.exact7.xtra.ui.player

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.ui.common.OnChannelClickedListener
import com.github.exact7.xtra.ui.view.draggableview.DraggableListener
import com.github.exact7.xtra.ui.view.draggableview.DraggableView
import com.github.exact7.xtra.util.LifecycleListener
import kotlinx.android.synthetic.main.player_stream.*
import javax.inject.Inject

@Suppress("PLUGIN_WARNING")
abstract class BasePlayerFragment : Fragment(), Injectable, LifecycleListener {

    private var channelListener: OnChannelClickedListener? = null
    private var dragListener: DraggableListener? = null
    private lateinit var draggableView: DraggableView
    protected abstract val viewModel: PlayerViewModel
    protected var isPortraitOrientation: Boolean = false
    private var decorView: View? = null


    @Inject
    protected lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnChannelClickedListener) {
            channelListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnViewChannelClickedListener")
        }
        if (context is DraggableListener) {
            dragListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement DraggableListener")
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
            minimize.setOnClickListener { minimize() }
            draggableView = view as DraggableView
            draggableView.setDraggableListener(dragListener)
            fullscreenEnter.setOnClickListener { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        } else {
            decorView = activity.window.decorView
            hideStatusBar()

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
//            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
//                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
//                    hideStatusBar()
//                }
//            }
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
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            decorView?.systemUiVisibility = 0
        }
        channelListener = null
        dragListener = null
    }

    fun onWindowFocusChanged(hasFocus:Boolean) {
        if (!isPortraitOrientation && hasFocus) {
            hideStatusBar()
        }
    }

    private fun hideStatusBar() {
        decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onMovedToForeground() {
//        viewModel.play()
    }

    override fun onMovedToBackground() {
        viewModel.player.stop()
    }

    abstract fun play(obj: Parcelable)

    fun minimize() {
        if (isPortraitOrientation) {
            draggableView.minimize()
        } else {
//                        slidingView.minimize();
        }
    }
}
