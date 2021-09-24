package com.github.andreyasadchy.xtra.model.chat

abstract class Emote {
    abstract val name: String
    abstract val url: String //TODO null if property
    open val isPng: Boolean
        get() = true
    open val zerowidth: Boolean
        get() = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Emote || name != other.name) return false
        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return ":$name"
    }
}