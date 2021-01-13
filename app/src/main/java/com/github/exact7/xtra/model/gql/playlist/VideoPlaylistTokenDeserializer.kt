package com.github.exact7.xtra.model.gql.playlist

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class VideoPlaylistTokenDeserializer : JsonDeserializer<VideoPlaylistTokenResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): VideoPlaylistTokenResponse {
        val tokenJson = json.asJsonArray.first().asJsonObject.getAsJsonObject("data").getAsJsonObject("videoPlaybackAccessToken")
        return VideoPlaylistTokenResponse(tokenJson.getAsJsonPrimitive("value").asString, tokenJson.getAsJsonPrimitive("signature").asString)
    }
}