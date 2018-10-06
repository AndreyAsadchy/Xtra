package com.exact.xtra.ui.videos

enum class Period(val value: String) {
    WEEK("week"),
    MONTH("month"),
    ALL("all");

    override fun toString() = value
}