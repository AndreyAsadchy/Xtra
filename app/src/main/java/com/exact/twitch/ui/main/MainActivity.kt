package com.exact.twitch.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.exact.twitch.R
import com.exact.twitch.model.OfflineVideo
import com.exact.twitch.model.User
import com.exact.twitch.model.clip.Clip
import com.exact.twitch.model.stream.Stream
import com.exact.twitch.model.video.Video
import com.exact.twitch.repository.AuthRepository
import com.exact.twitch.ui.Scrollable
import com.exact.twitch.ui.clips.BaseClipsFragment
import com.exact.twitch.ui.common.OnChannelClickedListener
import com.exact.twitch.ui.downloads.DownloadsFragment
import com.exact.twitch.ui.games.GamesFragment
import com.exact.twitch.ui.login.LoginActivity
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
import com.exact.twitch.util.C
import com.exact.twitch.util.TwitchApiHelper
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavSwitchController
import com.ncapdevi.fragnav.FragNavTransactionOptions
import com.ncapdevi.fragnav.tabhistory.UniqueTabHistoryStrategy
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
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

    @Inject lateinit var dispatchingFragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var authRepository: AuthRepository
    private lateinit var viewModel: MainActivityViewModel
    private val compositeDisposable = CompositeDisposable()
    val fragNavController = FragNavController(supportFragmentManager, R.id.fragmentContainer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainActivityViewModel::class.java)
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("first_launch", true)
        if (isFirstLaunch) {
            prefs.edit { putBoolean("first_launch", false) }
            startActivityForResult(Intent(this, LoginActivity::class.java).apply { putExtra("first_launch", true) }, 1)
        } else {
            initFragNavController()
            val user = TwitchApiHelper.getUserData(this@MainActivity)
            if (user != null) {
                navBar.selectedItemId = R.id.fragment_follow
                authRepository.validate(user.token)
                        .subscribe({
                            viewModel.userToken.postValue(user.token)
                            viewModel.username.postValue(user.name)
                            fragNavController.initialize(INDEX_FOLLOWED, savedInstanceState)
                        }, {
                            getSharedPreferences(C.AUTH_PREFS, Context.MODE_PRIVATE).edit { clear() }
                            //TODO prompt to redo username
                        })
                        .addTo(compositeDisposable)
            } else {
                navBar.selectedItemId = R.id.fragment_top
                fragNavController.initialize(INDEX_TOP, savedInstanceState)
            }
            initNavBar()
        }
        with (viewModel) {
            isPlayerOpened.observe(this@MainActivity, Observer {
                if (it == true) {
                    val playerFragment = supportFragmentManager.findFragmentByTag(PLAYER_TAG) as BasePlayerFragment?
                    if (viewModel.isPlayerMaximized.value != true)
                        Handler().post { playerFragment?.minimize() } //TODO add minimize fast
                }
            })
            isPlayerMaximized.observe(this@MainActivity, Observer { if (it == true) navBar.post { hideNavigationBar() } }) //TODO post method
        }

    }

    private fun initFragNavController() {
        fragNavController.apply {
            rootFragments = listOf(GamesFragment(), TopPagerFragment(), FollowPagerFragment(), DownloadsFragment(), MenuFragment())
            fragmentHideStrategy = FragNavController.DETACH_ON_NAVIGATE_HIDE_ON_SWITCH
            navigationStrategy = UniqueTabHistoryStrategy(object : FragNavSwitchController {
                override fun switchTab(index: Int, transactionOptions: FragNavTransactionOptions?) {
                    println(index)
                    navBar.selectedItemId = index
                }
            })
        }
    }

    private fun initNavBar() {
        navBar.apply {
            setOnNavigationItemSelectedListener {
                val index = when (it.itemId) {
                    R.id.fragment_games -> INDEX_GAMES
                    R.id.fragment_top -> INDEX_TOP
                    R.id.fragment_follow -> {
                        if (viewModel.isUserLoggedIn) {
                            INDEX_FOLLOWED
                        } else {
                            INDEX_MENU
                        }
                    }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) { //Result of Login Activity //TODO add requestcode 2 for clicking login after already launched
            initFragNavController()
            when (resultCode) {
                Activity.RESULT_OK -> {
                    with (viewModel) {
                        data?.getParcelableExtra<User>(C.USER)?.let {
                            userToken.postValue(it.token)
                            username.postValue(it.name)
                        }
                    }
                    navBar.selectedItemId = R.id.fragment_follow
                    if (fragNavController.size == 0) {
                        fragNavController.initialize(INDEX_FOLLOWED)
                    } else {
                        fragNavController.switchTab(INDEX_FOLLOWED)
                    }
                }
                Activity.RESULT_CANCELED -> {
                    navBar.selectedItemId = R.id.fragment_top
                    fragNavController.initialize(INDEX_TOP)
                }
            }
            initNavBar()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (fragNavController.size > 0)
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

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingFragmentInjector
    }
}
