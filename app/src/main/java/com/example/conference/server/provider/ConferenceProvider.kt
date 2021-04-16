package com.example.conference.server.provider

import android.content.Context
import com.example.conference.account.Account
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.exception.ConferencesGettingException
import com.example.conference.server.api.ConferenceAPI
import com.example.conference.server.api.ConferenceAPIProvider
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
}
