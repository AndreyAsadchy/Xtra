package com.github.exact7.xtra

import com.google.gson.annotations.SerializedName

data class Global(
        @SerializedName("default_sets")
        val defaultSets: List<Int>,
        val sets: Sets,
        val users: Users
) {
    data class Sets(
            @SerializedName("3")
            val x3: X3,
            @SerializedName("4330")
            val x4330: X4330
    ) {
        data class X4330(
                @SerializedName("_type")
                val type: Int,
                val css: Any?,
                val description: Any?,
                val emoticons: List<Emoticon>,
                val icon: Any?,
                val id: Int,
                val title: String
        ) {
            data class Emoticon(
                    val css: Any?,
                    val height: Int,
                    val hidden: Boolean,
                    val id: Int,
                    val margins: Any?,
                    val modifier: Boolean,
                    val name: String,
                    val offset: Any?,
                    val owner: Owner,
                    val `public`: Boolean,
                    val urls: Map<String, String>,
                    val width: Int
            ) {

                data class Owner(
                        @SerializedName("_id")
                        val id: Int,
                        @SerializedName("display_name")
                        val displayName: String,
                        val name: String
                )
            }
        }

        data class X3(
                @SerializedName("_type")
                val type: Int,
                val css: Any?,
                val description: Any?,
                val emoticons: List<Emoticon>,
                val icon: Any?,
                val id: Int,
                val title: String
        ) {
            data class Emoticon(
                    val css: Any?,
                    val height: Int,
                    val hidden: Boolean,
                    val id: Int,
                    val margins: Any?,
                    val modifier: Boolean,
                    val name: String,
                    val offset: Any?,
                    val owner: Owner,
                    val `public`: Boolean,
                    val urls: Map<String, String>,
                    val width: Int
            ) {
                data class Urls(
                        @SerializedName("1")
                        val x1: String
                )

                data class Owner(
                        @SerializedName("_id")
                        val id: Int,
                        @SerializedName("display_name")
                        val displayName: String,
                        val name: String
                )
            }
        }
    }

    data class Users(
            @SerializedName("4330")
            val x4330: List<String>
    )
}