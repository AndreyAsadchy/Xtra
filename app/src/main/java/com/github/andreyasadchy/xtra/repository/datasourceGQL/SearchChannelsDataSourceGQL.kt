package com.github.andreyasadchy.xtra.repository.datasourceGQL

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.model.helix.channel.Channel
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import kotlinx.coroutines.CoroutineScope

class SearchChannelsDataSourceGQL private constructor(
    private val clientId: String?,
    private val query: String,
    private val api: GraphQLRepository,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Channel>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Channel>) {
        loadInitial(params, callback) {
            val get = api.loadSearchChannels(clientId, query, offset)
            offset = get.cursor
            get.data
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Channel>) {
        loadRange(params, callback) {
            val get = api.loadSearchChannels(clientId, query, offset)
            if (offset != null && offset != "") {
                offset = get.cursor
                get.data
            } else mutableListOf()
        }
    }

    class Factory(
        private val clientId: String?,
        private val query: String,
        private val api: GraphQLRepository,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Channel, SearchChannelsDataSourceGQL>() {

        override fun create(): DataSource<Int, Channel> =
                SearchChannelsDataSourceGQL(clientId, query, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
