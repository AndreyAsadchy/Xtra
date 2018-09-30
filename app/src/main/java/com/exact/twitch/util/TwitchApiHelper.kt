package com.exact.twitch.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.text.format.DateUtils
import com.exact.twitch.model.chat.SubscriberBadgesResponse
import com.exact.twitch.tasks.LiveChatTask
import com.exact.twitch.util.chat.MessageListenerImpl
import com.exact.twitch.util.chat.OnChatMessageReceived
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object TwitchApiHelper {

    val clientId: String
        external get

    init {
        System.loadLibrary("keys")
    }

    fun getTemplateUrl(url: String, width: Int, height: Int): String {
        return url.replace("{width}", width.toString()).replace("{height}", height.toString())
    }

    @JvmStatic fun parseIso8601Date(context: Context, date: String): String {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val d = dateFormat.parse(date)
            val calendar = Calendar.getInstance()
            calendar.time = d
            val dateUtilType = if (calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                DateUtils.FORMAT_NO_YEAR
            } else {
                DateUtils.FORMAT_SHOW_DATE
            }
            return DateUtils.formatDateTime(context, d.time, dateUtilType)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    fun getCurrentTimeFormatted(context: Context) =
            DateUtils.formatDateTime(context, Calendar.getInstance().time.time, DateUtils.FORMAT_NO_YEAR)

    fun getUserToken(context: Context): String? =
            context.getSharedPreferences(C.AUTH_PREFS, MODE_PRIVATE).getString(C.TOKEN, null)

    fun startChat(channelName: String, userName: String?, userToken: String?, subscriberBadges: SubscriberBadgesResponse, newMessageCallback: OnChatMessageReceived): LiveChatTask {
        return LiveChatTask(userName, userToken, channelName, MessageListenerImpl(subscriberBadges, newMessageCallback)).apply { start() }
    }
}
