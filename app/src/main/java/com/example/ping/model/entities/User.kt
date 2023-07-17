package com.example.ping.model.entities

data class User(
    var name: String? = null,
    var number: String? = null,
    var uid: String = "",
    var profilePic: String? = null,
    var deviceToken: String? = null
)