package com.github.andreyasadchy.xtra.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.RecyclerView
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.chat.Badge
import com.github.andreyasadchy.xtra.model.chat.ChatMessage
import com.github.andreyasadchy.xtra.model.chat.SubscriberBadge
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote

class ChatViewPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    init {
        widgetLayoutResource = R.layout.chat_settings_recycler_view
    }


    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val recyclerView = holder.itemView.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//        val adapter = ChatAdapter(context.convertDpToPixels(29.5f), context.convertDpToPixels(18.5f), true)
        val messages = mutableListOf(object : ChatMessage {
            override val id: String = "1"
            override val userName: String = "user1"
            override val displayName: String = "user1"
            override val message: String = "Hello 4Head Kappa OMEGALUL FeelsGoodMan"
            override val color: String? = null
            override val isAction: Boolean = false
            override val emotes: List<TwitchEmote>? = listOf(TwitchEmote("354", 6, 10), TwitchEmote("25", 12, 16))
            override val badges: List<Badge>? = null
            override var subscriberBadge: SubscriberBadge? = null
        }, object : ChatMessage {
            override val id: String = "1"
            override val userName: String = "user1"
            override val displayName: String = "user1"
            override val message: String = "Hello 4Head Kappa OMEGALUL FeelsGoodMan"
            override val color: String? = null
            override val isAction: Boolean = false
            override val emotes: List<TwitchEmote>? = listOf(TwitchEmote("354", 6, 10), TwitchEmote("25", 12, 16))
            override val badges: List<Badge>? = null
            override var subscriberBadge: SubscriberBadge? = null
        })
//        adapter.messages = messages
//        recyclerView.adapter = adapter
    }
}