package com.github.exact7.xtra

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Test


class Main {

    @Test
    fun gsonTest() {
        val gson = Gson()
        val json = "{\"id\":4,\"code\":\"[oO](_|\\\\.)[oO]\"}"
        val jsonObject = gson.fromJson<JsonObject>(json, JsonObject::class.java)
        val string = jsonObject.getAsJsonPrimitive("code").asString
        println(string)
//        println(gson.fromJson("{\"test\": \"${jsonObject.getAsJsonPrimitive("code").asString}\"}", JsonPrimitive::class.java).asString)

    }

    @Test
    fun test() {
//        val minutes = System.currentTimeMillis() / 1000 / 60
        val minutes = 26051805
        val lastMinute = minutes % 10
        println(minutes)
        println(lastMinute)
        println(if (lastMinute < 5) minutes - lastMinute else minutes - (lastMinute - 5))
    }

    @Test
    fun test2() {
//        val minutes = 26051805
        val minutes = System.currentTimeMillis() / 60000L
        val lastMinute = minutes % 10
        println(if (lastMinute < 5) minutes - lastMinute else minutes - (lastMinute - 5))
    }
}