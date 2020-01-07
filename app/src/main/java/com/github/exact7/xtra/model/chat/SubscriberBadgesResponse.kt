package com.github.exact7.xtra.model.chat

class SubscriberBadgesResponse(private val badges: Map<Int, SubscriberBadge> = emptyMap()) {

    fun getBadge(months: Int): SubscriberBadge? = badges[months]
}
