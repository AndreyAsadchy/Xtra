package com.github.exact7.xtra.util.chat

import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.model.chat.RecentEmote
import com.github.exact7.xtra.model.kraken.user.Emote


object EmotesUrlHelper {

    private const val TWITCH_URL = "https://static-cdn.jtvnw.net/emoticons/v1/"
    private const val BTTV_URL = "https://cdn.betterttv.net/emote/"

    fun getTwitchUrl(emoteId: Any) = "$TWITCH_URL$emoteId/2.0"
    fun getBttvUrl(emoteId: Any) = "$BTTV_URL$emoteId/2x"

    fun resolveUrl(emote: com.github.exact7.xtra.model.chat.Emote) = when (emote) {
        is BttvEmote -> getBttvUrl(emote.id)
        is FfzEmote -> emote.url
        is Emote -> getTwitchUrl(emote.id)
        is RecentEmote -> emote.url
        else -> throw IllegalStateException("Unknown emote")
    }
}

