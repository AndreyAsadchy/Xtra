package com.github.exact7.xtra.util

import android.content.Context
import android.text.format.DateUtils
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.util.chat.LiveChatThread
import com.github.exact7.xtra.util.chat.MessageListenerImpl
import com.github.exact7.xtra.util.chat.OnChatMessageReceived
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TwitchApiHelper {

    val clientId: String
        external get

    init {
        System.loadLibrary("keys")
    }

    fun getTemplateUrl(url: String, width: Int, height: Int): String {
        return url.replace("{width}", width.toString()).replace("{height}", height.toString())
    }

    @JvmStatic fun parseIso8601Date(date: String): Long {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            return dateFormat.parse(date).time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0L
    }

    @JvmStatic fun formatTime(context: Context, date: Long): String {
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

    fun startChat(channelName: String, userName: String?, userToken: String?, subscriberBadges: SubscriberBadgesResponse?, newMessageCallback: OnChatMessageReceived): LiveChatThread {
        return LiveChatThread(userName, userToken, channelName, MessageListenerImpl(subscriberBadges, newMessageCallback)).apply { start() }
    }
}
