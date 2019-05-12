package com.github.exact7.xtra.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.exact7.xtra.ui.clips.common.ClipsViewModel
import com.github.exact7.xtra.ui.clips.followed.FollowedClipsViewModel
import com.github.exact7.xtra.ui.common.GenericViewModelFactory
import com.github.exact7.xtra.ui.download.ClipDownloadViewModel
import com.github.exact7.xtra.ui.download.VideoDownloadViewModel
import com.github.exact7.xtra.ui.downloads.DownloadsViewModel
import com.github.exact7.xtra.ui.games.GamesViewModel
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.ui.pagers.ChannelPagerViewModel
import com.github.exact7.xtra.ui.player.clip.ClipPlayerViewModel
import com.github.exact7.xtra.ui.player.offline.OfflinePlayerViewModel
import com.github.exact7.xtra.ui.player.stream.StreamPlayerViewModel
import com.github.exact7.xtra.ui.player.video.VideoPlayerViewModel
import com.github.exact7.xtra.ui.search.SearchViewModel
import com.github.exact7.xtra.ui.streams.common.StreamsViewModel
import com.github.exact7.xtra.ui.streams.followed.FollowedStreamsViewModel
import com.github.exact7.xtra.ui.videos.channel.ChannelVideosViewModel
import com.github.exact7.xtra.ui.videos.followed.FollowedVideosViewModel
import com.github.exact7.xtra.ui.videos.game.GameVideosViewModel
import com.github.exact7.xtra.ui.videos.top.TopVideosViewModel
import com.github.exact7.xtra.ui.view.chat.MessageClickedViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: GenericViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

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
    @ViewModelKey(FollowedStreamsViewModel::class)
    abstract fun bindFollowedStreamsViewModel(followedStreamsViewModel: FollowedStreamsViewModel): ViewModel

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
    @ViewModelKey(ChannelVideosViewModel::class)
    abstract fun bindChannelVideosViewModel(channelVideosViewModel: ChannelVideosViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FollowedVideosViewModel::class)
    abstract fun bindFollowedVideosViewModel(followedVideosViewModel: FollowedVideosViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GameVideosViewModel::class)
    abstract fun bindGameVideosViewModel(gameVideosViewModel: GameVideosViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TopVideosViewModel::class)
    abstract fun bindTopVideosViewModel(topVideosViewModel: TopVideosViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DownloadsViewModel::class)
    abstract fun bindDownloadsViewModel(downloadsViewModel: DownloadsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ClipsViewModel::class)
    abstract fun bindClipsViewModel(clipsViewModel: ClipsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FollowedClipsViewModel::class)
    abstract fun bindFollowedClipsViewModel(followedClipsViewModel: FollowedClipsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VideoDownloadViewModel::class)
    abstract fun bindVideoDownloadViewModel(videoDownloadViewModel: VideoDownloadViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ClipDownloadViewModel::class)
    abstract fun bindClipDownloadViewModel(clipDownloadViewModel: ClipDownloadViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(searchViewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChannelPagerViewModel::class)
    abstract fun bindChannelPagerViewModel(channelPagerViewModel: ChannelPagerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MessageClickedViewModel::class)
    abstract fun bindMessageClickedViewModel(messageClickedViewModel: MessageClickedViewModel): ViewModel
}
