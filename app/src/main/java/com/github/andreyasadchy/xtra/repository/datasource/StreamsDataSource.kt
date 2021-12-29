package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import kotlinx.coroutines.CoroutineScope

class StreamsDataSource private constructor(
    private val clientId: String?,
    private val userToken: String?,
    private val game: String?,
    private val languages: String?,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Stream>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            val get = api.getStreams(clientId, userToken, game, languages, params.requestedLoadSize, offset)
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
            val get = api.getStreams(clientId, userToken, game, languages, params.loadSize, offset)
            val list = mutableListOf<Stream>()
            if (offset != null && offset != "") {
                list.addAll(get.data)
                for (i in list) {
                    i.profileImageURL = i.user_id?.let { api.getUserById(clientId, userToken, i.user_id).data?.first()?.profile_image_url }
                }
                offset = get.pagination?.cursor
            }
            list
        }
    }

    class Factory(
        private val clientId: String?,
        private val userToken: String?,
        private val game: String?,
        private val languages: String?,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, StreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
                StreamsDataSource(clientId, userToken, game, languages, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
