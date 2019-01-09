package com.github.exact7.xtra.util.chat

import com.github.exact7.xtra.model.chat.Badge
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.model.chat.LiveChatMessage
import com.github.exact7.xtra.model.chat.SubscriberBadge
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.tasks.LiveChatTask
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set

class MessageListenerImpl(
        private val subscriberBadges: SubscriberBadgesResponse?,
        private val callback: OnChatMessageReceived) : LiveChatTask.OnMessageReceivedListener {

    companion object {
        private const val TAG = "MessageListenerImpl"
    }

    override fun onMessage(message: String) {
        val parts = message.split(" ".toRegex(), 2)
        val prefix = parts[0]
        val prefixes = splitAndMakeMap(prefix, ";", "=")

        val messageInfo = parts[1] //:<user>!<user>@<user>.tmi.twitch.tv PRIVMSG #<channelName> :<message>
        val userName = messageInfo.substring(1, messageInfo.indexOf("!"))
        val userMessage = messageInfo.substring(messageInfo.indexOf(":", 44) + 1) //from <message>

        var emotesList: MutableList<Emote>? = null
        val emotes = prefixes["emotes"]
        if (emotes != null) {
            val entries = splitAndMakeMap(emotes, "/", ":").entries
            emotesList = ArrayList(entries.size)
            entries.forEach { emote ->
                emote.value?.split(",")?.forEach { indexes ->
                    val index = indexes.split("-")
                    emotesList.add(Emote(emote.key, index[0].toInt(), index[1].toInt()))
                }
            }
        }

        var badgesList: MutableList<Badge>? = null
        var subscriberBadge: SubscriberBadge? = null
        val badges = prefixes["@badges"]
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

    }

    override fun onUserNotice(message: String) {

    }

    override fun onRoomState(message: String) {

    }

    override fun onJoin(message: String) {

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
}
