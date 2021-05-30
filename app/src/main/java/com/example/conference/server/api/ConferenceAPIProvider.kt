package com.example.conference.server.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ConferenceAPIProvider {
    const val BASE_URL = "http://192.168.0.137:8082"

    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val conferenceAPI: ConferenceAPI = retrofit.create(ConferenceAPI::class.java)
    val dialogueMessageAPI: DialogueMessageAPI = retrofit.create(DialogueMessageAPI::class.java)
    val conferenceMessageAPI: ConferenceMessageAPI = retrofit.create(ConferenceMessageAPI::class.java)
    val dialogueAPI: DialogueAPI = retrofit.create(DialogueAPI::class.java)
    val userAPI: UserAPI = retrofit.create(UserAPI::class.java)
}