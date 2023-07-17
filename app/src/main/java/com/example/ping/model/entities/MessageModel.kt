package com.example.ping.model.entities


data class MessageModel(
    var message: String? = null,
    var senderId: String? = null,
    var sentAtTime: String? = null,
    var sentOnDay: String? = null,
    val id: Int = 0
)