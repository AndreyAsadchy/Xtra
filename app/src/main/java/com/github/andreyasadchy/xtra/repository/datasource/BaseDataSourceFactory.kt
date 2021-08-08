package com.github.andreyasadchy.xtra.repository.datasource

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource

abstract class BaseDataSourceFactory<Key, Value, DS> : DataSource.Factory<Key, Value>() where DS : DataSource<Key, Value>, DS : PagingDataSource {

    val sourceLiveData = MutableLiveData<DS>()
}
