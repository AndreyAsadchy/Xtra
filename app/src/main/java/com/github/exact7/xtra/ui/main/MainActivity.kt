package com.github.exact7.xtra.ui.main

import android.app.PictureInPictureParams
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.BuildConfig
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.NotLoggedIn
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.ui.channel.ChannelPagerFragment
import com.github.exact7.xtra.ui.chat.ChatFragment
import com.github.exact7.xtra.ui.clips.BaseClipsFragment
import com.github.exact7.xtra.ui.common.OnChannelSelectedListener
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.common.pagers.MediaPagerFragment
import com.github.exact7.xtra.ui.download.HasDownloadDialog
import com.github.exact7.xtra.ui.downloads.DownloadsFragment
import com.github.exact7.xtra.ui.follow.FollowValidationFragment
import com.github.exact7.xtra.ui.games.GameFragment
import com.github.exact7.xtra.ui.games.GamesFragment
import com.github.exact7.xtra.ui.menu.MenuFragment
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import com.github.exact7.xtra.ui.player.clip.ClipPlayerFragment
import com.github.exact7.xtra.ui.player.offline.OfflinePlayerFragment
import com.github.exact7.xtra.ui.player.stream.StreamPlayerFragment
import com.github.exact7.xtra.ui.player.video.VideoPlayerFragment
import com.github.exact7.xtra.ui.search.SearchFragment
import com.github.exact7.xtra.ui.streams.BaseStreamsFragment
import com.github.exact7.xtra.ui.top.TopFragment
import com.github.exact7.xtra.ui.videos.BaseVideosFragment
import com.github.exact7.xtra.ui.view.SlidingLayout
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.applyTheme
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.isNetworkAvailable
import com.github.exact7.xtra.util.prefs
import com.github.exact7.xtra.util.visible
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

    companion object {
        const val KEY_CODE = "code"
        const val KEY_VIDEO = "video"

        const val INTENT_OPEN_DOWNLOADS_TAB = 0
        const val INTENT_OPEN_DOWNLOADED_VIDEO = 1
        const val INTENT_OPEN_PLAYER = 2
    }

    @Inject
    lateinit var dispatchingFragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: MainViewModel
    var playerFragment: BasePlayerFragment? = null
        private set
    private val fragNavController = FragNavController(supportFragmentManager, R.id.fragmentContainer)
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.setNetworkAvailable(isNetworkAvailable)
        }
    }
    private val isSearchOpened
        get() = fragNavController.currentFrag is SearchFragment
    var currentTheme = "0"
        private set
    private lateinit var prefs: SharedPreferences

    //Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = prefs()
        val notFirstLaunch = !prefs.getBoolean(C.FIRST_LAUNCH, true)
        if (notFirstLaunch) {
            currentTheme = applyTheme()
        } else {
            prefs.edit {
                putBoolean(C.FIRST_LAUNCH, false)
                putLong("firstLaunchDate", System.currentTimeMillis())
            }
            AlertDialog.Builder(this)
                    .setSingleChoiceItems(arrayOf(getString(R.string.dark), getString(R.string.amoled), getString(R.string.light)), 0) { _, which -> currentTheme = which.toString() }
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        prefs.edit { putString(C.THEME, currentTheme) }
                        if (currentTheme != "0") {
                            recreate()
                        }
                    }
                    .setTitle(getString(R.string.choose_theme))
                    .show()
        }

        setContentView(R.layout.activity_main)

        val notInitialized = savedInstanceState == null
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        val user = User.get(this)
        viewModel.setUser(user)
        initNavigation()
        if (user !is NotLoggedIn) {
            fragNavController.initialize(INDEX_FOLLOWED, savedInstanceState)
            if (notInitialized) {
                navBar.selectedItemId = R.id.fragment_follow
            }
        } else {
            fragNavController.initialize(INDEX_TOP, savedInstanceState)
            if (notInitialized) {
                navBar.selectedItemId = R.id.fragment_top
            }
        }
        var flag = notInitialized && !isNetworkAvailable
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
        if (notInitialized) {
//            ProviderInstaller.installIfNeededAsync(this, object : ProviderInstaller.ProviderInstallListener {
//                override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
//                    GoogleApiAvailability.getInstance().apply {
//                        if (isUserResolvableError(errorCode)) {
//                             Prompt the user to install/update/enable Google Play services.
//                            showErrorDialogFragment(this@MainActivity, errorCode, 0)
//                        } else {
//                            Toast.makeText(this@MainActivity, getString(R.string.play_services_not_available), Toast.LENGTH_LONG).show()
//                        }
//                    }
//                }
//                override fun onProviderInstalled() {}
//            })
            handleIntent(intent)
            if (prefs.getString("lastUpdateVersion", null) != BuildConfig.VERSION_NAME) {
                prefs.edit { putString("lastUpdateVersion", BuildConfig.VERSION_NAME) }
                if (prefs.getBoolean(C.SHOW_CHANGELOGS, true) && notFirstLaunch) {
                    NewUpdateChangelogDialog().show(supportFragmentManager, null)
                }
            } else {
                if (prefs.getBoolean("showRateAppDialog", true)) {
                    val launchCount = prefs.getInt("launchCount", 0) + 1
                    val dateOfFirstLaunch = prefs.getLong("firstLaunchDate", 0L)
                    if (System.currentTimeMillis() < dateOfFirstLaunch + 345600000L || launchCount < 7) {
                        prefs.edit { putInt("launchCount", launchCount) }
                    } else { //4 days passed and launched at least 8 times
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
                    }
                }
            }
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
//        val nMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        nMgr.cancelAll()
        unregisterReceiver(networkReceiver)
        super.onDestroy()
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
                val currentFrag = fragNavController.currentFrag
                if (currentFrag !is ChannelPagerFragment || (currentFrag.currentFragment.let { it !is ChatFragment || !it.hideEmotesMenu() })) {
                    fragNavController.popFragment()
                }
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
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && viewModel.isPlayerMaximized && playerFragment!!.shouldEnterPictureInPicture && prefs.getBoolean(C.PICTURE_IN_PICTURE, true)) {
                enterPictureInPictureMode(PictureInPictureParams.Builder().build())
            }
        } catch (e: IllegalStateException) {
            //device doesn't support PIP
        } catch (e: Exception) { //TODO playerFragment null, wtf?
            Crashlytics.logException(e)
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            viewModel.orientationBeforePictureInPicture = resources.configuration.orientation
            viewModel.wasInPictureInPicture = true
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isInPictureInPictureMode) {
                if (!viewModel.wasInPictureInPicture) {
                    recreate()
                } else {
                    viewModel.wasInPictureInPicture = false
                    if (viewModel.orientationBeforePictureInPicture != newConfig.orientation) {
                        recreate()
                    }
                }
            }
        } else {
            recreate()
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent?.also {
            when (it.getIntExtra(KEY_CODE, -1)) {
                INTENT_OPEN_DOWNLOADS_TAB -> navBar.selectedItemId = R.id.fragment_downloads
                INTENT_OPEN_DOWNLOADED_VIDEO -> startOfflineVideo(it.getParcelableExtra(KEY_VIDEO))
                INTENT_OPEN_PLAYER -> playerFragment?.maximize() //TODO if was closed need to reopen
            }
        }
    }

    //Navigation listeners

    override fun openGame(game: Game) {
        fragNavController.pushFragment(GameFragment.newInstance(game))
    }

    override fun startStream(stream: Stream) {
//        playerFragment?.play(stream)
        startPlayer(StreamPlayerFragment.newInstance(stream))
    }

    override fun startVideo(video: Video, offset: Double?) {
        startPlayer(VideoPlayerFragment.newInstance(video, offset))
    }

    override fun startClip(clip: Clip) {
        startPlayer(ClipPlayerFragment.newInstance(clip))
    }

    override fun startOfflineVideo(video: OfflineVideo) {
        startPlayer(OfflinePlayerFragment.newInstance(video))
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

    private fun startPlayer(fragment: BasePlayerFragment) {
//        if (playerFragment == null) {
        playerFragment = fragment
        supportFragmentManager.beginTransaction()
                .replace(R.id.playerContainer, fragment).commit()
        viewModel.onPlayerStarted()
    }

    fun closePlayer() {
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
