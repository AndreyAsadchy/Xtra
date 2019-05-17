package com.github.exact7.xtra.model.chat

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class FfzRoomDeserializer : JsonDeserializer<FfzRoomResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): FfzRoomResponse {
        val emotes = mutableListOf<FfzEmote>()
        for (setEntry in json.asJsonObject.getAsJsonObject("sets").entrySet()) {
            val emotesArray = setEntry.value.asJsonObject.getAsJsonArray("emoticons")
            for (i in 0 until emotesArray.size()) {
                val emote = emotesArray.get(i).asJsonObject
                val urls = emote.getAsJsonObject("urls")
                emotes.add(FfzEmote(emote.get("name").asString, "https:" + (urls.get("2")?.asString ?: urls.get("1").asString)))
            }
        }
        return FfzRoomResponse(emotes)
    }
}
