package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import kotlinx.coroutines.CoroutineScope

class FollowedChannelsDataSource(
    private val clientId: String?,
    private val userToken: String?,
    private val userId: String,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Follow>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Follow>) {
        loadInitial(params, callback) {
            val get = api.getFollowedChannels(clientId, userToken, userId, params.requestedLoadSize, offset)
            val list = mutableListOf<Follow>()
            list.addAll(get.data)
            for (i in list) {
                if (i.to_id != "") i.profileImageURL = api.getUserById(clientId, userToken, i.to_id).data?.first()?.profile_image_url
            }
            offset = get.pagination?.cursor
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Follow>) {
        loadRange(params, callback) {
            val get = api.getFollowedChannels(clientId, userToken, userId, params.loadSize, offset)
            val list = mutableListOf<Follow>()
            if (offset != null && offset != "") {
                list.addAll(get.data)
                for (i in list) {
                    if (i.to_id != "") i.profileImageURL = api.getUserById(clientId, userToken, i.to_id).data?.first()?.profile_image_url
                }
                offset = get.pagination?.cursor
            }
            list
        }
    }

    class Factory(
        private val clientId: String?,
        private val userToken: String?,
        private val userId: String,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Follow, FollowedChannelsDataSource>() {

        override fun create(): DataSource<Int, Follow> =
                FollowedChannelsDataSource(clientId, userToken, userId, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
