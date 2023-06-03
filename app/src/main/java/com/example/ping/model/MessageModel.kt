package com.example.ping.model

class MessageModel {
    var message: String? = null
    var senderId: String? = null
    var sentAtTime: String? = null
    var sentOnDay: String? = null

    constructor(){}

    constructor(message: String?, senderId: String?, sentAtTime: String?, sentAtDay: String?){
        this.message = message
        this.senderId = senderId
        this.sentOnDay = sentAtDay
        this.sentAtTime = sentAtTime
    }
}