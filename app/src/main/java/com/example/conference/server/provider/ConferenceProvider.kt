package com.example.conference.server.provider

import android.content.Context
import com.example.conference.account.Account
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.exception.ConferencesGettingException
import com.example.conference.exception.CreateConferenceException
import com.example.conference.json.ConferenceMember
import com.example.conference.server.api.ConferenceAPI
import com.example.conference.server.api.ConferenceAPIProvider
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

class ConferenceProvider {
    private val conferenceAPI: ConferenceAPI = ConferenceAPIProvider.conferenceAPI

    fun getAllConferences(context: Context): List<ConferenceEntity> {
        try {
            val account = Account(context)
            return conferenceAPI
                .getAllConferences(account.userID)
                .execute()
                .body()!!
        } catch (e: Exception) {
            when (e) {
                is ConnectException, is SocketTimeoutException, is SocketException ->
                    throw ConferencesGettingException()
                else ->
                    throw e
            }
        }
    }

    fun createNewConference(conference: ConferenceEntity, members: List<ConferenceMember>): Boolean {
        try {
            val conferenceRequestBody: RequestBody =
                Gson()
                    .toJson(conference)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            val membersRequestBody: RequestBody =
                Gson()
                    .toJson(members)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            return conferenceAPI
                .createNewConference(conferenceRequestBody, membersRequestBody)
                .execute()
                .body() ?: false
        } catch (e: Exception) {
            when (e) {
                is ConnectException, is SocketTimeoutException, is SocketException ->
                    throw CreateConferenceException()
                else ->
                    throw e
            }
        }
    }
}
