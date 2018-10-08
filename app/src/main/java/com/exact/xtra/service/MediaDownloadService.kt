package com.exact.xtra.service

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import com.exact.xtra.R
import com.exact.xtra.db.VideosDao
import com.exact.xtra.model.OfflineVideo
import com.exact.xtra.util.DownloadUtils
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadManager.TaskState.STATE_COMPLETED
import com.google.android.exoplayer2.offline.DownloadManager.TaskState.STATE_FAILED
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationUtil
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util
import dagger.android.AndroidInjection
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class MediaDownloadService : DownloadService(
        FOREGROUND_NOTIFICATION_ID,
        DownloadService.DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
        CHANNEL_ID,
        R.string.all_time) {

    private companion object {
        private const val CHANNEL_ID = "download_channel"
        private const val JOB_ID = 1
        private const val FOREGROUND_NOTIFICATION_ID = 1
    }

    @Inject
    lateinit var dao: VideosDao
    private lateinit var video: OfflineVideo

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (!this::video.isInitialized) {
            video = intent.extras!!.getParcelable("video")!!
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun getDownloadManager(): DownloadManager {
        return DownloadUtils.getDownloadManager(this)
    }

    override fun getScheduler(): Scheduler? {
        return if (Util.SDK_INT >= 21) PlatformScheduler(this, JOB_ID) else null
    }

    override fun getForegroundNotification(taskStates: Array<DownloadManager.TaskState>): Notification {
        return DownloadNotificationUtil.buildProgressNotification(this, R.drawable.exo_controls_play, CHANNEL_ID, null, getString(R.string.downloading, video.name), taskStates)
    }

    @SuppressLint("SwitchIntDef")
    override fun onTaskStateChanged(taskState: DownloadManager.TaskState?) {
        if (taskState!!.action.isRemoveAction) {
            return
        }
        val notification: Notification? = when (taskState.state) {
            STATE_COMPLETED -> {
                launch { dao.insert(video) }
                DownloadNotificationUtil.buildDownloadCompletedNotification(
                        this,
                        R.drawable.exo_controls_play,
                        CHANNEL_ID,
                        null,
                        getString(R.string.downloaded, video.name))

            } STATE_FAILED -> {
                DownloadNotificationUtil.buildDownloadFailedNotification(
                        this,
                        R.drawable.exo_controls_play,
                        CHANNEL_ID,
                        null,
                        getString(R.string.download_error))
            }
            else -> null
        }
        val notificationId = FOREGROUND_NOTIFICATION_ID + 1 + taskState.taskId
        NotificationUtil.setNotification(this, notificationId, notification)
    }
}
