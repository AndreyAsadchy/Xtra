package com.github.exact7.xtra.model.chat

interface ChatMessage {
    val id: String
    val userName: String
    val displayName: String
    val message: String
    val color: String?
    val isAction: Boolean
    val emotes: List<TwitchEmote>?
    val badges: List<Badge>?
    var subscriberBadge: SubscriberBadge?
}
