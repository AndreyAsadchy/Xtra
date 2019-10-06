package com.github.exact7.xtra.util

import android.content.Context
import android.text.format.DateUtils
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.util.chat.LiveChatThread
import com.github.exact7.xtra.util.chat.MessageListenerImpl
import com.github.exact7.xtra.util.chat.OnChatMessageReceivedListener
import java.text.SimpleDateFormat
import java.util.*


object TwitchApiHelper {

    init {
        System.loadLibrary("keys")
    }

    external fun getClientId(): String

    var validated = false

    fun getTemplateUrl(url: String, width: Int, height: Int): String {
        return url.replace("{width}", width.toString()).replace("{height}", height.toString())
    }

    fun parseIso8601Date(date: String): Long {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(date)?.time ?: 0L
    }

    fun formatTime(context: Context, iso8601date: String): String {
        return formatTime(context, parseIso8601Date(iso8601date))
    }

    fun formatTime(context: Context, date: Long): String {
        val year = Calendar.getInstance().let {
            it.timeInMillis = date
            it.get(Calendar.YEAR)
        }
        val format = if (year == Calendar.getInstance().get(Calendar.YEAR)) {
            DateUtils.FORMAT_NO_YEAR
        } else {
            DateUtils.FORMAT_SHOW_DATE
        }
        return DateUtils.formatDateTime(context, date, format)
    }

    fun startChat(channelName: String, userName: String?, userToken: String?, subscriberBadges: SubscriberBadgesResponse?, newMessageListener: OnChatMessageReceivedListener): LiveChatThread {
        return LiveChatThread(userName, userToken, channelName, MessageListenerImpl(subscriberBadges, newMessageListener)).apply { start() }
    }

    fun parseClipOffset(url: String): Double {
        val time = url.substringAfterLast('=').split("\\D".toRegex())
        var offset = 0.0
        var multiplier = 1.0
        for (i in time.lastIndex - 1 downTo 0) {
            offset += time[i].toDouble() * multiplier
            multiplier *= 60
        }
        return offset
    }

    fun formatCount(context: Context, count: Int, viewers: Boolean = false): String {
        return if (count > 1000) {
            val divider: Int
            val suffix = if (count.toString().length < 7) {
                divider = 1000
                "K"
            } else {
                divider = 1_000_000
                "M"
            }
            val truncated = count / (divider / 10)
            val hasDecimal = truncated / 10.0 != (truncated / 10).toDouble()
            val formatted = if (hasDecimal) "${truncated / 10.0}$suffix" else "${truncated / 10}$suffix"
            context.getString(if (viewers) R.string.viewers else R.string.views, formatted)
        } else {
            context.resources.getQuantityString(if (viewers) R.plurals.viewers else R.plurals.views, count, count)
        }
    }
}
