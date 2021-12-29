package com.github.andreyasadchy.xtra.model.gql.game

import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class GameDataDeserializer : JsonDeserializer<GameDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): GameDataResponse {
        val data = mutableListOf<Game>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("directoriesWithTags").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = if (!it.asJsonObject.get("cursor").isJsonNull) it.asJsonObject.get("cursor").asString else null
            val obj = it.asJsonObject.getAsJsonObject("node")
            data.add(Game(
                    id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else "",
                    name = if (!(obj.get("displayName").isJsonNull)) { obj.getAsJsonPrimitive("displayName").asString } else "",
                    box_art_url = if (!(obj.get("avatarURL").isJsonNull)) { obj.getAsJsonPrimitive("avatarURL").asString } else "",
                    viewersCount = if (!(obj.get("viewersCount").isJsonNull)) { obj.getAsJsonPrimitive("viewersCount").asInt } else 0,
                )
            )
        }
        return GameDataResponse(data, cursor)
    }
}
