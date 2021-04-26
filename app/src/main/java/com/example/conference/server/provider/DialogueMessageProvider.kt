package com.example.conference.server.provider

import android.content.Context
import com.example.conference.account.Account
import com.example.conference.db.entity.DMessageEntity
import com.example.conference.server.api.ConferenceAPIProvider
import com.example.conference.server.api.DialogueMessageAPI
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

class DialogueMessageProvider {

    private val dialogueMessageAPI: DialogueMessageAPI = ConferenceAPIProvider.dialogueMessageAPI

    fun getNewMessages(
        messengerID: Int,
        lastMessageID: Int,
        context: Context
    ): List<DMessageEntity>? {
        val account = Account(context)
        try {
            return dialogueMessageAPI
                .getNewDialogueMessages(messengerID, lastMessageID, account.id)
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

    fun checkNewDialogueMessages(dialogueID: Int, lastMessageID: Int): Boolean {
        try {
            return dialogueMessageAPI
                .checkNewDialogueMessages(dialogueID, lastMessageID)
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