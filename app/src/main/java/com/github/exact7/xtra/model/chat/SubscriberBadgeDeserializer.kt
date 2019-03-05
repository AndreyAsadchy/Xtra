package com.github.exact7.xtra.model.chat

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class SubscriberBadgeDeserializer : JsonDeserializer<SubscriberBadgesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): SubscriberBadgesResponse {
        val gson = Gson()
        val badgeSets = json.asJsonObject.getAsJsonObject("badge_sets")
        val response: SubscriberBadgesResponse
        if (badgeSets.size() > 0) {
            val versions = badgeSets.getAsJsonObject("subscriber").getAsJsonObject("versions")
            response = SubscriberBadgesResponse(LinkedHashMap(versions.size()))
            for ((key, value) in versions.entrySet()) {
                response.badges?.put(key.toInt(), gson.fromJson(value.asJsonObject, SubscriberBadge::class.java))
            }
        } else {
            response = SubscriberBadgesResponse()
        }
        return response
    }
}
