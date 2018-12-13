package com.github.exact7.xtra

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
import kotlin.math.roundToInt


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
        val calendar = Calendar.getInstance()
        println("${(calendar.get(Calendar.MINUTE))} ${(calendar.get(Calendar.MINUTE) / 10f).roundToInt()}")
        assertEquals(5, (calendar.get(Calendar.MINUTE) / 10f).roundToInt())
    }
}