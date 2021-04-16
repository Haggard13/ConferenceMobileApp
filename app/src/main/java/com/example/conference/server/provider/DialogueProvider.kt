package com.example.conference.server.provider

import android.content.Context
import com.example.conference.account.Account
import com.example.conference.db.entity.DialogueEntity
import com.example.conference.exception.DialoguesGettingException
import com.example.conference.server.api.ConferenceAPI
import com.example.conference.server.api.ConferenceAPIProvider
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

class DialogueProvider {
    private val conferenceAPI: ConferenceAPI = ConferenceAPIProvider.conferenceAPI

    fun getAllDialogues(context: Context): List<DialogueEntity> {
        try {
            val account = Account(context)
            return conferenceAPI
                .getAllDialogues(account.userID)
                .execute()
                .body()!!
        } catch (e: Exception) {
            when (e) {
                is ConnectException, is SocketException, is SocketTimeoutException ->
                    throw DialoguesGettingException()
                else ->
                    throw e
            }
        }
    }
}
