package com.exact.twitch.model.user

import com.google.gson.*
import java.lang.reflect.Type
import java.util.*

class UserEmotesDeserializer : JsonDeserializer<UserEmotesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): UserEmotesResponse {
        val gson = Gson()
        val sets = json.asJsonObject.getAsJsonObject("emoticon_sets")
        val list = ArrayList<Emote>()
        for ((_, value) in sets.entrySet()) {
            for (emote in value.asJsonArray) {
                list.add(gson.fromJson(emote.asJsonObject, Emote::class.java))
            }
        }
        return UserEmotesResponse(list)
    }
}
