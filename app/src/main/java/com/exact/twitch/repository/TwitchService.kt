package com.exact.twitch.repository

import androidx.lifecycle.LiveData
import com.exact.twitch.model.clip.Clip
import com.exact.twitch.model.game.Game
import com.exact.twitch.model.stream.Stream
import com.exact.twitch.model.user.Emote
import com.exact.twitch.model.user.User
import com.exact.twitch.model.video.Video
import com.exact.twitch.ui.clips.Period
import com.exact.twitch.ui.streams.StreamType
import com.exact.twitch.ui.videos.BroadcastType
import com.exact.twitch.ui.videos.Sort
import io.reactivex.disposables.CompositeDisposable

interface TwitchService {

    fun loadTopGames(compositeDisposable: CompositeDisposable): Listing<Game>
    fun loadStreams(game: String?, languages: String?, streamType: StreamType, compositeDisposable: CompositeDisposable): Listing<Stream>
    fun loadFollowedStreams(userToken: String, streamType: StreamType, compositeDisposable: CompositeDisposable): Listing<Stream>
    fun loadClips(channelName: String?, gameName: String?, languages: String?, period: Period, trending: Boolean, compositeDisposable: CompositeDisposable): Listing<Clip>
    fun loadFollowedClips(userToken: String, trending: Boolean, compositeDisposable: CompositeDisposable): Listing<Clip>
    fun loadVideos(game: String?, period: com.exact.twitch.ui.videos.Period, broadcastType: BroadcastType, language: String?, sort: Sort, compositeDisposable: CompositeDisposable): Listing<Video>
    fun loadFollowedVideos(userToken: String, broadcastType: BroadcastType, language: String?, sort: Sort, compositeDisposable: CompositeDisposable): Listing<Video>
    fun loadChannelVideos(channelId: Any, broadcastType: BroadcastType, sort: Sort, compositeDisposable: CompositeDisposable): Listing<Video>
    fun loadUserById(id: Int) : LiveData<User>
    fun loadUserByLogin(login: String) : LiveData<User>
    fun loadUserEmotes(userId: Int) : LiveData<List<Emote>>
}
