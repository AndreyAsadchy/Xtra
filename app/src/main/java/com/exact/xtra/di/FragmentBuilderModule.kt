package com.exact.xtra.di

import com.exact.xtra.ui.clips.ClipsFragment
import com.exact.xtra.ui.clips.FollowedClipsFragment
import com.exact.xtra.ui.downloads.DownloadsFragment
import com.exact.xtra.ui.games.GamesFragment
import com.exact.xtra.ui.player.clip.ClipPlayerFragment
import com.exact.xtra.ui.player.offline.OfflinePlayerFragment
import com.exact.xtra.ui.player.stream.StreamPlayerFragment
import com.exact.xtra.ui.player.video.VideoPlayerFragment
import com.exact.xtra.ui.streams.FollowedStreamsFragment
import com.exact.xtra.ui.streams.StreamsFragment
import com.exact.xtra.ui.videos.FollowedVideosFragment
import com.exact.xtra.ui.videos.GameVideosFragment
import com.exact.xtra.ui.videos.TopVideosFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeGamesFragment(): GamesFragment

    @ContributesAndroidInjector
    abstract fun contributeFollowedStreamsFragment(): FollowedStreamsFragment

    @ContributesAndroidInjector
    abstract fun contributeGameStreamsFragment(): StreamsFragment

    @ContributesAndroidInjector
    abstract fun contributeStreamPlayerFragment(): StreamPlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeVideoPlayerFragment(): VideoPlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeClipPlayerFragment(): ClipPlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeOfflinePlayerFragment(): OfflinePlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeClipsFragment(): ClipsFragment

    @ContributesAndroidInjector
    abstract fun contributeFollowedClipsFragment(): FollowedClipsFragment

    @ContributesAndroidInjector
    abstract fun contributeVideosFragment(): GameVideosFragment

    @ContributesAndroidInjector
    abstract fun contributeFollowedVideosFragment(): FollowedVideosFragment

    @ContributesAndroidInjector
    abstract fun contributeTopVideosFragment(): TopVideosFragment

    @ContributesAndroidInjector
    abstract fun contributeDownloadsFragment(): DownloadsFragment
}
