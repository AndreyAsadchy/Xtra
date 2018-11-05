package com.github.exact7.xtra.model.chat

interface ChatMessage {

    val id: String
    val userName: String
    val displayName: String
    val message: String
    val color: String?
    val emotes: List<Emote>?
    val badges: List<Badge>?
    val subscriberBadge: SubscriberBadge?
}
