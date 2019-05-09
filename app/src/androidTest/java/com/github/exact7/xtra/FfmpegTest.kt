package com.github.exact7.xtra

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import java.io.File

class FfmpegTest {

    @Test
    fun concatenate() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file1 = "file \'/storage/self/primary/Android/data/com.github.exact7.xtra.debug/files/.downloads/v420751968720p60/420751968v0-1051.ts\'"
        val file2 = "file \'/storage/self/primary/Android/data/com.github.exact7.xtra.debug/files/.downloads/v420751968720p60/1052.ts\'"
        val directory = context.getExternalFilesDir("downloads")!!
        val file = File(directory, "text.txt")
        file.writeText(file1 + "\n" + file2)
    }
}