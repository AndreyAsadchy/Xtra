package com.github.exact7.xtra.model.chat

data class LiveChatMessage(
        override val id: String,
        override val userName: String,
        override val message: String,
        override var color: String?,
        override val isAction: Boolean,
        override val emotes: List<TwitchEmote>?,
        override val badges: List<Badge>?,
        override var subscriberBadge: SubscriberBadge?,
        val userId: Int,
        val userType: String?,
        override val displayName: String,
        val roomId: String,
        val timestamp: Long) : ChatMessage

