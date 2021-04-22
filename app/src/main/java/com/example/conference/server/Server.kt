package com.example.conference.server

import android.content.Context
import com.example.conference.account.Account
import com.example.conference.db.data.MessageType
import com.example.conference.db.data.SenderEnum
import com.example.conference.exception.SendMessageException
import com.example.conference.json.CMessageList
import com.example.conference.json.ConferenceMessageWithoutID
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*


object Server {
    const val baseURL = "http://192.168.0.105:8082"

    fun get(url: String): Response {
        val client = OkHttpClient.Builder().connectionSpecs(
            listOf(
                ConnectionSpec.MODERN_TLS,
                ConnectionSpec.CLEARTEXT
            )
        ).build()
        val request = Request.Builder().url("$baseURL$url").build()
        return client.newCall(request).execute()
    }

    fun sendNewConferenceAvatar(conference_id: Int, file: ByteArray): Int {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file" , "$conference_id.png",
                file.toRequestBody("application/octet-stream".toMediaTypeOrNull(), 0, file.size)
            )
            .build()

        val request = Request.Builder()
            .url("$baseURL/conference/avatar/upload")
            .post(requestBody)
            .build()
        val client = OkHttpClient.Builder().build()
        val response = client.newCall(request).execute()
        return if (!response.isSuccessful) {
            -1
        } else
            1
    }

    fun sendMeetChatTextMessage(
        conferenceID: Int,
        messageText: String,
        context: Context
    ) {

        val accountData = Account(context)

        val message = ConferenceMessageWithoutID(
            messageText,
            Date().time,
            accountData.userID,
            conferenceID,
            accountData.userName?: "",
            accountData.userSurname?: "",
            SenderEnum.USER.ordinal,
            MessageType.TEXT_MESSAGE.type
        )

        val request = Request.Builder()
            .url("$baseURL/meet_chat/send_message")
            .post(
                Gson()
                    .toJson(message)
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
            ).build()

        val client = OkHttpClient.Builder().build()

        try {
            val response = client.newCall(request).execute()
            if (response.headers["message_id"]?.toInt() ?: -1 == -1) {
                throw SendMessageException()
            }
        } catch(e: SocketException) {
            throw SendMessageException()
        } catch (e: ConnectException) {
            throw SendMessageException()
        } catch (e: SocketTimeoutException) {
            throw SendMessageException()
        }
    }

    fun checkNewMeetChatMessages(conferenceID: Int, lastMessageID: Int): Boolean {
        try {
            val response = get("/meet_chat/check_new_message/" +
                    "?conference_id=$conferenceID&last_message_id=$lastMessageID")
            return if (!response.isSuccessful) {
                false
            } else {
                response.body?.string().toBoolean()
            }
        } catch(e: SocketException) {
        } catch (e: ConnectException) {
        } catch (e: SocketTimeoutException) {
        }
        return false
    }

    fun getNewMeetChatMessages(
        conferenceID: Int,
        lastMessagesID: Int,
        context: Context
    ): CMessageList {
        try {
            val response: Response = get("/meet_chat/get_messages/?" +
                    "conference_id=$conferenceID" +
                    "&last_message_id=$lastMessagesID" +
                    "&user_id=${Account(context).userID}")
            return Gson().fromJson(
                response.body?.string(),
                CMessageList::class.java
            ) ?: CMessageList()
        } catch (e: SocketException) {
        } catch (e: ConnectException) {
        } catch (e: SocketTimeoutException) {
        }
        return CMessageList()
    }
}