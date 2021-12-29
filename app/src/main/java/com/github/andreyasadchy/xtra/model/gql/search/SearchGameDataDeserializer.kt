package com.github.andreyasadchy.xtra.model.gql.search

import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class SearchGameDataDeserializer : JsonDeserializer<SearchGameDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): SearchGameDataResponse {
        val data = mutableListOf<Game>()
        var cursor: String? = null
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonObject("searchFor").getAsJsonObject("games").getAsJsonArray("edges")
        dataJson.forEach {
            cursor = if (!json.asJsonObject.getAsJsonObject("data").getAsJsonObject("searchFor").getAsJsonObject("games").get("cursor").isJsonNull) json.asJsonObject.getAsJsonObject("data").getAsJsonObject("searchFor").getAsJsonObject("games").getAsJsonPrimitive("cursor").asString else null
            val obj = it.asJsonObject.getAsJsonObject("item")
            data.add(Game(
                    id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else "",
                    name = if (!(obj.get("displayName").isJsonNull)) { obj.getAsJsonPrimitive("displayName").asString } else "",
                    box_art_url = if (!(obj.get("boxArtURL").isJsonNull)) { obj.getAsJsonPrimitive("boxArtURL").asString } else "",
                    viewersCount = if (!(obj.get("viewersCount").isJsonNull)) { obj.getAsJsonPrimitive("viewersCount").asInt } else 0,
                )
            )
        }
        return SearchGameDataResponse(data, cursor)
    }
}
