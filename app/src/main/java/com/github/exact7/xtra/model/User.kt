package com.github.exact7.xtra.model

sealed class User

class LoggedIn(user: NotValidated) : User()
class NotValidated(
        val id: String,
        val name: String,
        val token: String) : User()
object NotLoggedIn : User()
