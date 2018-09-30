package com.exact.twitch.di

import com.exact.twitch.ui.downloads.DownloadsFragment
import com.exact.twitch.ui.games.GamesFragment
import com.exact.twitch.ui.clips.ClipsFragment
import com.exact.twitch.ui.clips.FollowedClipsFragment
import com.exact.twitch.ui.player.clip.ClipPlayerFragment
import com.exact.twitch.ui.player.offline.OfflinePlayerFragment
import com.exact.twitch.ui.player.stream.StreamPlayerFragment
import com.exact.twitch.ui.player.video.VideoPlayerFragment
import com.exact.twitch.ui.streams.FollowedStreamsFragment
import com.exact.twitch.ui.streams.StreamsFragment
import com.exact.twitch.ui.videos.FollowedVideosFragment
import com.exact.twitch.ui.videos.GameVideosFragment
import com.exact.twitch.ui.videos.TopVideosFragment

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuildersModule {

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
