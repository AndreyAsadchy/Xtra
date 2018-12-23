package com.github.exact7.xtra.model.kraken.clip

enum class Period(val value: String) {
    DAY("day"),
    WEEK("week"),
    MONTH("month"),
    ALL("all");

    override fun toString() = value
}