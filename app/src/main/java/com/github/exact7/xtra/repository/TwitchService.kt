package com.github.exact7.xtra.repository

import com.github.exact7.xtra.model.chat.VideoMessagesResponse
import com.github.exact7.xtra.model.kraken.channel.Channel
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.kraken.clip.Period
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.model.kraken.game.GameWrapper
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.model.kraken.stream.StreamType
import com.github.exact7.xtra.model.kraken.stream.StreamWrapper
import com.github.exact7.xtra.model.kraken.user.User
import com.github.exact7.xtra.model.kraken.video.BroadcastType
import com.github.exact7.xtra.model.kraken.video.Sort
import com.github.exact7.xtra.model.kraken.video.Video

interface TwitchService {

    fun loadTopGames(): Listing<GameWrapper>
    suspend fun loadStream(channelId: String): StreamWrapper
    fun loadStreams(game: String?, languages: String?, streamType: StreamType): Listing<Stream>
    fun loadFollowedStreams(userToken: String, streamType: StreamType): Listing<Stream>
    fun loadClips(channelName: String?, gameName: String?, languages: String?, period: Period?, trending: Boolean): Listing<Clip>
    fun loadFollowedClips(userToken: String, trending: Boolean): Listing<Clip>
    suspend fun loadVideo(videoId: String): Video
    fun loadVideos(game: String?, period: com.github.exact7.xtra.model.kraken.video.Period, broadcastType: BroadcastType, language: String?, sort: Sort): Listing<Video>
    fun loadFollowedVideos(userToken: String, broadcastType: BroadcastType, language: String?, sort: Sort): Listing<Video>
    fun loadChannelVideos(channelId: String, broadcastType: BroadcastType, sort: Sort): Listing<Video>
    suspend fun loadUserById(id: Int): User
    suspend fun loadUserByLogin(login: String): User
    suspend fun loadUserEmotes(token: String, userId: String)
    fun loadChannels(query: String): Listing<Channel>
    suspend fun loadVideoChatLog(videoId: String, offsetSeconds: Double): VideoMessagesResponse
    suspend fun loadVideoChatAfter(videoId: String, cursor: String): VideoMessagesResponse
    suspend fun loadUserFollows(userId: String, channelId: String): Boolean
    suspend fun followChannel(userToken: String, userId: String, channelId: String): Boolean
    suspend fun unfollowChannel(userToken: String, userId: String, channelId: String): Boolean
    suspend fun loadGames(query: String): List<Game>
}
