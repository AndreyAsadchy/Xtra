package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.KrakenApi
import com.github.andreyasadchy.xtra.model.kraken.clip.Clip
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.CoroutineScope

class FollowedClipsDataSource(
        userToken: String,
        private val trending: Boolean?,
        private val api: KrakenApi,
        coroutineScope: CoroutineScope) : BasePageKeyedDataSource<Clip>(coroutineScope) {

    private val userToken: String = TwitchApiHelper.addTokenPrefix(userToken)

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, Clip>) {
        loadInitial(params, callback) {
            api.getFollowedClips(userToken, trending, params.requestedLoadSize, null).let {
                it.clips to it.cursor
            }
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Clip>) {
        loadAfter(params, callback) {
            api.getFollowedClips(userToken, trending, params.requestedLoadSize, params.key).let {
                it.clips to it.cursor
            }
        }
    }

    class Factory(
            private val userToken: String,
            private val trending: Boolean?,
            private val api: KrakenApi,
            private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<String, Clip, FollowedClipsDataSource>() {

        override fun create(): DataSource<String, Clip> =
                FollowedClipsDataSource(userToken, trending, api, coroutineScope).also(sourceLiveData::postValue)
    }
}