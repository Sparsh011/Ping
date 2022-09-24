package com.example.ping.model

class User {
    var name: String? = null
    var number: String? = null
    var uid: String? = null

    constructor(){}

    constructor(name: String?, number: String?, uid: String?){
        this.name = name
        this.number = number
        this.uid = uid
    }
}