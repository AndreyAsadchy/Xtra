package com.github.andreyasadchy.xtra.model.chat

class SubscriberBadgesResponse(private val badges: Map<Int, SubscriberBadge> = emptyMap()) {

    fun getBadge(months: Int): SubscriberBadge? = badges[months]
}
