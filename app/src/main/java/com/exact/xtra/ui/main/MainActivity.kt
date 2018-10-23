package com.exact.xtra.ui.main

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.exact.xtra.R
import com.exact.xtra.model.OfflineVideo
import com.exact.xtra.model.User
import com.exact.xtra.model.clip.Clip
import com.exact.xtra.model.stream.Stream
import com.exact.xtra.model.video.Video
import com.exact.xtra.repository.AuthRepository
import com.exact.xtra.ui.Scrollable
import com.exact.xtra.ui.clips.BaseClipsFragment
import com.exact.xtra.ui.common.OnChannelClickedListener
import com.exact.xtra.ui.downloads.DownloadsFragment
import com.exact.xtra.ui.games.GamesFragment
import com.exact.xtra.ui.login.LoginActivity
import com.exact.xtra.ui.menu.MenuFragment
import com.exact.xtra.ui.pagers.FollowPagerFragment
import com.exact.xtra.ui.pagers.TopPagerFragment
import com.exact.xtra.ui.player.BasePlayerFragment
import com.exact.xtra.ui.player.clip.ClipPlayerFragment
import com.exact.xtra.ui.player.offline.OfflinePlayerFragment
import com.exact.xtra.ui.player.stream.StreamPlayerFragment
import com.exact.xtra.ui.player.video.VideoPlayerFragment
import com.exact.xtra.ui.streams.BaseStreamsFragment
import com.exact.xtra.ui.videos.BaseVideosFragment
import com.exact.xtra.ui.view.draggableview.DraggableListener
import com.exact.xtra.util.C
import com.exact.xtra.util.TwitchApiHelper
import com.ncapdevi.fragnav.FragNavController
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
    private lateinit var viewModel: MainViewModel
    private val compositeDisposable = CompositeDisposable()
    private var playerFragment: BasePlayerFragment? = null
    private val handler by lazy { Handler() }
    val fragNavController = FragNavController(supportFragmentManager, R.id.fragmentContainer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("first_launch", true)
        if (!isFirstLaunch) {
            val user = TwitchApiHelper.getUserData(this)
            if (user != null) {
                navBar.selectedItemId = R.id.fragment_follow

                fun init() {
                    initFragNavController()
                    viewModel.user.value = user
                    fragNavController.initialize(INDEX_FOLLOWED, savedInstanceState)
                }

                if (viewModel.hasValidated) {
                    init()
                } else {
                    viewModel.hasValidated = true
                    authRepository.validate(user.token + " a")
                            .subscribe({
                                init()
                            }, {
                                viewModel.user.value = null
                                getSharedPreferences(C.AUTH_PREFS, Context.MODE_PRIVATE).edit { clear() }
                                Toast.makeText(this, getString(R.string.token_expired), Toast.LENGTH_LONG).show()
                                startActivityForResult(Intent(this, LoginActivity::class.java), 1)
                            })
                            .addTo(compositeDisposable)
                }
            } else {
                initFragNavController()
                viewModel.user.value = null
                navBar.selectedItemId = R.id.fragment_top
                fragNavController.initialize(INDEX_TOP, savedInstanceState)
            }
            initNavBar()

        } else {
            prefs.edit { putBoolean("first_launch", false) }
            startActivityForResult(Intent(this, LoginActivity::class.java).apply { putExtra("first_launch", true) }, 1)

        }
        if (viewModel.isPlayerOpened) {
            playerFragment = supportFragmentManager.findFragmentByTag(PLAYER_TAG) as BasePlayerFragment?
        }
        viewModel.playerMaximized().observe(this, Observer {
            val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE //TODO change
            if (!isLandscape) {
                if (it == true) {
                    handler.post { hideNavigationBar() }
                } else {
                    handler.post { playerFragment!!.minimize() } //TODO add minimize fast without callback
                }
            } else {
                navBarContainer.visibility = View.GONE
            }
        })
    }


    private fun initFragNavController() {
        fragNavController.apply {
            rootFragments = listOf(GamesFragment(), TopPagerFragment(), FollowPagerFragment(), DownloadsFragment(), MenuFragment())
            fragmentHideStrategy = FragNavController.DETACH_ON_NAVIGATE_HIDE_ON_SWITCH
        }
    }

    private fun initNavBar() {
        navBar.apply {
            setOnNavigationItemSelectedListener {
                val index = when (it.itemId) {
                    R.id.fragment_games -> INDEX_GAMES
                    R.id.fragment_top -> INDEX_TOP
                    R.id.fragment_follow -> {
                        if (viewModel.user.value != null) {
                            INDEX_FOLLOWED
                        } else {
                            INDEX_MENU //TODO create another screen instead of this
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

    /**
     * Result of LoginActivity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        fun updateUserLiveData() {
            data?.getParcelableExtra<User>(C.USER)?.let(viewModel.user::postValue)
        }

        when (requestCode) {
            1 -> { //After first launch
                initFragNavController()
                when (resultCode) {
                    RESULT_OK -> { //Logged in
                        updateUserLiveData()
                        fragNavController.initialize(INDEX_FOLLOWED)
                        navBar.selectedItemId = R.id.fragment_follow
                    }
                    RESULT_CANCELED, 2 -> { //Skipped first time or after token expiration
                        viewModel.user.value = null
                        fragNavController.initialize(INDEX_TOP)
                        navBar.selectedItemId = R.id.fragment_top
                    }
                }
                initNavBar()
            }
            2 -> { //Other
                //TODO reset fragments and restart messageview in init if it's running
                when (resultCode) {
                    RESULT_OK -> { //Logged in
                        updateUserLiveData()
                        navBar.selectedItemId = R.id.fragment_follow
                        fragNavController.switchTab(INDEX_FOLLOWED)
                    }
                    RESULT_CANCELED -> { //Logged out
                        viewModel.user.value = null
                        navBar.selectedItemId = R.id.fragment_top
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (fragNavController.size > 0) {
            fragNavController.onSaveInstanceState(outState)
        }
    }

    override fun startStream(stream: Stream) {
//        playerFragment?.play(stream)
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
        if (viewModel.isPlayerOpened && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) { //TODO change
            supportFragmentManager.beginTransaction().remove(playerFragment!!).commit()
            navBarContainer.visibility = View.VISIBLE
            return
        }
        if (!viewModel.isPlayerMaximized) {
            if (fragNavController.isRootFragment) {
                if (viewModel.user.value != null) {
                    if (fragNavController.currentStackIndex != INDEX_FOLLOWED) {
                        navBar.selectedItemId = R.id.fragment_follow
                    } else {
                        super.onBackPressed()
                    }
                } else {
                    if (fragNavController.currentStackIndex != INDEX_TOP) {
                        navBar.selectedItemId = R.id.fragment_top
                    } else {
                        super.onBackPressed()
                    }
                }
            } else {
                fragNavController.popFragment()
            }
        } else {
            playerFragment?.minimize()
        }
    }

    override fun onMaximized() {
        viewModel.onMaximize()
    }

    override fun onMinimized() {
        viewModel.onMinimize()
    }

    override fun onClosedToLeft() {
        closePlayer()
    }

    override fun onClosedToRight() {
        closePlayer()
    }

    override fun onMoved(horizontalDragOffset: Float, verticalDragOffset: Float) {
        navBarContainer.translationY = -verticalDragOffset * navBarContainer.height + navBarContainer.height
    }

    private fun startPlayer(fragment: BasePlayerFragment) {
//        if (playerFragment == null) {
        playerFragment = fragment
        supportFragmentManager.beginTransaction().replace(R.id.playerContainer, fragment, PLAYER_TAG).commit()
//        }
        viewModel.onPlayerStarted()
    }

    private fun closePlayer() {
        supportFragmentManager.beginTransaction().remove(playerFragment!!).commit()
        viewModel.onPlayerClosed()
    }

    private fun hideNavigationBar() {
        navBarContainer.translationY = navBarContainer.height.toFloat()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingFragmentInjector
    }
}
