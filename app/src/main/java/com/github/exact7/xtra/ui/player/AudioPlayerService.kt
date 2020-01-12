package com.github.exact7.xtra.ui.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.VideoPosition
import com.github.exact7.xtra.player.lowlatency.DefaultHlsPlaylistParserFactory
import com.github.exact7.xtra.player.lowlatency.DefaultHlsPlaylistTracker
import com.github.exact7.xtra.player.lowlatency.HlsMediaSource
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.ui.main.MainActivity
import com.google.android.exoplayer2.DefaultControlDispatcher
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy
import com.google.android.exoplayer2.util.Util
import dagger.android.AndroidInjection
import javax.inject.Inject

class AudioPlayerService : Service() {

    @Inject
    lateinit var playerRepository: PlayerRepository

    @Inject
    lateinit var offlineRepository: OfflineRepository

    private lateinit var player: ExoPlayer
    private lateinit var mediaSource: MediaSource
    private lateinit var playerNotificationManager: PlayerNotificationManager

    private var restorePosition = false
    private var type = -1
    private var videoId: Number? = null

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        player = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector().apply {
            parameters = buildUponParameters().setRendererDisabled(0, true).build()
        })
    }

    override fun onDestroy() {
        when (type) {
            TYPE_VIDEO -> {
                position = player.currentPosition
                playerRepository.saveVideoPosition(VideoPosition(videoId as Long, position))
            }
            TYPE_OFFLINE -> {
                position = player.currentPosition
                offlineRepository.updateVideoPosition(videoId as Int, position)
            }
        }
        player.release()
        connection = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        val channelId = getString(R.string.notification_playback_channel_id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(channelId, getString(R.string.notification_playback_channel_title), NotificationManager.IMPORTANCE_LOW)
                channel.setSound(null, null)
                manager.createNotificationChannel(channel)
            }
        }
        mediaSource = HlsMediaSource.Factory(DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name))))
                .setAllowChunklessPreparation(true)
                .setPlaylistParserFactory(DefaultHlsPlaylistParserFactory())
                .setPlaylistTrackerFactory(DefaultHlsPlaylistTracker.FACTORY)
                .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(6))
                .createMediaSource(intent.getStringExtra(KEY_PLAYLIST_URL).toUri())
        var currentPlaybackPosition = intent.getLongExtra(KEY_CURRENT_POSITION, 0L)
        val usePlayPause = intent.getBooleanExtra(KEY_USE_PLAY_PAUSE, false)
        type = intent.getIntExtra(KEY_TYPE, -1)
        when (type) {
            TYPE_VIDEO -> videoId = intent.getLongExtra(KEY_VIDEO_ID, -1L)
            TYPE_OFFLINE -> videoId = intent.getIntExtra(KEY_VIDEO_ID, -1)
        }
        player.apply {
            addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if (restorePosition && playbackState == Player.STATE_READY) {
                        restorePosition = false
                        player.seekTo(currentPlaybackPosition)
                    }
                }

                override fun onPlayerError(error: ExoPlaybackException) {
                    if (usePlayPause && !restorePosition) { //if it's a vod and didn't already save position
                        currentPlaybackPosition = player.currentPosition
                        restorePosition = true
                    }
                    prepare(mediaSource)
                }
            })
            prepare(mediaSource)
            playWhenReady = true
            if (currentPlaybackPosition > 0) {
                player.seekTo(currentPlaybackPosition)
            }
        }
        playerNotificationManager = CustomPlayerNotificationManager(
                this,
                channelId,
                System.currentTimeMillis().toInt(),
                DescriptionAdapter(intent.getStringExtra(KEY_TITLE), intent.getStringExtra(KEY_CHANNEL_NAME), intent.getStringExtra(KEY_IMAGE_URL)),
                object : PlayerNotificationManager.NotificationListener {
                    override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                        startForeground(notificationId, notification)
                    }

                    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                        } else {
                            stopForeground(true)
                        }
                    }
                },
                !usePlayPause
        ).apply {
            setUseNavigationActions(false)
            setUsePlayPauseActions(usePlayPause)
            setUseStopAction(true)
            setFastForwardIncrementMs(0)
            setRewindIncrementMs(0)
            setSmallIcon(R.drawable.baseline_audiotrack_black_24)
            setControlDispatcher(object : DefaultControlDispatcher() {
                override fun dispatchStop(player: Player, reset: Boolean): Boolean {
                    connection.let {
                        //TODO TEMP FIX, REWORK SERVICE BINDING NORMALLY
                        if (it != null) {
                            applicationContext.unbindService(it)
                        } else {
                            playerNotificationManager.setPlayer(null)
                        }
                    }
                    return true
                }
            })
        }
        return AudioBinder()
    }

    inner class AudioBinder : Binder() {

        val player: ExoPlayer
            get() = this@AudioPlayerService.player

        fun showNotification() {
            playerNotificationManager.setPlayer(player)
        }

        fun hideNotification() {
            playerNotificationManager.setPlayer(null)
        }

        fun restartPlayer() {
            player.stop()
            player.prepare(mediaSource)
        }
    }

    private class CustomPlayerNotificationManager(context: Context, channelId: String, notificationId: Int, mediaDescriptionAdapter: MediaDescriptionAdapter, notificationListener: NotificationListener, private val isLive: Boolean) : PlayerNotificationManager(context, channelId, notificationId, mediaDescriptionAdapter, notificationListener) {
        override fun createNotification(player: Player, builder: NotificationCompat.Builder?, ongoing: Boolean, largeIcon: Bitmap?): NotificationCompat.Builder? {
            return super.createNotification(player, builder, ongoing, largeIcon)?.apply { mActions[if (isLive) 0 else 1].icon = R.drawable.baseline_close_black_36 }
        }

        override fun getActionIndicesForCompactView(actionNames: List<String>, player: Player): IntArray {
            return if (isLive) intArrayOf(0) else intArrayOf(0, 1)
        }
    }

    private inner class DescriptionAdapter(
            private val text: String,
            private val title: String,
            private val imageUrl: String) : PlayerNotificationManager.MediaDescriptionAdapter {

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val clickIntent = Intent(this@AudioPlayerService, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.KEY_CODE, MainActivity.INTENT_OPEN_PLAYER)
            }
            return PendingIntent.getActivity(this@AudioPlayerService, REQUEST_CODE_RESUME, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        override fun getCurrentContentText(player: Player): String? = text

        override fun getCurrentContentTitle(player: Player): String = title

        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
            GlideApp.with(this@AudioPlayerService)
                    .asBitmap()
                    .load(imageUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            callback.onBitmap(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            return null
        }
    }

    companion object {
        const val KEY_PLAYLIST_URL = "playlistUrl"
        const val KEY_CHANNEL_NAME = "channelName"
        const val KEY_TITLE = "title"
        const val KEY_IMAGE_URL = "imageUrl"
        const val KEY_USE_PLAY_PAUSE = "playPause"
        const val KEY_CURRENT_POSITION = "currentPosition"
        const val KEY_TYPE = "type"
        const val KEY_VIDEO_ID = "videoId"

        const val REQUEST_CODE_RESUME = 2

        const val TYPE_STREAM = 0
        const val TYPE_VIDEO = 1
        const val TYPE_OFFLINE = 2

        var connection: ServiceConnection? = null
        var position = 0L
    }
}