package com.exact.twitch.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.exact.twitch.api.KrakenApi
import com.exact.twitch.model.clip.Clip
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.Executor

class ClipsDataSource(
        private val channelName: String?,
        private val gameName: String?,
        private val languages: String?,
        private val period: String?,
        private val trending: Boolean?,
        private val api: KrakenApi,
        networkExecutor: Executor) : BasePageKeyedDataSource<Clip>(networkExecutor) {

    override fun loadInitial(params: PageKeyedDataSource.LoadInitialParams<String>, callback: LoadInitialCallback<String, Clip>) {
        super.loadInitial(params, callback)
        api.getClips(channelName, gameName, languages, period, trending, params.requestedLoadSize, null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.clips, it.cursor) }, { callback.onFailure(it, params) })
                .dispose()
    }

    override fun loadAfter(params: PageKeyedDataSource.LoadParams<String>, callback: PageKeyedDataSource.LoadCallback<String, Clip>) {
        super.loadAfter(params, callback)
        api.getClips(channelName, gameName, languages, period, trending, params.requestedLoadSize, params.key)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.clips, it.cursor) }, { callback.onFailure(it, params) })
                .dispose()
    }

    class Factory(
            private val channelName: String?,
            private val gameName: String?,
            private val languages: String?,
            private val period: String?,
            private val trending: Boolean?,
            private val api: KrakenApi,
            private val networkExecutor: Executor) : BaseDataSourceFactory<String, Clip, ClipsDataSource>() {

        override fun create(): DataSource<String, Clip> =
            ClipsDataSource(channelName, gameName, languages, period, trending, api, networkExecutor).also(sourceLiveData::postValue)
    }
}
