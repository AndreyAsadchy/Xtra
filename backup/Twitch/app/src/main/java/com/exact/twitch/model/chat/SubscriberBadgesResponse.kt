package com.exact.twitch.model.chat

import android.util.SparseArray

class SubscriberBadgesResponse(val badges: SparseArray<SubscriberBadge>? = null) {

    fun getBadge(months: Int): SubscriberBadge? {
        return badges?.get(months)
    }
}
