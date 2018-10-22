package com.exact.xtra.repository

import com.exact.xtra.model.clip.Clip
import com.exact.xtra.model.game.Game
import com.exact.xtra.model.stream.Stream
import com.exact.xtra.model.user.Emote
import com.exact.xtra.model.user.User
import com.exact.xtra.model.video.Video
import com.exact.xtra.ui.clips.Period
import com.exact.xtra.ui.streams.StreamType
import com.exact.xtra.ui.videos.BroadcastType
import com.exact.xtra.ui.videos.Sort
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

interface TwitchService {

    fun loadTopGames(compositeDisposable: CompositeDisposable): Listing<Game>
    fun loadStreams(game: String?, languages: String?, streamType: StreamType, compositeDisposable: CompositeDisposable): Listing<Stream>
    fun loadFollowedStreams(userToken: String, streamType: StreamType, compositeDisposable: CompositeDisposable): Listing<Stream>
    fun loadClips(channelName: String?, gameName: String?, languages: String?, period: Period, trending: Boolean, compositeDisposable: CompositeDisposable): Listing<Clip>
    fun loadFollowedClips(userToken: String, trending: Boolean, compositeDisposable: CompositeDisposable): Listing<Clip>
    fun loadVideos(game: String?, period: com.exact.xtra.ui.videos.Period, broadcastType: BroadcastType, language: String?, sort: Sort, compositeDisposable: CompositeDisposable): Listing<Video>
    fun loadFollowedVideos(userToken: String, broadcastType: BroadcastType, language: String?, sort: Sort, compositeDisposable: CompositeDisposable): Listing<Video>
    fun loadChannelVideos(channelId: Any, broadcastType: BroadcastType, sort: Sort, compositeDisposable: CompositeDisposable): Listing<Video>
    fun loadUserById(id: Int) : Single<User>?
    fun loadUserByLogin(login: String) : Single<User>?
    fun loadUserEmotes(userId: Int) : Single<List<Emote>>?
}
