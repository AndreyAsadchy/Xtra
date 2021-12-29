package com.github.andreyasadchy.xtra.model.helix.emote

import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.ui.view.chat.emoteQuality
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class EmoteSetDeserializer : JsonDeserializer<EmoteSetResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): EmoteSetResponse {
        val emotes = mutableListOf<TwitchEmote>()
        for (i in 0 until json.asJsonObject.getAsJsonArray("data").size()) {
            val emote = json.asJsonObject.getAsJsonArray("data").get(i).asJsonObject
            val urls = emote.getAsJsonObject("images")
            val url = urls.get(when (emoteQuality) {"3" -> ("url_4x") "2" -> ("url_2x") else -> ("url_1x")}).takeUnless { it?.isJsonNull == true }?.asString ?: urls.get("url_2x").takeUnless { it?.isJsonNull == true }?.asString ?: urls.get("url_1x").asString
            val format = if (emote.getAsJsonArray("format").first().asString.equals("animated")) "image/gif" else "image/png"
            emotes.add(TwitchEmote(emote.get("name").asString, type = format, url = url))
        }
        return EmoteSetResponse(emotes)
    }
}