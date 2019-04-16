package com.github.exact7.xtra.ui.main

import android.app.PictureInPictureParams
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Rational
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.view.ViewGroupCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.NotLoggedIn
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.ui.FollowValidationFragment
import com.github.exact7.xtra.ui.clips.BaseClipsFragment
import com.github.exact7.xtra.ui.common.OnChannelSelectedListener
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.download.HasDownloadDialog
import com.github.exact7.xtra.ui.downloads.DownloadsFragment
import com.github.exact7.xtra.ui.games.GamesFragment
import com.github.exact7.xtra.ui.menu.MenuFragment
import com.github.exact7.xtra.ui.pagers.ChannelPagerFragment
import com.github.exact7.xtra.ui.pagers.GameFragment
import com.github.exact7.xtra.ui.pagers.MediaPagerFragment
import com.github.exact7.xtra.ui.pagers.TopFragment
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import com.github.exact7.xtra.ui.player.clip.ClipPlayerFragment
import com.github.exact7.xtra.ui.player.offline.OfflinePlayerFragment
import com.github.exact7.xtra.ui.player.stream.StreamPlayerFragment
import com.github.exact7.xtra.ui.player.video.VideoPlayerFragment
import com.github.exact7.xtra.ui.search.SearchFragment
import com.github.exact7.xtra.ui.streams.BaseStreamsFragment
import com.github.exact7.xtra.ui.videos.BaseVideosFragment
import com.github.exact7.xtra.ui.view.SlidingLayout
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.NetworkUtils
import com.github.exact7.xtra.util.Prefs
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.visible
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.ncapdevi.fragnav.FragNavController
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


const val INDEX_GAMES = FragNavController.TAB1
const val INDEX_TOP = FragNavController.TAB2
const val INDEX_FOLLOWED = FragNavController.TAB3
const val INDEX_DOWNLOADS = FragNavController.TAB4
const val INDEX_MENU = FragNavController.TAB5

class MainActivity : AppCompatActivity(), GamesFragment.OnGameSelectedListener, BaseStreamsFragment.OnStreamSelectedListener, OnChannelSelectedListener, BaseClipsFragment.OnClipSelectedListener, BaseVideosFragment.OnVideoSelectedListener, HasSupportFragmentInjector, DownloadsFragment.OnVideoSelectedListener, Injectable, SlidingLayout.Listener {

    @Inject lateinit var dispatchingFragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: MainViewModel
    var playerFragment: BasePlayerFragment? = null
        private set
    private val fragNavController = FragNavController(supportFragmentManager, R.id.fragmentContainer)
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.setNetworkAvailable(NetworkUtils.isConnected(context))
        }
    }
    private val isSearchOpened
        get() = fragNavController.currentFrag is SearchFragment
    var isDarkTheme = true
        private set

    //Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs.get(this)
        if (!prefs.getBoolean(C.FIRST_LAUNCH, true)) {
            setTheme(if (prefs.getBoolean(C.THEME, true).also { isDarkTheme = it }) R.style.DarkTheme else R.style.LightTheme)
        } else {
            prefs.edit {
                putBoolean(C.FIRST_LAUNCH, false)
                putLong("firstLaunchDate", System.currentTimeMillis())
            }
            AlertDialog.Builder(this)
                    .setSingleChoiceItems(arrayOf(getString(R.string.dark), getString(R.string.light)), 0) { _, which -> isDarkTheme = which == 0 }
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        prefs.edit { putBoolean(C.THEME, isDarkTheme) }
                        if (!isDarkTheme) {
                            recreate()
                        }
                    }
                    .setTitle(getString(R.string.choose_theme))
                    .show()
        }

        setContentView(R.layout.activity_main)

        if (prefs.getBoolean("showRateAppDialog", true) && savedInstanceState == null) {
            val launchCount = prefs.getInt("launchCount", 0) + 1
            val dateOfFirstLaunch = prefs.getLong("firstLaunchDate", 0L)
            if (System.currentTimeMillis() >= dateOfFirstLaunch + 259200000L && launchCount >= 3) { //3 days passed and launched at least 3 times
                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.thank_you))
                        .setMessage(getString(R.string.rate_app_message))
                        .setPositiveButton(getString(R.string.rate)) { _, _ ->
                            prefs.edit { putBoolean("showRateAppDialog", false) }
                            try {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                            } catch (e: ActivityNotFoundException) {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                            }
                        }
                        .setNegativeButton(getString(R.string.remind_me_later), null)
                        .setNeutralButton(getString(R.string.no_thanks)) { _, _ -> prefs.edit { putBoolean("showRateAppDialog", false) } }
                        .show()
            } else {
                prefs.edit { putInt("launchCount", launchCount) }
            }
        }
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        val user = Prefs.getUser(this)
        viewModel.setUser(user)
        initNavigation()
        if (user !is NotLoggedIn) {
            fragNavController.initialize(INDEX_FOLLOWED, savedInstanceState)
            if (savedInstanceState == null) {
                navBar.selectedItemId = R.id.fragment_follow
            }
        } else {
            fragNavController.initialize(INDEX_TOP, savedInstanceState)
            if (savedInstanceState == null) {
                navBar.selectedItemId = R.id.fragment_top
            }
        }
        var flag = savedInstanceState == null && !NetworkUtils.isConnected(this)
        viewModel.isNetworkAvailable.observe(this, Observer {
            it.getContentIfNotHandled()?.let { online ->
                if (online) {
                    viewModel.validate(this)
                }
                if (flag) {
                    Toast.makeText(this, getString(if (online) R.string.connection_restored else R.string.no_connection), Toast.LENGTH_SHORT).show()
                } else {
                    flag = true
                }
            }
        })
        if (isSearchOpened) {
            hideNavigationBar()
        }
        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        if (savedInstanceState == null) {
            handleIntent(intent)
        }
        restorePlayerFragment()
    }

    override fun onResume() {
        super.onResume()
        restorePlayerFragment()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNavController.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * Result of LoginActivity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fun restartActivity() {
            finish()
            overridePendingTransition(0, 0)
            startActivity(Intent(this, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) })
            overridePendingTransition(0, 0)
        }
        when (requestCode) {
            1 -> { //Was not logged in
                when (resultCode) {//Logged in
                    RESULT_OK -> restartActivity()
                }
            }
            2 -> restartActivity() //Was logged in
        }
    }

    override fun onBackPressed() {
        if (!viewModel.isPlayerMaximized) {
            if (fragNavController.isRootFragment) {
                if (viewModel.user.value !is NotLoggedIn) {
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
            playerFragment?.let {
                if (it is StreamPlayerFragment) {
                    if (!it.hideEmotesMenu()) {
                        it.minimize()
                    }
                } else {
                    it.minimize()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && grantResults.indexOf(PackageManager.PERMISSION_DENIED) == -1) {
                    val fragment = fragNavController.currentFrag
                    if (fragment is HasDownloadDialog) {
                        fragment.showDownloadDialog()
                    } else if (fragment is MediaPagerFragment && fragment.currentFragment is HasDownloadDialog) {
                        (fragment.currentFragment as HasDownloadDialog).showDownloadDialog()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && viewModel.isPlayerMaximized) {
            enterPictureInPictureMode(PictureInPictureParams.Builder().build())
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        println(isInPictureInPictureMode)
        if (isInPictureInPictureMode) {
            viewModel.orientationBeforePictureInPicture = resources.configuration.orientation
            viewModel.shouldRecreate.value = false
        } else if (viewModel.orientationBeforePictureInPicture != newConfig?.orientation) {
            viewModel.shouldRecreate.value = true
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (viewModel.shouldRecreate.value == true) {
                recreate()
            } else if (!isInPictureInPictureMode) {
                viewModel.shouldRecreate.value = true
            }
        } else {
            recreate()
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            when (it.getIntExtra("code", -1)) {
                0 -> navBar.selectedItemId = R.id.fragment_downloads
                1 -> startOfflineVideo(it.getParcelableExtra(C.VIDEO))
            }
        }
    }

    //Navigation listeners

    override fun openGame(game: Game) {
        fragNavController.pushFragment(GameFragment.newInstance(game))
    }

    override fun startStream(stream: Stream) {
//        playerFragment?.play(stream)
        startPlayer(StreamPlayerFragment(), C.STREAM, stream)
    }

    override fun startVideo(video: Video) {
        startPlayer(VideoPlayerFragment(), C.VIDEO, video)
    }

    override fun startClip(clip: Clip) {
        startPlayer(ClipPlayerFragment(), C.CLIP, clip)
    }

    override fun startOfflineVideo(video: OfflineVideo) {
        startPlayer(OfflinePlayerFragment(), C.VIDEO, video)
    }

    override fun viewChannel(channel: Channel) {
        fragNavController.pushFragment(ChannelPagerFragment.newInstance(channel))
    }

//SlidingLayout.Listener

    override fun onMaximize() {
        viewModel.onMaximize()
    }

    override fun onMinimize() {
        viewModel.onMinimize()
    }

    override fun onClose() {
        closePlayer()
    }

//Player methods

    private fun startPlayer(fragment: BasePlayerFragment, argKey: String, argValue: Parcelable) {
//        if (playerFragment == null) {
        playerFragment = fragment.apply { arguments = bundleOf(argKey to argValue) }
        supportFragmentManager.beginTransaction().replace(R.id.playerContainer, fragment).commit()
        viewModel.onPlayerStarted()
    }

    private fun closePlayer() {
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .remove(supportFragmentManager.findFragmentById(R.id.playerContainer)!!)
                .commit()
        playerFragment = null
        viewModel.onPlayerClosed()
    }

    private fun restorePlayerFragment() {
        if (viewModel.isPlayerOpened && playerFragment == null) {
            playerFragment = supportFragmentManager.findFragmentById(R.id.playerContainer) as BasePlayerFragment?
        }
    }

    private fun hideNavigationBar() {
        navBarContainer.gone()
    }

    private fun showNavigationBar() {
        navBarContainer.visible()
    }

    fun popFragment() {
        fragNavController.popFragment()
    }

    fun openSearch() {
        fragNavController.pushFragment(SearchFragment())
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingFragmentInjector
    }

    private fun initNavigation() {
        fragNavController.apply {
            rootFragments = listOf(GamesFragment(), TopFragment(), FollowValidationFragment(), DownloadsFragment(), MenuFragment())
            fragmentHideStrategy = FragNavController.DETACH_ON_NAVIGATE_HIDE_ON_SWITCH
            transactionListener = object : FragNavController.TransactionListener {
                override fun onFragmentTransaction(fragment: Fragment?, transactionType: FragNavController.TransactionType) {
                    if (isSearchOpened) {
                        hideNavigationBar()
                    } else {
                        showNavigationBar()
                    }
                }

                override fun onTabTransaction(fragment: Fragment?, index: Int) {
                }
            }
        }
        navBar.apply {
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
                true
            }

            setOnNavigationItemReselectedListener {
                val currentFragment = fragNavController.currentFrag
                when (it.itemId) {
                    R.id.fragment_games -> {
                        when (currentFragment) {
                            is GamesFragment -> currentFragment.scrollToTop()
                            else -> fragNavController.clearStack()
                        }
                    }
                    else -> if (fragNavController.isRootFragment) {
                        if (currentFragment is Scrollable) {
                            currentFragment.scrollToTop()
                        }
                    } else {
                        fragNavController.clearStack()
                    }
                }
            }
        }
    }
}
