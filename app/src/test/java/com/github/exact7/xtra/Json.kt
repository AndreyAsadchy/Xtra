package com.github.exact7.xtra

data class Json(
        val emotes: List<Emote>,
        val status: Int,
        val urlTemplate: String
) {
    data class Emote(
            val channel: String?,
            val code: String,
            val id: String,
            val imageType: String,
            val restrictions: Restrictions
    ) {
        data class Restrictions(
                val channels: List<Any>,
                val games: List<Any>
        )
    }
}