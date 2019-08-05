package com.github.exact7.xtra.util

fun String.nullIfEmpty() = takeIf { it.isNotEmpty() }