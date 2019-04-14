package com.github.exact7.xtra

import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.FfzEmote
import org.junit.Test

class Main {

    @Test
    fun test() {
        val bttv = BttvEmote("", "Kappa", "")
        val ffz = FfzEmote("Kappa", "", 0f)
        println(bttv.equals(ffz))
    }
}