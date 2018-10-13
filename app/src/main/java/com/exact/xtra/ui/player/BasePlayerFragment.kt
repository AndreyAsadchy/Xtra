package com.exact.xtra.ui.player

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProvider
import com.exact.xtra.R
import com.exact.xtra.di.Injectable
import com.exact.xtra.ui.common.OnChannelClickedListener
import com.exact.xtra.ui.view.draggableview.DraggableListener
import com.exact.xtra.ui.view.draggableview.DraggableView
import kotlinx.android.synthetic.main.fragment_player_stream.*
import javax.inject.Inject

abstract class BasePlayerFragment : Fragment(), Injectable, LifecycleObserver {

    private var channelListener: OnChannelClickedListener? = null
    private var dragListener: DraggableListener? = null
    private var isPortraitOrientation: Boolean = false
    private lateinit var draggableView: DraggableView

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
        if (isPortraitOrientation) {
            view.findViewById<ImageButton>(R.id.minimize).setOnClickListener { minimize() }
            draggableView = view as DraggableView
            draggableView.setDraggableListener(dragListener)
            view.findViewById<ImageButton>(R.id.fullscreen).setOnClickListener { requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        } else {
            //            slidingView = view.findViewById(R.id.fragment_player_sv);
            view.findViewById<ImageButton>(R.id.fullscreen_exit).setOnClickListener { requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        }
    }

    override fun onDetach() {
        super.onDetach()
        channelListener = null
        dragListener = null
    }

    abstract fun play(obj: Parcelable)

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun onMoveToForeground() {
        playerView.player = viewModel.player
        viewModel.play()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    open fun onMoveToBackground() {
        viewModel.player.stop()
    }

    protected abstract val viewModel: PlayerViewModel

    fun minimize() {
        if (isPortraitOrientation) {
            draggableView.minimize()
        } else {
            //            slidingView.minimize();
        }
    }
}
