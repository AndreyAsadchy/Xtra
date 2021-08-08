package com.github.andreyasadchy.xtra

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
        runBlocking {
            val job = GlobalScope.launch {
                throw IllegalStateException()
            }
            job.join()
        }
    }
}