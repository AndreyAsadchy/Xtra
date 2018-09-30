package com.exact.twitch.repository

import androidx.lifecycle.LiveData
import com.exact.twitch.model.clip.Clip
import com.exact.twitch.model.game.Game
import com.exact.twitch.model.stream.Stream
import com.exact.twitch.model.user.Emote
import com.exact.twitch.model.user.User
import com.exact.twitch.model.video.Video

interface TwitchService {

    fun loadTopGames(): Listing<Game>
    fun loadStreams(game: String?, languages: String?, streamType: String?): Listing<Stream>
    fun loadFollowedStreams(userToken: String, streamType: String?): Listing<Stream>
    fun loadClips(channelName: String?, gameName: String?, languages: String?, period: String?, trending: Boolean?): Listing<Clip>
    fun loadFollowedClips(userToken: String, trending: Boolean?): Listing<Clip>
    fun loadVideos(game: String?, period: String?, broadcastTypes: String?, language: String?, sort: String?): Listing<Video>
    fun loadFollowedVideos(userToken: String, broadcastTypes: String?, language: String?, sort: String?): Listing<Video>
    fun loadChannelVideos(channelId: Any, broadcastTypes: String?, sort: String?): Listing<Video>
    fun loadUserById(id: Int) : LiveData<User>
    fun loadUserByLogin(login: String) : LiveData<User>
    fun loadUserEmotes(userId: Int) : LiveData<List<Emote>>
}
