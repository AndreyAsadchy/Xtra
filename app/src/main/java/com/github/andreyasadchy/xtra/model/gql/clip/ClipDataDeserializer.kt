package com.github.andreyasadchy.xtra.model.gql.clip

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class ClipDataDeserializer : JsonDeserializer<ClipDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ClipDataResponse {
        val videos = mutableListOf<ClipDataResponse.Video>()
        val videosJson = json.asJsonArray.first().asJsonObject.getAsJsonObject("data").getAsJsonObject("clip").getAsJsonArray("videoQualities")
        videosJson.forEach {
            val video = it.asJsonObject
            videos.add(ClipDataResponse.Video(
                    video.getAsJsonPrimitive("frameRate").asInt,
                    video.getAsJsonPrimitive("quality").asString,
                    video.getAsJsonPrimitive("sourceURL").asString))
        }
        return ClipDataResponse(videos)
    }
}
