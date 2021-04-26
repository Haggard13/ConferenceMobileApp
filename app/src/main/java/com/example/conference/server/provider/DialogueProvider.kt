package com.example.conference.server.provider

import android.content.Context
import com.example.conference.account.Account
import com.example.conference.db.entity.DialogueEntity
import com.example.conference.exception.CreateDialogueException
import com.example.conference.exception.DialoguesGettingException
import com.example.conference.server.api.ConferenceAPIProvider
import com.example.conference.server.api.DialogueAPI
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

class DialogueProvider {
    private val dialogueAPI: DialogueAPI = ConferenceAPIProvider.dialogueAPI

    fun getAllDialogues(context: Context): List<DialogueEntity> {
        try {
            val account = Account(context)
            return dialogueAPI
                .getAllDialogues(account.id)
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

    fun createNewDialogue(dialogue: DialogueEntity, user: Account): Boolean {
        try {
            val dialogueRequestBody: RequestBody =
                Gson()
                    .toJson(dialogue)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            val userRequestBody: RequestBody =
                Gson()
                    .toJson(user)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            return dialogueAPI
                .createNewDialogue(dialogueRequestBody, userRequestBody)
                .execute()
                .body() ?: false
        } catch (e: Exception) {
            when (e) {
                is ConnectException, is SocketTimeoutException, is SocketException ->
                    throw CreateDialogueException()
                else ->
                    throw e
            }
        }
    }
}
