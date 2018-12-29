package com.github.exact7.xtra.ui.downloads


import androidx.lifecycle.ViewModel
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.repository.OfflineRepository
import com.iheartradio.m3u8.Encoding
import com.iheartradio.m3u8.Format
import com.iheartradio.m3u8.PlaylistParser
import java.io.File
import javax.inject.Inject

class DownloadsViewModel @Inject internal constructor(
        private val repository: OfflineRepository) : ViewModel() {

    val list = repository.loadAllVideos()

    fun delete(video: OfflineVideo) {
        repository.deleteVideo(video)
        val file = File(video.url)
        if (video.vod) {
            val playlist = PlaylistParser(file.inputStream(), Format.EXT_M3U, Encoding.UTF_8).parse()
            for (track in playlist.mediaPlaylist.tracks) {
                File(track.uri).delete()
            }
            val directory = file.parentFile
            if (directory.list().size == 1) {
                file.delete()
                directory.delete()
            } else {
                file.delete()
            }
        } else {
            file.delete()
        }
    }
}
