package com.example.conference.server.conferenceapi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ConferenceAPIProvider {
    private const val BASE_URL = "http://192.168.0.105:8082"

    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val conferenceAPI: ConferenceAPI = retrofit.create(ConferenceAPI::class.java)
}