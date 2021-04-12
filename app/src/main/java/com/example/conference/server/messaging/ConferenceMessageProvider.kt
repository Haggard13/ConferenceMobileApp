package com.example.conference.server.messaging

import android.content.Context
import com.example.conference.account.Account
import com.example.conference.db.entity.CMessageEntity
import com.example.conference.server.api.ConferenceAPIProvider
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

class ConferenceMessageProvider {
    private val conferenceAPI = ConferenceAPIProvider.conferenceAPI

    fun getNewMessages(
        conferenceID: Int,
        lastMessageID: Int,
        context: Context
    ): List<CMessageEntity>? {
        val account = Account(context)
        try {
            return conferenceAPI
                .getNewConferenceMessages(conferenceID, lastMessageID, account.userID)
                .execute()
                .body()
        } catch (e: SocketException) {
        } catch (e: ConnectException) {
        } catch (e: SocketTimeoutException) {
        }
        return null
    }
}