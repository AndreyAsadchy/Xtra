package com.github.exact7.xtra.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private var job: Job? = null
    private val _query = MutableLiveData<String>()
    val query: LiveData<String>
        get() = _query

    fun setQuery(query: String) {
        job?.cancel()
        val trimmed = query.trim()
        if (trimmed.isNotEmpty() && _query.value != trimmed) {
            job = GlobalScope.launch {
                delay(750)
                _query.postValue(query)
            }
        }
    }

    fun retry() {
        _query.value = _query.value
    }
}