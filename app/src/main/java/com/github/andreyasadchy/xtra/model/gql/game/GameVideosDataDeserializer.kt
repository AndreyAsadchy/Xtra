package com.github.andreyasadchy.xtra.model.gql.game

import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class GameVideosDataDeserializer : JsonDeserializer<GameVideosDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): GameVideosDataResponse {
        val data = mutableListOf<Video>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("game").getAsJsonObject("videos").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = if (!it.asJsonObject.get("cursor").isJsonNull) it.asJsonObject.get("cursor").asString else null
            val obj = it.asJsonObject.getAsJsonObject("node")
            data.add(Video(
                    id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else "",
                    user_id = if (!(obj.get("owner").isJsonNull)) { obj.getAsJsonObject("owner").getAsJsonPrimitive("id").asString } else "",
                    user_login = if (!(obj.get("owner").isJsonNull)) { obj.getAsJsonObject("owner").getAsJsonPrimitive("login").asString } else "",
                    user_name = if (!(obj.get("owner").isJsonNull)) { obj.getAsJsonObject("owner").getAsJsonPrimitive("displayName").asString } else "",
                    title = if (!(obj.get("title").isJsonNull)) { obj.getAsJsonPrimitive("title").asString } else "",
                    createdAt = if (!(obj.get("publishedAt").isJsonNull)) { obj.getAsJsonPrimitive("publishedAt").asString } else "",
                    thumbnail_url = if (!(obj.get("previewThumbnailURL").isJsonNull)) { obj.getAsJsonPrimitive("previewThumbnailURL").asString } else "",
                    view_count = if (!(obj.get("viewCount").isJsonNull)) { obj.getAsJsonPrimitive("viewCount").asInt } else 0,
                    duration = if (!(obj.get("lengthSeconds").isJsonNull)) { obj.getAsJsonPrimitive("lengthSeconds").asString } else "",
                    game_name = if (!(json.asJsonObject.getAsJsonObject("data").getAsJsonObject("game").getAsJsonPrimitive("name").isJsonNull)) { json.asJsonObject.getAsJsonObject("data").getAsJsonObject("game").getAsJsonPrimitive("name").asString } else "",
                    profileImageURL = if (!(obj.get("owner").isJsonNull)) { obj.getAsJsonObject("owner").getAsJsonPrimitive("profileImageURL").asString } else ""
                )
            )
        }
        return GameVideosDataResponse(data, cursor)
    }
}
