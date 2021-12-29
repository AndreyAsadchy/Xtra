package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import kotlinx.coroutines.CoroutineScope

class FollowedStreamsDataSource(
    private val clientId: String?,
    private val userToken: String?,
    private val user_id: String,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Stream>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            val get = api.getFollowedStreams(clientId, userToken, user_id, params.requestedLoadSize, offset)
            val list = mutableListOf<Stream>()
            list.addAll(get.data)
            for (i in list) {
                i.profileImageURL = i.user_id?.let { api.getUserById(clientId, userToken, i.user_id).data?.first()?.profile_image_url }
            }
            offset = get.pagination?.cursor
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            val get = api.getFollowedStreams(clientId, userToken, user_id, params.loadSize, offset)
            val list = mutableListOf<Stream>()
            if (offset != null && offset != "") {
                list.addAll(get.data)
                for (i in list) {
                    i.profileImageURL = i.user_id?.let { api.getUserById(clientId, userToken, it).data?.first()?.profile_image_url }
                }
                offset = get.pagination?.cursor
            }
            list
        }
    }

    class Factory(
        private val clientId: String?,
        private val userToken: String?,
        private val user_id: String,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, FollowedStreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
                FollowedStreamsDataSource(clientId, userToken, user_id, api, coroutineScope).also(sourceLiveData::postValue)
    }
}