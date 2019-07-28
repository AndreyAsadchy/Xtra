package com.github.exact7.xtra.ui.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.exact7.xtra.GlideApp
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

    private var player: ExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null

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
                player = ExoPlayerFactory.newSimpleInstance(this).apply {
                    prepare(mediaSource)
                    playWhenReady = true
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
                    setSmallIcon(R.drawable.baseline_audiotrack_black_24)
                }
            }
            ACTION_SHOW -> playerNotificationManager?.setPlayer(player)
            ACTION_HIDE -> playerNotificationManager?.setPlayer(null)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        playerNotificationManager?.setPlayer(null)
        player?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private class CustomPlayerNotificationManager(context: Context, channelId: String, notificationId: Int, mediaDescriptionAdapter: MediaDescriptionAdapter, notificationListener: NotificationListener) : PlayerNotificationManager(context, channelId, notificationId, mediaDescriptionAdapter, notificationListener) {
        override fun createNotification(player: Player, builder: NotificationCompat.Builder?, ongoing: Boolean, largeIcon: Bitmap?): NotificationCompat.Builder? {
            return super.createNotification(player, builder, ongoing, largeIcon)?.apply { mActions[0].icon = R.drawable.baseline_close_black_24 }
        }

        override fun getActionIndicesForCompactView(actionNames: List<String>, player: Player): IntArray {
            return intArrayOf(0)
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

        override fun getCurrentContentTitle(player: Player): String? = title

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

        const val ACTION_START = "com.github.exact7.action.AUDIO_START"
        const val ACTION_SHOW = "com.github.exact7.action.AUDIO_SHOW"
        const val ACTION_HIDE = "com.github.exact7.action.AUDIO_HIDE"

        const val REQUEST_CODE_RESUME = 2
    }
}