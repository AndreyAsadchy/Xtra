package com.exact.xtra

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.regex.Pattern

/**
 * Example local unit changeQuality, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }

    @Test
    fun test() {
        val url = "http://localhost/#access_token=sl45dggra7pqytb6l8e2ctzdobsn5c&scope=chat_login+user_follows_edit+user_read+user_subscriptions&token_type=bearer"
        val pattern = Pattern.compile("token=(.+?)(?=&)")
        val matcher = pattern.matcher(url)
        if (matcher.find()) {
            println(matcher.group(0))
        }
    }
}