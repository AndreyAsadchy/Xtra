package com.github.andreyasadchy.xtra.model.gql.stream

import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class StreamDataDeserializer : JsonDeserializer<StreamDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): StreamDataResponse {
        val data = mutableListOf<Stream>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("streams").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = if (!it.asJsonObject.get("cursor").isJsonNull) it.asJsonObject.get("cursor").asString else null
            val obj = it.asJsonObject.getAsJsonObject("node")
            data.add(Stream(
                    id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else "",
                    user_id = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("id").asString } else "",
                    user_login = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("login").asString } else "",
                    user_name = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("displayName").asString } else "",
                    game_id = if (!(obj.get("game").isJsonNull)) { obj.getAsJsonObject("game").getAsJsonPrimitive("id").asString } else "",
                    game_name = if (!(obj.get("game").isJsonNull)) { obj.getAsJsonObject("game").getAsJsonPrimitive("displayName").asString } else "",
                    type = if (!(obj.get("type").isJsonNull)) { obj.getAsJsonPrimitive("type").asString } else "",
                    title = if (!(obj.get("title").isJsonNull)) { obj.getAsJsonPrimitive("title").asString } else "",
                    viewer_count = if (!(obj.get("viewersCount").isJsonNull)) { obj.getAsJsonPrimitive("viewersCount").asInt } else 0,
                    thumbnail_url = if (!(obj.get("previewImageURL").isJsonNull)) { obj.getAsJsonPrimitive("previewImageURL").asString } else "",
                    profileImageURL = if (!(obj.get("broadcaster").isJsonNull)) { obj.getAsJsonObject("broadcaster").getAsJsonPrimitive("profileImageURL").asString } else ""
                )
            )
        }
        return StreamDataResponse(data, cursor)
    }
}
