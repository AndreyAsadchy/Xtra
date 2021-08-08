package com.github.andreyasadchy.xtra.model.kraken.video

enum class Period(val value: String) {
    DAY("day"),
    WEEK("week"),
    MONTH("month"),
    ALL("all");

    override fun toString() = value
}