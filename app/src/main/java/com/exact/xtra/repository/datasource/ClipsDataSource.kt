package com.exact.xtra.repository.datasource

import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.exact.xtra.api.KrakenApi
import com.exact.xtra.model.clip.Clip
import com.exact.xtra.ui.clips.Period
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.Executor

class ClipsDataSource(
        private val channelName: String?,
        private val gameName: String?,
        private val languages: String?,
        private val period: Period,
        private val trending: Boolean?,
        private val api: KrakenApi,
        networkExecutor: Executor,
        private val compositeDisposable: CompositeDisposable) : BasePageKeyedDataSource<Clip>(networkExecutor) {

    override fun loadInitial(params: PageKeyedDataSource.LoadInitialParams<String>, callback: LoadInitialCallback<String, Clip>) {
        super.loadInitial(params, callback)
        api.getClips(channelName, gameName, languages, period, trending, params.requestedLoadSize, null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.clips, it.cursor) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    override fun loadAfter(params: PageKeyedDataSource.LoadParams<String>, callback: PageKeyedDataSource.LoadCallback<String, Clip>) {
        super.loadAfter(params, callback)
        api.getClips(channelName, gameName, languages, period, trending, params.requestedLoadSize, params.key)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback.onSuccess(it.clips, it.cursor) }, { callback.onFailure(it, params) })
                .addTo(compositeDisposable)
    }

    class Factory(
            private val channelName: String?,
            private val gameName: String?,
            private val languages: String?,
            private val period: Period,
            private val trending: Boolean?,
            private val api: KrakenApi,
            private val networkExecutor: Executor,
            private val compositeDisposable: CompositeDisposable) : BaseDataSourceFactory<String, Clip, ClipsDataSource>() {

        override fun create(): DataSource<String, Clip> =
                ClipsDataSource(channelName, gameName, languages, period, trending, api, networkExecutor, compositeDisposable).also(sourceLiveData::postValue)
    }
}
