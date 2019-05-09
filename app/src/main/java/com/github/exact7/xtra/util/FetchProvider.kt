package com.github.exact7.xtra.util

import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.NetworkType
import javax.inject.Inject

class FetchProvider @Inject constructor(
        private val configurationBuilder: FetchConfiguration.Builder) {

    private var instance: Fetch? = null

    fun get(videoId: Int? = null, wifiOnly: Boolean = false): Fetch {
        if (instance == null || instance!!.isClosed) {
            instance = Fetch.getInstance(
                    configurationBuilder
                            .setGlobalNetworkType(if (wifiOnly) NetworkType.WIFI_ONLY else NetworkType.ALL)
                            .setNamespace("Fetch #$videoId")
                            .build())
        }
        return instance!!
    }
}