package com.github.exact7.xtra.repository

import android.util.Log
import com.github.exact7.xtra.api.GraphQLApi
import com.github.exact7.xtra.util.TwitchApiHelper
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GraphQLRepositoy"

@Singleton
class GraphQLRepositoy @Inject constructor(private val graphQL: GraphQLApi) {

    suspend fun followChannel(userToken: String, channelId: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Following channel $channelId")
        val array = JsonArray(1)
        val followOperation = JsonObject().apply {
            addProperty("operationName", "FollowButton_FollowUser")
            add("variables", JsonObject().apply {
                add("input", JsonObject().apply {
                    addProperty("disableNotifications", false)
                    addProperty("targetID", channelId)
                })
            })
            add("extensions", JsonObject().apply {
                add("persistedQuery", JsonObject().apply {
                    addProperty("version", 1)
                    addProperty("sha256Hash", "3efee1acda90efdff9fef6e6b4a29213be3ee490781c5b54469717b6131ffdfe")
                })
            })
        }
        array.add(followOperation)
        graphQL.followChannel(TwitchApiHelper.addTokenPrefix(userToken), array).code() == 200
    }

    suspend fun loadClipUrls(slug: String): Map<String, String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading clip urls for clip: $slug")
        val array = JsonArray(1)
        val videoAccessTokenOperation = JsonObject().apply {
            addProperty("operationName", "VideoAccessToken_Clip")
            add("variables", JsonObject().apply {
                addProperty("slug", slug)
            })
            add("extensions", JsonObject().apply {
                add("persistedQuery", JsonObject().apply {
                    addProperty("version", 1)
                    addProperty("sha256Hash", "9bfcc0177bffc730bd5a5a89005869d2773480cf1738c592143b5173634b7d15")
                })
            })
        }
        array.add(videoAccessTokenOperation)
        val response = graphQL.getClipData(array)
        response.videos.associateBy({ if (it.frameRate != 60) "${it.quality}p" else "${it.quality}p${it.frameRate}" }, { it.url })
    }

    suspend fun loadChannelPanel(channelId: String): String? = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading panel for channel: $channelId")
        val array = JsonArray(1)
        val panelOperation = JsonObject().apply {
            addProperty("operationName", "ChannelPanels")
            add("variables", JsonObject().apply {
                addProperty("id", channelId)
            })
            add("extensions", JsonObject().apply {
                add("persistedQuery", JsonObject().apply {
                    addProperty("version", 1)
                    addProperty("sha256Hash", "236b0ec07489e5172ee1327d114172f27aceca206a1a8053106d60926a7f622e")
                })
            })
        }
        array.add(panelOperation)
        graphQL.getChannelPanel(array).body()?.string()
    }
}