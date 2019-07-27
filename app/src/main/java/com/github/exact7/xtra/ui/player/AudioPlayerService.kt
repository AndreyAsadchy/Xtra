package com.github.exact7.xtra.ui.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import androidx.core.net.toUri
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.main.MainActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class AudioPlayerService : Service() {

    private lateinit var player: ExoPlayer
    private lateinit var playerNotificationManager: PlayerNotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val channelId = getString(R.string.notification_playback_channel_id)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (manager.getNotificationChannel(channelId) == null) {
                        val channel = NotificationChannel(channelId, getString(R.string.notification_playback_channel_title), NotificationManager.IMPORTANCE_LOW)
                        channel.setSound(null, null)
                        manager.createNotificationChannel(channel)
                    }
                }
                val mediaSource = HlsMediaSource.Factory(DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)))).createMediaSource(intent.getStringExtra(KEY_PLAYLIST_URL).toUri())
                player = ExoPlayerFactory.newSimpleInstance(this)
                player.prepare(mediaSource)
                player.playWhenReady = true
                playerNotificationManager = PlayerNotificationManager(
                        this,
                        channelId,
                        System.currentTimeMillis().toInt(),
                        DescriptionAdapter(intent.getStringExtra(KEY_CHANNEL_NAME), intent.getStringExtra(KEY_TITLE)),
                        object : PlayerNotificationManager.NotificationListener {
                            override fun onNotificationPosted(notificationId: Int, notification: Notification?, ongoing: Boolean) {
                                startForeground(notificationId, notification)
                            }

                            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                                if (dismissedByUser) {
                                    stopSelf()
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        stopForeground(STOP_FOREGROUND_REMOVE)
                                    } else {
                                        stopForeground(true)
                                    }
                                }
                            }
                        }
                ).apply {
                    setUseNavigationActions(false)
                    setUsePlayPauseActions(intent.getBooleanExtra(KEY_USE_PLAY_PAUSE, false))
                    setUseStopAction(true)
                    setFastForwardIncrementMs(0)
                    setRewindIncrementMs(0)
                }
            }
            ACTION_SHOW -> playerNotificationManager.setPlayer(player)
            ACTION_HIDE -> playerNotificationManager.setPlayer(null)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        playerNotificationManager.setPlayer(null)
        player.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private inner class DescriptionAdapter(
            private val text: String,
            private val title: String) : PlayerNotificationManager.MediaDescriptionAdapter {

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val clickIntent = Intent(this@AudioPlayerService, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.KEY_CODE, MainActivity.INTENT_OPEN_PLAYER)
            }
            return PendingIntent.getActivity(this@AudioPlayerService, REQUEST_CODE_RESUME, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        override fun getCurrentContentText(player: Player): String? = text

        override fun getCurrentContentTitle(player: Player): String? = title

        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? =
                null
    }

    companion object {
        const val KEY_PLAYLIST_URL = "playlistUrl"
        const val KEY_CHANNEL_NAME = "channelName"
        const val KEY_TITLE = "title"
        const val KEY_USE_PLAY_PAUSE = "playPause"

        const val ACTION_START = "com.github.exact7.action.AUDIO_START"
        const val ACTION_SHOW = "com.github.exact7.action.AUDIO_SHOW"
        const val ACTION_HIDE = "com.github.exact7.action.AUDIO_HIDE"

        const val REQUEST_CODE_RESUME = 2
    }
}