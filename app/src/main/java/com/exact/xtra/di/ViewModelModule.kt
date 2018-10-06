package com.exact.xtra.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.exact.xtra.ui.clips.ClipsViewModel
import com.exact.xtra.ui.common.GenericViewModelFactory
import com.exact.xtra.ui.downloads.DownloadsViewModel
import com.exact.xtra.ui.games.GamesViewModel
import com.exact.xtra.ui.main.MainActivityViewModel
import com.exact.xtra.ui.player.clip.ClipPlayerViewModel
import com.exact.xtra.ui.player.offline.OfflinePlayerViewModel
import com.exact.xtra.ui.player.stream.StreamPlayerViewModel
import com.exact.xtra.ui.player.video.VideoPlayerViewModel
import com.exact.xtra.ui.streams.StreamsViewModel
import com.exact.xtra.ui.videos.VideosViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindMainActivityViewModel(mainActivityViewModel: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GamesViewModel::class)
    abstract fun bindGamesViewModel(gamesViewModel: GamesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StreamsViewModel::class)
    abstract fun bindStreamsViewModel(streamListViewModel: StreamsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StreamPlayerViewModel::class)
    abstract fun bindStreamPlayerViewModel(streamPlayerViewModel: StreamPlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VideoPlayerViewModel::class)
    abstract fun bindVideoPlayerViewModel(videoPlayerViewModel: VideoPlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ClipPlayerViewModel::class)
    abstract fun bindClipPlayerViewModel(clipPlayerViewModel: ClipPlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OfflinePlayerViewModel::class)
    abstract fun bindOfflinePlayerViewModel(offlinePlayerViewModel: OfflinePlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VideosViewModel::class)
    abstract fun bindVideosViewModel(videosViewModel: VideosViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DownloadsViewModel::class)
    abstract fun bindDownloadsViewModel(downloadsViewModel: DownloadsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ClipsViewModel::class)
    abstract fun bindClipsViewModel(clipsViewModel: ClipsViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: GenericViewModelFactory): ViewModelProvider.Factory
}
