package com.exact.twitch.model.chat

import android.util.SparseArray
import com.google.gson.*
import java.lang.reflect.Type

class SubscriberBadgeDeserializer : JsonDeserializer<SubscriberBadgesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): SubscriberBadgesResponse {
        val gson = Gson()
        val response: SubscriberBadgesResponse
        val badgeSets = json.asJsonObject.getAsJsonObject("badge_sets")
        if (badgeSets != null) {
            response = SubscriberBadgesResponse(SparseArray())
            val versions = badgeSets.getAsJsonObject("subscriber").getAsJsonObject("versions")
            for ((key, value) in versions.entrySet()) {
                response.badges?.put(Integer.parseInt(key), gson.fromJson(value.asJsonObject, SubscriberBadge::class.java))
            }
        } else {
            response = SubscriberBadgesResponse()
        }
        return response
    }
}
