package com.github.andreyasadchy.xtra.util

fun String.nullIfEmpty() = takeIf { it.isNotEmpty() }