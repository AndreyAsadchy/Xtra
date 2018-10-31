package com.github.exact7.xtra.model.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LiveChatMessage(
        override val id: String,
        override val userName: String,
        override val message: String,
        override var color: String?,
        override val emotes: List<Emote>?,
        override val badges: List<Badge>?,
        override val subscriberBadge: SubscriberBadge?,
        val userId: Int,
        val userType: String?,
        override val displayName: String,
        val roomId: String,
        val timestamp: Long) : ChatMessage, Parcelable

