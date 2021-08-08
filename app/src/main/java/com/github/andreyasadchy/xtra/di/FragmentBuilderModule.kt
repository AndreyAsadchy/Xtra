package com.github.andreyasadchy.xtra.di

import com.github.andreyasadchy.xtra.ui.channel.ChannelPagerFragment
import com.github.andreyasadchy.xtra.ui.channel.info.ChannelInfoFragment
import com.github.andreyasadchy.xtra.ui.chat.ChatFragment
import com.github.andreyasadchy.xtra.ui.clips.common.ClipsFragment
import com.github.andreyasadchy.xtra.ui.clips.followed.FollowedClipsFragment
import com.github.andreyasadchy.xtra.ui.download.ClipDownloadDialog
import com.github.andreyasadchy.xtra.ui.download.VideoDownloadDialog
import com.github.andreyasadchy.xtra.ui.downloads.DownloadsFragment
import com.github.andreyasadchy.xtra.ui.follow.channels.FollowedChannelsFragment
import com.github.andreyasadchy.xtra.ui.games.GamesFragment
import com.github.andreyasadchy.xtra.ui.player.clip.ClipPlayerFragment
import com.github.andreyasadchy.xtra.ui.player.offline.OfflinePlayerFragment
import com.github.andreyasadchy.xtra.ui.player.stream.StreamPlayerFragment
import com.github.andreyasadchy.xtra.ui.player.video.VideoPlayerFragment
import com.github.andreyasadchy.xtra.ui.search.SearchFragment
import com.github.andreyasadchy.xtra.ui.search.channels.ChannelSearchFragment
import com.github.andreyasadchy.xtra.ui.search.games.GameSearchFragment
import com.github.andreyasadchy.xtra.ui.streams.common.StreamsFragment
import com.github.andreyasadchy.xtra.ui.streams.followed.FollowedStreamsFragment
import com.github.andreyasadchy.xtra.ui.videos.channel.ChannelVideosFragment
import com.github.andreyasadchy.xtra.ui.videos.followed.FollowedVideosFragment
import com.github.andreyasadchy.xtra.ui.videos.game.GameVideosFragment
import com.github.andreyasadchy.xtra.ui.videos.top.TopVideosFragment
import com.github.andreyasadchy.xtra.ui.view.chat.MessageClickedDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeGamesFragment(): GamesFragment

    @ContributesAndroidInjector
    abstract fun contributeFollowedStreamsFragment(): FollowedStreamsFragment

    @ContributesAndroidInjector
    abstract fun contributeStreamsFragment(): StreamsFragment

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
    abstract fun contributeChannelVideosFragment(): ChannelVideosFragment

    @ContributesAndroidInjector
    abstract fun contributeVideosFragment(): GameVideosFragment

    @ContributesAndroidInjector
    abstract fun contributeFollowedVideosFragment(): FollowedVideosFragment

    @ContributesAndroidInjector
    abstract fun contributeTopVideosFragment(): TopVideosFragment

    @ContributesAndroidInjector
    abstract fun contributeDownloadsFragment(): DownloadsFragment

    @ContributesAndroidInjector
    abstract fun contributeVideoDownloadDialog(): VideoDownloadDialog

    @ContributesAndroidInjector
    abstract fun contributeClipDownloadDialog(): ClipDownloadDialog

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment

    @ContributesAndroidInjector
    abstract fun contributeChannelSearchFragment(): ChannelSearchFragment

    @ContributesAndroidInjector
    abstract fun contributeGameSearchFragment(): GameSearchFragment

    @ContributesAndroidInjector
    abstract fun contributeChannelPagerFragment(): ChannelPagerFragment

    @ContributesAndroidInjector
    abstract fun contributeMessageClickedDialog(): MessageClickedDialog

    @ContributesAndroidInjector
    abstract fun contributeChatFragment(): ChatFragment

    @ContributesAndroidInjector
    abstract fun contributeFollowedChannelsFragment(): FollowedChannelsFragment

    @ContributesAndroidInjector
    abstract fun contributeChannelInfoFragment(): ChannelInfoFragment
}
