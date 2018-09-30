package com.exact.twitch.service

import android.app.Notification
import android.content.Intent

import com.exact.twitch.R
import com.exact.twitch.db.AppDatabase
import com.exact.twitch.model.OfflineVideo
import com.exact.twitch.util.DownloadUtils
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationUtil
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util

import androidx.room.Room

class MediaDownloadService : DownloadService(FOREGROUND_NOTIFICATION_ID, DownloadService.DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL, CHANNEL_ID, R.string.all_time) {
    private var video: OfflineVideo? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (video ==
                null) { //TODO hack, fix later
            video = intent.extras!!.getParcelable("video")
            if (database == null) {
                database = Room.databaseBuilder(this, AppDatabase::class.java, "database").build()
            }
            //            DaggerTwitchComponent.builder().application(getApplication()).build().inject(this);
            println(database)
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
        return DownloadNotificationUtil.buildProgressNotification(this, R.drawable.exo_controls_play, CHANNEL_ID, null, getString(R.string.downloading, video!!.name), taskStates)
    }

    override fun onTaskStateChanged(taskState: DownloadManager.TaskState?) {
        if (taskState!!.action.isRemoveAction) {
            return
        }
        var notification: Notification? = null
        if (taskState.state == DownloadManager.TaskState.STATE_COMPLETED) {
            notification = DownloadNotificationUtil.buildDownloadCompletedNotification(
                    this,
                    R.drawable.exo_controls_play,
                    CHANNEL_ID, null,
                    getString(R.string.downloaded, video!!.name))
            Thread { database!!.videos().insert(video!!) }.start()
        } else if (taskState.state == DownloadManager.TaskState.STATE_FAILED) {
            notification = DownloadNotificationUtil.buildDownloadFailedNotification(
                    this,
                    R.drawable.exo_controls_play,
                    CHANNEL_ID, null,
                    getString(R.string.download_error))
        }
        val notificationId = FOREGROUND_NOTIFICATION_ID + 1 + taskState.taskId
        NotificationUtil.setNotification(this, notificationId, notification)
    }

    companion object {

        //    @Inject
        private var database: AppDatabase? = null

        private val CHANNEL_ID = "download_channel"
        private val JOB_ID = 1
        private val FOREGROUND_NOTIFICATION_ID = 1
    }
}//TODO change
