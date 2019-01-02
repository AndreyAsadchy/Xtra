package com.github.exact7.xtra.db

import androidx.room.TypeConverter
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.kraken.video.Video
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class TypeConverters {

    @TypeConverter
    fun fromVideo(value: Video): String = Gson().toJson(value)

    @TypeConverter
    fun toVideo(value: String): Video = Gson().fromJson(value, Video::class.java)

    @TypeConverter
    fun fromClip(value: Clip): String = Gson().toJson(value)

    @TypeConverter
    fun toClip(value: String): Clip = Gson().fromJson(value, Clip::class.java)

    @TypeConverter
    fun fromArrayList(value: ArrayList<Pair<String, Long>>): String {
        val type = object : TypeToken<ArrayList<Pair<String, Long>>>(){}.type
        return Gson().toJson(value, type)
    }
    @TypeConverter
    fun toArrayList(value: String): ArrayList<Pair<String, Long>> {
        val type = object : TypeToken<ArrayList<Pair<String, Long>>>(){}.type
        return Gson().fromJson<ArrayList<Pair<String, Long>>>(value, type)
    }
}
