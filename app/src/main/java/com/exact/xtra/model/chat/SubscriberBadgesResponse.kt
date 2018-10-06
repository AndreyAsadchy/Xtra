package com.exact.xtra.model.chat

class SubscriberBadgesResponse(val badges: LinkedHashMap<Int, SubscriberBadge>? = null) {

    fun getBadge(months: Int): SubscriberBadge? {
        return badges?.get(months)
    }
}
