package com.github.exact7.xtra.model

sealed class User(val id: String,
                  val name: String,
                  val token: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (name != other.name) return false
        if (token != other.token) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + token.hashCode()
        return result
    }

}

class LoggedIn(id: String, name: String, token: String) : User(id, name, token) {
    constructor(user: NotValidated) : this(user.id, user.name, user.token)
}
class NotValidated(id: String, name: String, token: String) : User(id, name, token)
class NotLoggedIn : User("", "", "")
