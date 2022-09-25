package com.example.ping.network

import com.example.ping.utils.Constants
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface ApiInterface {
    @GET(Constants.API_ENDPOINT)
    fun getRandomMeme(): Single<RandomMemeAPIModel>
}