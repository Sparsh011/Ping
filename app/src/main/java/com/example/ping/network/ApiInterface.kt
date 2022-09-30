package com.example.ping.network

import com.example.ping.utils.Constants
import retrofit2.Response
import retrofit2.http.GET

interface ApiInterface {
    @GET(Constants.API_ENDPOINT)
    suspend fun getMemes(): Response<NewMemeModel.MemeClass>
}