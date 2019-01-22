package com.github.exact7.xtra.util

import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import javax.inject.Inject

class FetchProvider @Inject constructor(
        private val configuration: FetchConfiguration
) {

    private var instance: Fetch? = null

    fun get(): Fetch {
        if (instance == null || instance!!.isClosed) {
            instance = Fetch.getInstance(configuration).also { instance = it }
        }
        return instance!!
    }
}