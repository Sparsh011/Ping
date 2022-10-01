package com.example.ping.utils

object Constants {
    const val BASE_URL = "https://meme-api.herokuapp.com/"
    const val API_ENDPOINT = "gimme/memes"

    fun selectImageFrom(): ArrayList<String>{
        val list = ArrayList<String>()
        list.add("Camera")
        list.add("Gallery")
        return list
    }

    const val SELECT_FROM_CAMERA = "Camera"
    const val SELECT_FROM_GALLERY = "Gallery"
}