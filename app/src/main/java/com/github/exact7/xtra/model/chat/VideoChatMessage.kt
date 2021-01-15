package com.github.exact7.xtra.model.chat

import com.google.gson.annotations.SerializedName

data class VideoChatMessage(
        @SerializedName("_id")
        override val id: String,
        @SerializedName("created_at")
        val createdAt: String,
        @SerializedName("updated_at")
        val updatedAt: String,
        @SerializedName("channel_id")
        val channelId: String,
        @SerializedName("content_type")
        val contentType: String,
        @SerializedName("content_id")
        val contentId: String,
        @SerializedName("content_offset_seconds")
        val contentOffsetSeconds: Double,
        val commenter: Commenter?,
        val source: String,
        val state: String,
        @SerializedName("message")
        val messageObj: Message,
        @SerializedName("more_replies")
        val moreReplies: Boolean) : ChatMessage {

    override val userName: String
        get() = commenter?.name.orEmpty()

    override val message: String
        get() = messageObj.body

    override val color: String?
        get() = messageObj.userColor

    override val isAction: Boolean
        get() = messageObj.isAction

    override val emotes: List<TwitchEmote>?
        get() = messageObj.emoticons

    override val badges: List<Badge>?
        get() = messageObj.userBadges

    override var subscriberBadge: SubscriberBadge? = null

    override val displayName: String
        get() = commenter?.displayName.orEmpty()

    data class Commenter(
            @SerializedName("display_name")
            val displayName: String,
            @SerializedName("_id")
            val id: String,
            val name: String,
            val type: String,
            val bio: String?,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("updated_at")
            val updatedAt: String,
            val logo: String)

    data class Message(
            val body: String,
            val emoticons: List<TwitchEmote>?,
            val fragments: List<Fragment>,
            @SerializedName("is_action")
            val isAction: Boolean,
            @SerializedName("user_badges")
            val userBadges: List<Badge>?,
            @SerializedName("user_color")
            val userColor: String) {

        data class Fragment(val text: String, val emoticon: Emoticon) {

            data class Emoticon(
                    @SerializedName("emoticon_id")
                    val emoticonId: String,
                    @SerializedName("emoticon_set_id")
                    val emoticonSetId: String)
        }
    }
}