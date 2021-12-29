package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import kotlinx.coroutines.CoroutineScope

class ClipsDataSource(
    private val clientId: String?,
    private val userToken: String?,
    private val channelName: String?,
    private val gameName: String?,
    private val started_at: String?,
    private val ended_at: String?,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Clip>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Clip>) {
        loadInitial(params, callback) {
            val get = api.getClips(clientId, userToken, channelName, gameName, started_at, ended_at, params.requestedLoadSize, offset)
            val list = mutableListOf<Clip>()
            list.addAll(get.data)
            for (i in list) {
                i.game_name = i.game_id?.let { api.getGame(clientId, userToken, i.game_id).data.first().name }
                val user = i.broadcaster_id?.let { api.getUserById(clientId, userToken, i.broadcaster_id).data?.first() }
                if (i.broadcaster_id != "") {
                    i.profileImageURL = user?.profile_image_url
                    i.broadcaster_login = user?.login ?: ""
                }
            }
            offset = get.pagination?.cursor
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Clip>) {
        loadRange(params, callback) {
            val get = api.getClips(clientId, userToken, channelName, gameName, started_at, ended_at, params.loadSize, offset)
            val list = mutableListOf<Clip>()
            if (offset != null && offset != "") {
                list.addAll(get.data)
                for (i in list) {
                    i.game_name = i.game_id?.let { api.getGame(clientId, userToken, i.game_id).data.first().name }
                    val user = i.broadcaster_id?.let { api.getUserById(clientId, userToken, i.broadcaster_id).data?.first() }
                    if (i.broadcaster_id != "") {
                        i.profileImageURL = user?.profile_image_url
                        i.broadcaster_login = user?.login ?: ""
                    }
                }
                offset = get.pagination?.cursor
            }
            list
        }
    }

    class Factory(
        private val clientId: String?,
        private val userToken: String?,
        private val channelName: String?,
        private val gameName: String?,
        private val started_at: String?,
        private val ended_at: String?,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Clip, ClipsDataSource>() {

        override fun create(): DataSource<Int, Clip> =
                ClipsDataSource(clientId, userToken, channelName, gameName, started_at, ended_at, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
