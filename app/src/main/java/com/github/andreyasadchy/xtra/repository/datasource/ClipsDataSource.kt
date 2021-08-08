package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.KrakenApi
import com.github.andreyasadchy.xtra.model.kraken.clip.Clip
import com.github.andreyasadchy.xtra.model.kraken.clip.Period
import kotlinx.coroutines.CoroutineScope

class ClipsDataSource(
        private val channelName: String?,
        private val gameName: String?,
        private val languages: String?,
        private val period: Period?,
        private val trending: Boolean?,
        private val api: KrakenApi,
        coroutineScope: CoroutineScope) : BasePageKeyedDataSource<Clip>(coroutineScope) {

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, Clip>) {
        loadInitial(params, callback) {
            api.getClips(channelName, gameName, languages, period, trending, params.requestedLoadSize, null).let {
                it.clips to it.cursor
            }
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Clip>) {
        loadAfter(params, callback) {
            api.getClips(channelName, gameName, languages, period, trending, params.requestedLoadSize, params.key).let {
                it.clips to it.cursor
            }
        }
    }

    class Factory(
            private val channelName: String?,
            private val gameName: String?,
            private val languages: String?,
            private val period: Period?,
            private val trending: Boolean?,
            private val api: KrakenApi,
            private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<String, Clip, ClipsDataSource>() {

        override fun create(): DataSource<String, Clip> =
                ClipsDataSource(channelName, gameName, languages, period, trending, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
