package com.github.exact7.xtra.ui.downloads


import android.app.Application
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.util.FetchProvider
import com.iheartradio.m3u8.Encoding
import com.iheartradio.m3u8.Format
import com.iheartradio.m3u8.PlaylistParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileFilter
import javax.inject.Inject

class DownloadsViewModel @Inject internal constructor(
        application: Application,
        private val repository: OfflineRepository,
        private val fetchProvider: FetchProvider) : AndroidViewModel(application) {

    val list = repository.loadAllVideos()

    fun delete(video: OfflineVideo) {
        repository.deleteVideo(video)
        GlobalScope.launch {
            if (video.status == OfflineVideo.STATUS_DOWNLOADED) {
                val playlistFile = File(video.url)
                if (!playlistFile.exists()) {
                    return@launch
                }
                if (video.vod) {
                    val directory = playlistFile.parentFile
                    val playlists = directory.listFiles(FileFilter { it.extension == "m3u8" && it != playlistFile })
                    if (playlists.isEmpty()) {
                        val context = getApplication<Application>()
                        fun deleteImage(url: String) {
                            runBlocking(Dispatchers.Main) {
                                GlideApp.with(context)
                                        .load(url)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(object : CustomTarget<Drawable>() {
                                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {}
                                            override fun onLoadCleared(placeholder: Drawable?) {}
                                        })
                            }
                        }
                        deleteImage(video.channelLogo)
                        deleteImage(video.thumbnail)
                        directory.deleteRecursively()
                    } else {
                        val playlist = PlaylistParser(playlistFile.inputStream(), Format.EXT_M3U, Encoding.UTF_8).parse()
                        val tracksToDelete = playlist.mediaPlaylist.tracks.toMutableSet()
                        playlists.forEach {
                            val p = PlaylistParser(it.inputStream(), Format.EXT_M3U, Encoding.UTF_8).parse()
                            tracksToDelete.removeAll(p.mediaPlaylist.tracks)
                        }
                        playlistFile.delete()
                        tracksToDelete.forEach { File(it.uri).delete() }
                    }
                } else {
                    playlistFile.delete()
                }
            } else {
                fetchProvider.get(video.id).deleteGroup(video.id)
            }
        }
    }
}