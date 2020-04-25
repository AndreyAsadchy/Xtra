package com.github.exact7.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.model.kraken.follows.Follow
import com.github.exact7.xtra.model.kraken.follows.Order
import com.github.exact7.xtra.model.kraken.follows.Sort
import java.util.concurrent.Executor

class FollowedChannelsDataSource(
        private val userId: String,
        private val sort: Sort,
        private val order: Order,
        private val api: KrakenApi,
        retryExecutor: Executor) : BasePositionalDataSource<Follow>(retryExecutor) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Follow>) {
        loadInitial(params, callback) {
            api.getFollowedChannels(userId, sort, order, params.requestedLoadSize, 0).execute().body()!!.follows
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Follow>) {
        loadRange(params, callback) {
            api.getFollowedChannels(userId, sort, order, params.loadSize, params.startPosition).execute().body()!!.follows
        }
    }

    class Factory(
            private val userId: String,
            private val sort: Sort,
            private val order: Order,
            private val api: KrakenApi,
            private val retryExecutor: Executor) : BaseDataSourceFactory<Int, Follow, FollowedChannelsDataSource>() {

        override fun create(): DataSource<Int, Follow> =
                FollowedChannelsDataSource(userId, sort, order, api, retryExecutor).also(sourceLiveData::postValue)
    }
}
