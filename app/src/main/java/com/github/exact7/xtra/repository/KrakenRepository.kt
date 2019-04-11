package com.github.exact7.xtra.repository

import android.util.Log
import androidx.paging.PagedList
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.db.EmotesDao
import com.github.exact7.xtra.model.chat.VideoMessagesResponse
import com.github.exact7.xtra.model.kraken.channel.Channel
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.model.kraken.stream.Stream
import com.github.exact7.xtra.model.kraken.stream.StreamType
import com.github.exact7.xtra.model.kraken.stream.StreamWrapper
import com.github.exact7.xtra.model.kraken.user.Emote
import com.github.exact7.xtra.model.kraken.user.User
import com.github.exact7.xtra.model.kraken.video.BroadcastType
import com.github.exact7.xtra.model.kraken.video.Period
import com.github.exact7.xtra.model.kraken.video.Sort
import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.repository.datasource.ChannelVideosDataSource
import com.github.exact7.xtra.repository.datasource.ChannelsSearchDataSource
import com.github.exact7.xtra.repository.datasource.ClipsDataSource
import com.github.exact7.xtra.repository.datasource.FollowedClipsDataSource
import com.github.exact7.xtra.repository.datasource.FollowedStreamsDataSource
import com.github.exact7.xtra.repository.datasource.FollowedVideosDataSource
import com.github.exact7.xtra.repository.datasource.GamesDataSource
import com.github.exact7.xtra.repository.datasource.StreamsDataSource
import com.github.exact7.xtra.repository.datasource.VideosDataSource
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "KrakenRepository"

@Singleton
class KrakenRepository @Inject constructor(
        private val api: KrakenApi,
        private val networkExecutor: Executor,
        private val emotesDao: EmotesDao) : TwitchService {

    override fun loadTopGames(compositeDisposable: CompositeDisposable): Listing<Game> {
        val factory = GamesDataSource.Factory(api, networkExecutor, compositeDisposable)
        val config = PagedList.Config.Builder()
                .setPageSize(30)
                .setInitialLoadSizeHint(30)
                .setPrefetchDistance(10)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config, networkExecutor)
    }

    override fun loadStream(channelId: String): Single<StreamWrapper> {
        return api.getStream(channelId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { StreamWrapper(it.streams.firstOrNull()) }
//                .toLiveData()
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

    override fun loadClips(channelName: String?, gameName: String?, languages: String?, period: com.github.exact7.xtra.model.kraken.clip.Period?, trending: Boolean, compositeDisposable: CompositeDisposable): Listing<Clip> {
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

    override fun loadChannelVideos(channelId: String, broadcastType: BroadcastType, sort: Sort, compositeDisposable: CompositeDisposable): Listing<Video> {
        val factory = ChannelVideosDataSource.Factory(channelId, broadcastType, sort, api, networkExecutor, compositeDisposable)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config, networkExecutor)
    }

    override fun loadUserById(id: Int): Single<User> {
        Log.d(TAG, "Loading user by id $id")
        return api.getUserById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun loadUserByLogin(login: String): Single<User> {
        Log.d(TAG, "Loading user by login $login")
        return api.getUserByLogin(login)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun loadUserEmotes(token: String, userId: String, compositeDisposable: CompositeDisposable) {
        Log.d(TAG, "Loading user emotes")
        api.getUserEmotes("OAuth $token", userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { emotesDao.insertAll(it.emotes) })
                .addTo(compositeDisposable)
    }

    override fun loadChannels(query: String, compositeDisposable: CompositeDisposable): Listing<Channel> {
        Log.d(TAG, "Loading channels containing: $query")
        val factory = ChannelsSearchDataSource.Factory(query, api, networkExecutor, compositeDisposable)
        val config = PagedList.Config.Builder()
                .setPageSize(15)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(5)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config, networkExecutor)
    }

    override fun loadVideoChatLog(videoId: String, offsetSeconds: Double): Single<VideoMessagesResponse> {
        Log.d(TAG, "Loading chat log for video $videoId. Offset in seconds: $offsetSeconds")
        return api.getVideoChatLog(videoId.substring(1), offsetSeconds, 75)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun loadVideoChatAfter(videoId: String, cursor: String): Single<VideoMessagesResponse> {
        Log.d(TAG, "Loading chat log for video $videoId. Cursor: $cursor")
        return api.getVideoChatLogAfter(videoId.substring(1), cursor, 75)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun loadUserFollows(userId: String, channelId: String): Single<Boolean> {
        Log.d(TAG, "Loading if user is following channel $channelId")
        return api.getUserFollows(userId, channelId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.body()?.let { body -> body.string().length > 300 } == true }
    }

    override fun followChannel(userToken: String, userId: String, channelId: String): Single<Boolean> {
        Log.d(TAG, "Following channel $channelId")
        return api.followChannel("OAuth $userToken", userId, channelId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.body() != null }
    }

    override fun unfollowChannel(userToken: String, userId: String, channelId: String): Single<Boolean> {
        Log.d(TAG, "Unfollowing channel $channelId")
        return api.unfollowChannel("OAuth $userToken", userId, channelId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.code() == 204 }
    }
}
