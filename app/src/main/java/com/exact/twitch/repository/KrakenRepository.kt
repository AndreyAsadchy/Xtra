package com.exact.twitch.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.exact.twitch.api.KrakenApi
import com.exact.twitch.model.clip.Clip
import com.exact.twitch.model.game.Game
import com.exact.twitch.model.stream.Stream
import com.exact.twitch.model.user.Emote
import com.exact.twitch.model.user.User
import com.exact.twitch.model.video.Video
import com.exact.twitch.repository.datasource.ChannelVideosDataSource
import com.exact.twitch.repository.datasource.ClipsDataSource
import com.exact.twitch.repository.datasource.FollowedClipsDataSource
import com.exact.twitch.repository.datasource.FollowedStreamsDataSource
import com.exact.twitch.repository.datasource.FollowedVideosDataSource
import com.exact.twitch.repository.datasource.GamesDataSource
import com.exact.twitch.repository.datasource.StreamsDataSource
import com.exact.twitch.repository.datasource.VideosDataSource
import com.exact.twitch.ui.streams.StreamType
import com.exact.twitch.ui.videos.BroadcastType
import com.exact.twitch.ui.videos.Period
import com.exact.twitch.ui.videos.Sort
import com.exact.twitch.util.toLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KrakenRepository @Inject constructor(
        private val api: KrakenApi,
        private val networkExecutor: Executor) : TwitchService {

    companion object {
        private const val TAG = "KrakenRepository"
    }

    override fun loadTopGames(compositeDisposable: CompositeDisposable): Listing<Game> {
        val factory = GamesDataSource.Factory(api, networkExecutor, compositeDisposable)
        val config = PagedList.Config.Builder()
                .setPageSize(50)
                .setInitialLoadSizeHint(30)
                .setPrefetchDistance(20)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config, networkExecutor)
    }

    override fun loadStreams(game: String?, languages: String?, streamType: StreamType, compositeDisposable: CompositeDisposable): Listing<Stream> {
        val factory = StreamsDataSource.Factory(game, languages, streamType, api, networkExecutor, compositeDisposable)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config, networkExecutor)
    }

    override fun loadFollowedStreams(userToken: String, streamType: StreamType, compositeDisposable: CompositeDisposable): Listing<Stream> {
        val factory = FollowedStreamsDataSource.Factory(userToken, streamType, api, networkExecutor, compositeDisposable)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config, networkExecutor)
    }

    override fun loadClips(channelName: String?, gameName: String?, languages: String?, period: com.exact.twitch.ui.clips.Period, trending: Boolean, compositeDisposable: CompositeDisposable): Listing<Clip> {
        val factory = ClipsDataSource.Factory(channelName, gameName, languages, period, trending, api, networkExecutor, compositeDisposable)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config, networkExecutor)
    }

    override fun loadFollowedClips(userToken: String, trending: Boolean, compositeDisposable: CompositeDisposable): Listing<Clip> {
        val factory = FollowedClipsDataSource.Factory(userToken, trending, api, networkExecutor, compositeDisposable)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config, networkExecutor)
    }

    override fun loadVideos(game: String?, period: Period, broadcastType: BroadcastType, language: String?, sort: Sort, compositeDisposable: CompositeDisposable): Listing<Video> {
        val factory = VideosDataSource.Factory(game, period, broadcastType, language, sort, api, networkExecutor, compositeDisposable)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config, networkExecutor)
    }

    override fun loadFollowedVideos(userToken: String, broadcastType: BroadcastType, language: String?, sort: Sort, compositeDisposable: CompositeDisposable): Listing<Video> {
        val factory = FollowedVideosDataSource.Factory(userToken, broadcastType, language, sort, api, networkExecutor, compositeDisposable)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config, networkExecutor)
    }

    override fun loadChannelVideos(channelId: Any, broadcastType: BroadcastType, sort: Sort, compositeDisposable: CompositeDisposable): Listing<Video> {
        val factory = ChannelVideosDataSource.Factory(channelId, broadcastType, sort, api, networkExecutor, compositeDisposable)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config, networkExecutor)
    }

    override fun loadUserById(id: Int): LiveData<User> {
        return api.getUserById(id)
                .observeOn(AndroidSchedulers.mainThread())
                .toLiveData() //TODO decide single or livedata
    }

    override fun loadUserByLogin(login: String): LiveData<User> {
        return api.getUserByLogin(login)
                .observeOn(AndroidSchedulers.mainThread())
                .toLiveData()
    }

    override fun loadUserEmotes(userId: Int): LiveData<List<Emote>> {
        return api.getUserEmotes(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.emotes }
                .toLiveData()
    }
}
