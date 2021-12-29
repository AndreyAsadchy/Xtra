package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.channel.Channel
import kotlinx.coroutines.CoroutineScope

class ChannelsSearchDataSource private constructor(
    private val clientId: String?,
    private val userToken: String?,
    private val query: String,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Channel>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Channel>) {
        loadInitial(params, callback) {
            val get = api.getChannels(clientId, userToken, query, params.requestedLoadSize, offset)
            val list = mutableListOf<Channel>()
            list.addAll(get.data)
            for (i in list) {
                i.profileImageURL = i.id?.let { api.getUserById(clientId, userToken, i.id).data?.first()?.profile_image_url }
            }
            offset = get.pagination?.cursor
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Channel>) {
        loadRange(params, callback) {
            val get = api.getChannels(clientId, userToken, query, params.loadSize, offset)
            val list = mutableListOf<Channel>()
            if (offset != null && offset != "") {
                list.addAll(get.data)
                for (i in list) {
                    i.profileImageURL = i.id?.let { api.getUserById(clientId, userToken, i.id).data?.first()?.profile_image_url }
                }
                offset = get.pagination?.cursor
            }
            list
        }
    }

    class Factory(
        private val clientId: String?,
        private val userToken: String?,
        private val query: String,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Channel, ChannelsSearchDataSource>() {

        override fun create(): DataSource<Int, Channel> =
                ChannelsSearchDataSource(clientId, userToken, query, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
