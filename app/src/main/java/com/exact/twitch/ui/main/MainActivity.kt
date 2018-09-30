package com.exact.twitch.ui.main

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.exact.twitch.R
import com.exact.twitch.model.OfflineVideo
import com.exact.twitch.model.clip.Clip
import com.exact.twitch.model.stream.Stream
import com.exact.twitch.model.video.Video
import com.exact.twitch.ui.Scrollable
import com.exact.twitch.ui.clips.BaseClipsFragment
import com.exact.twitch.ui.common.OnChannelClickedListener
import com.exact.twitch.ui.downloads.DownloadsFragment
import com.exact.twitch.ui.games.GamesFragment
import com.exact.twitch.ui.menu.MenuFragment
import com.exact.twitch.ui.pagers.FollowPagerFragment
import com.exact.twitch.ui.pagers.TopPagerFragment
import com.exact.twitch.ui.player.BasePlayerFragment
import com.exact.twitch.ui.player.clip.ClipPlayerFragment
import com.exact.twitch.ui.player.offline.OfflinePlayerFragment
import com.exact.twitch.ui.player.stream.StreamPlayerFragment
import com.exact.twitch.ui.player.video.VideoPlayerFragment
import com.exact.twitch.ui.streams.BaseStreamsFragment
import com.exact.twitch.ui.videos.BaseVideosFragment
import com.exact.twitch.ui.view.draggableview.DraggableListener
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavSwitchController
import com.ncapdevi.fragnav.FragNavTransactionOptions
import com.ncapdevi.fragnav.tabhistory.UniqueTabHistoryStrategy
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), BaseStreamsFragment.OnStreamSelectedListener, OnChannelClickedListener, BaseClipsFragment.OnClipSelectedListener, BaseVideosFragment.OnVideoSelectedListener, HasSupportFragmentInjector, DraggableListener, DownloadsFragment.OnVideoSelectedListener {

    companion object {
        private const val PLAYER_TAG = "player"
        const val INDEX_GAMES = FragNavController.TAB1
        const val INDEX_TOP = FragNavController.TAB2
        const val INDEX_FOLLOWED = FragNavController.TAB3
        const val INDEX_DOWNLOADS = FragNavController.TAB4
        const val INDEX_MENU = FragNavController.TAB5
    }

    @Inject lateinit var dispatchingFragmentInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: MainActivityViewModel
    val fragNavController = FragNavController(supportFragmentManager, R.id.fragmentContainer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainActivityViewModel::class.java)
        viewModel.isPlayerOpened.observe(this, Observer {
            if (it == true) {
                val playerFragment = supportFragmentManager.findFragmentByTag(PLAYER_TAG) as BasePlayerFragment?
                if (viewModel.isPlayerMaximized.value != true)
                    Handler().post { playerFragment?.minimize() } //TODO add minimize fast
            }
        })
        viewModel.isPlayerMaximized.observe(this, Observer { if (it == true) navBar.post { hideNavigationBar() } }) //TODO post method
        //TODO add token validation
//        val token = getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", null)
//        val isFirstLaunch = getPreferences(Context.MODE_PRIVATE).getBoolean("first_launch", true)
//        if (isFirstLaunch) {
        //            navController.navigate(R.id.activity_login, null, new NavOptions.Builder().setPopUpTo(R.id.fragment_top, false).build());
//        }
        fragNavController.apply {
            rootFragments = listOf(GamesFragment(), TopPagerFragment(), FollowPagerFragment(), DownloadsFragment(), MenuFragment())
            fragmentHideStrategy = FragNavController.DETACH_ON_NAVIGATE_HIDE_ON_SWITCH
            navigationStrategy = UniqueTabHistoryStrategy(object : FragNavSwitchController {
                override fun switchTab(index: Int, transactionOptions: FragNavTransactionOptions?) {
                    println(index)
                    navBar.selectedItemId = index
                }
            })
            initialize(INDEX_FOLLOWED, savedInstanceState)
        }
        navBar.apply {
            selectedItemId = R.id.fragment_follow

            setOnNavigationItemSelectedListener {
                val index = when (it.itemId) {
                    R.id.fragment_games -> INDEX_GAMES
                    R.id.fragment_top -> INDEX_TOP
                    R.id.fragment_follow -> INDEX_FOLLOWED
                    R.id.fragment_downloads -> INDEX_DOWNLOADS
                    R.id.fragment_menu -> INDEX_MENU
                    else -> throw IllegalArgumentException()
                }
                fragNavController.switchTab(index)
                return@setOnNavigationItemSelectedListener true
            }

            setOnNavigationItemReselectedListener {
                val currentFragment = fragNavController.currentFrag
                when (it.itemId) {
                    R.id.fragment_games -> {
                        when (currentFragment) {
                            is GamesFragment -> currentFragment.scrollToTop()
                            else -> fragNavController.popFragment()
                        }
                    }
                    else -> {
                        if (currentFragment is Scrollable) {
                            currentFragment.scrollToTop()
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        fragNavController.onSaveInstanceState(outState)
    }

    override fun startStream(stream: Stream) {
        startPlayer(StreamPlayerFragment().apply { arguments = bundleOf("stream" to stream) })
    }

    override fun startVideo(video: Video) {
        startPlayer(VideoPlayerFragment().apply { arguments = bundleOf("video" to video) })
    }

    override fun startClip(clip: Clip) {
        startPlayer(ClipPlayerFragment().apply { arguments = bundleOf("clip" to clip) })
    }

    override fun startOfflineVideo(video: OfflineVideo) {
        startPlayer(OfflinePlayerFragment().apply { arguments = bundleOf("video" to video) })
    }

    override fun viewChannel(channelName: String) {
        //TODO
    }

    override fun onBackPressed() {
        if (viewModel.isPlayerMaximized.value != true || (!fragNavController.isRootFragment && fragNavController.popFragment().not())) {
            super.onBackPressed()
        } else {
            (supportFragmentManager.findFragmentByTag(PLAYER_TAG) as BasePlayerFragment).minimize()
            viewModel.isPlayerMaximized.postValue(false)
        }
    }

    override fun onMaximized() {
        viewModel.isPlayerMaximized.postValue(true)
    }

    override fun onMinimized() {
        viewModel.isPlayerMaximized.postValue(false)
    }

    override fun onClosedToLeft() {
        closePlayer()
    }

    override fun onClosedToRight() {
        closePlayer()
    }

    override fun onMoved(horizontalDragOffset: Float, verticalDragOffset: Float) {
        navBar.translationY = -verticalDragOffset * navBar.height + navBar.height
    }

    private fun startPlayer(fragment: BasePlayerFragment) {
        hideNavigationBar()
        supportFragmentManager.beginTransaction().replace(R.id.playerContainer, fragment, PLAYER_TAG).commit()
        viewModel.isPlayerOpened.postValue(true)
        viewModel.isPlayerMaximized.postValue(true)
    }

    private fun closePlayer() {
        supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentByTag(PLAYER_TAG)!!).commit()
        viewModel.isPlayerOpened.postValue(false)
        viewModel.isPlayerMaximized.postValue(false)
    }

    private fun hideNavigationBar() {
        navBar.translationY = navBar.height.toFloat()
    }

    override fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return dispatchingFragmentInjector
    }
}
