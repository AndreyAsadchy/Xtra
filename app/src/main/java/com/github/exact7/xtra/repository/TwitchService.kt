package com.github.exact7.xtra.repository

import com.github.exact7.xtra.model.clip.Clip
import com.github.exact7.xtra.model.game.Game
import com.github.exact7.xtra.model.stream.Stream
import com.github.exact7.xtra.model.user.Emote
import com.github.exact7.xtra.model.user.User
import com.github.exact7.xtra.model.video.Video
import com.github.exact7.xtra.ui.clips.Period
import com.github.exact7.xtra.ui.streams.StreamType
import com.github.exact7.xtra.ui.videos.BroadcastType
import com.github.exact7.xtra.ui.videos.Sort
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

interface TwitchService {

    fun loadTopGames(compositeDisposable: CompositeDisposable): Listing<Game>
    fun loadStreams(game: String?, languages: String?, streamType: StreamType, compositeDisposable: CompositeDisposable): Listing<Stream>
    fun loadFollowedStreams(userToken: String, streamType: StreamType, compositeDisposable: CompositeDisposable): Listing<Stream>
    fun loadClips(channelName: String?, gameName: String?, languages: String?, period: Period, trending: Boolean, compositeDisposable: CompositeDisposable): Listing<Clip>
    fun loadFollowedClips(userToken: String, trending: Boolean, compositeDisposable: CompositeDisposable): Listing<Clip>
    fun loadVideos(game: String?, period: com.github.exact7.xtra.ui.videos.Period, broadcastType: BroadcastType, language: String?, sort: Sort, compositeDisposable: CompositeDisposable): Listing<Video>
    fun loadFollowedVideos(userToken: String, broadcastType: BroadcastType, language: String?, sort: Sort, compositeDisposable: CompositeDisposable): Listing<Video>
    fun loadChannelVideos(channelId: Any, broadcastType: BroadcastType, sort: Sort, compositeDisposable: CompositeDisposable): Listing<Video>
    fun loadUserById(id: Int) : Single<User>?
    fun loadUserByLogin(login: String) : Single<User>?
    fun loadUserEmotes(userId: Int) : Single<List<Emote>>?
}
