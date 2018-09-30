package com.exact.twitch.ui.clips

enum class Period(val value: String) {
    DAY("day"), WEEK("week"), MONTH("month"), ALL("all");

    override fun toString() = value
}