package com.example.ping.network

object NewMemeModel{
    data class Memes(
        val count: Int,
        val memesList: List<MemeClass>
    )

    data class MemeClass(
        val author: String,
        val nsfw: Boolean,
        val postLink: String,
        val preview: List<String>,
        val spoiler: Boolean,
        val subreddit: String,
        val title: String,
        val ups: Int,
        val url: String
    )
}