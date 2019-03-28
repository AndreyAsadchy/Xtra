package com.github.exact7.xtra.ui.common

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.repository.TwitchService

class FollowLiveData(private val context: Context,
                     private val repository: TwitchService) : MutableLiveData<Boolean>()  {

    private lateinit var user: LoggedIn
    private lateinit var channelName: String
    private lateinit var channelId: String

    fun initialize(user: LoggedIn, channelId: String, channelName: String) {
        if ((!this::user.isInitialized || this.user != user) ||
                (!this::channelId.isInitialized || this.channelId != channelId) ||
                (!this::channelName.isInitialized || this.channelName != channelName)) {
            this.user = user
            this.channelId = channelId
            this.channelName = channelName
            repository.loadUserFollows(user.id, channelId).observeForever { super.setValue(it) }
        }
    }

    override fun setValue(value: Boolean?) {
        if (value == true) {
            repository.followChannel(user.token, user.id, channelId).observeForever { super.setValue(it) }
        } else {
            AlertDialog.Builder(context) //TODO need activity, so from fragments?
                    .setMessage(context.getString(R.string.unfollow, channelName))
                    .setPositiveButton(android.R.string.yes) { _, _ -> repository.unfollowChannel(user.token, user.id, channelId).observeForever { super.setValue(!it) }}
                    .setNegativeButton(android.R.string.no) { _, _ -> }
                    .show()
        }
    }
}