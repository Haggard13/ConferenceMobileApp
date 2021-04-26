package com.example.conference.server.provider

import android.content.Context
import com.example.conference.account.Account
import com.example.conference.db.entity.CMessageEntity
import com.example.conference.server.api.ConferenceAPIProvider
import com.example.conference.server.api.ConferenceMessageAPI
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

class ConferenceMessageProvider {

    private val conferenceMessageAPI: ConferenceMessageAPI =
        ConferenceAPIProvider.conferenceMessageAPI

    fun getNewMessages(
        messengerID: Int,
        lastMessageID: Int,
        context: Context
    ): List<CMessageEntity>? {
        val account = Account(context)
        try {
            return conferenceMessageAPI
                .getNewConferenceMessages(messengerID, lastMessageID, account.id)
                .execute()
                .body()
        } catch (e: Exception) {
            when (e) {
                is ConnectException, is SocketException, is SocketTimeoutException ->
                    return null
                else ->
                    throw e
            }
        }
    }

    fun checkNewConferenceMessages(conferenceID: Int, lastMessageID: Int): Boolean {
        try {
            return conferenceMessageAPI
                .checkNewConferenceMessages(conferenceID, lastMessageID)
                .execute()
                .body()?: false
        } catch (e: Exception) {
            when (e) {
                is ConnectException, is SocketException, is SocketTimeoutException ->
                    return false
                else ->
                    throw e
            }
        }
    }
}