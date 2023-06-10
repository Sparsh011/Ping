package com.example.ping.model

class User {
    var name: String? = null
    var number: String? = null
    var uid: String? = null
    var profilePic: String? = null
    var deviceToken: String? = null

    constructor(){}

    constructor(name: String?, number: String?, uid: String?, profilePic: String?){
        this.name = name
        this.number = number
        this.uid = uid
        this.profilePic = profilePic
    }
}