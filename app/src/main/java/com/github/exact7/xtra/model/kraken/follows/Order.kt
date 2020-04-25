package com.github.exact7.xtra.model.kraken.follows

enum class Order(val value: String) {
    ASC("asc"),
    DESC("desc");

    override fun toString() = value
}