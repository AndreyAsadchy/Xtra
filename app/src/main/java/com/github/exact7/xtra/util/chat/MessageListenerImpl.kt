package com.github.exact7.xtra.util.chat

import com.github.exact7.xtra.model.chat.Badge
import com.github.exact7.xtra.model.chat.LiveChatMessage
import com.github.exact7.xtra.model.chat.SubscriberBadge
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.model.chat.TwitchEmote
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

private const val TAG = "MessageListenerImpl"

class MessageListenerImpl(
        private val subscriberBadges: SubscriberBadgesResponse?,
        private val callback: OnChatMessageReceivedListener) : LiveChatThread.OnMessageReceivedListener {
    
    override fun onMessage(message: String) {
        val parts = message.split(" ".toRegex(), 2)
        val prefix = parts[0]
        val prefixes = splitAndMakeMap(prefix, ";", "=")

        val messageInfo = parts[1] //:<user>!<user>@<user>.tmi.twitch.tv PRIVMSG #<channelName> :<message>
        val userName = messageInfo.substring(1, messageInfo.indexOf("!"))
        val userMessage: String
        val isAction: Boolean
        messageInfo.substring(messageInfo.indexOf(":", 44) + 1).let { //from <message>
            if (!it.startsWith(ACTION)) {
                userMessage = it
                isAction = false
            } else {
                userMessage = it.substring(8, it.lastIndex)
                isAction = true
            }
        }
        var emotesList: MutableList<TwitchEmote>? = null
        val emotes = prefixes["emotes"]
        if (emotes != null) {
            val entries = splitAndMakeMap(emotes, "/", ":").entries
            emotesList = ArrayList(entries.size)
            entries.forEach { emote ->
                emote.value?.split(",")?.forEach { indexes ->
                    val index = indexes.split("-")
                    emotesList.add(TwitchEmote(emote.key, index[0].toInt(), index[1].toInt()))
                }
            }
        }

        var badgesList: MutableList<Badge>? = null
        var subscriberBadge: SubscriberBadge? = null
        val badges = prefixes["badges"]
        if (badges != null) {
            val entries = splitAndMakeMap(badges, ",", "/").entries
            badgesList = ArrayList(entries.size)
            entries.forEach {
                it.value?.let { value ->
                    badgesList.add(Badge(it.key, value))
                    if (it.key == "subscriber" && subscriberBadges != null) {
                        subscriberBadge = subscriberBadges.getBadge(value.toInt())
                    }
                }
            }
        }

        callback.onMessage(LiveChatMessage(
                prefixes["id"]!!,
                userName,
                userMessage,
                prefixes["color"],
                isAction,
                emotesList,
                badgesList,
                subscriberBadge,
                prefixes["user-id"]!!.toInt(),
                prefixes["user-type"],
                prefixes["display-name"]!!,
                prefixes["room-id"]!!,
                prefixes["tmi-sent-ts"]!!.toLong()))
    }

    override fun onNotice(message: String) {
//        println("NOTICE $message")
    }

    override fun onUserNotice(message: String) {
//        println("USER NOTICE $message")

    }

    override fun onRoomState(message: String) {
//        println("ROOMSTATE $message")

    }

    override fun onJoin(message: String) {
//        println("JOIN $message")

    }

    private fun splitAndMakeMap(string: String, splitRegex: String, mapRegex: String): Map<String, String?> {
        val list = string.split(splitRegex.toRegex()).dropLastWhile { it.isEmpty() }
        val map = HashMap<String, String?>()
        for (pair in list) {
            val kv = pair.split(mapRegex.toRegex()).dropLastWhile { it.isEmpty() }
            map[kv[0]] = if (kv.size == 2) kv[1] else null
        }
        return map
    }
    
    companion object {
        const val ACTION = "\u0001ACTION"
    }
}
