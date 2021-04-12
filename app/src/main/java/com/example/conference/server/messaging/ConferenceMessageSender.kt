package com.example.conference.server.messaging

import android.content.Context
import com.example.conference.account.Account
import com.example.conference.db.data.MessageType
import com.example.conference.db.data.SenderEnum
import com.example.conference.db.entity.CMessageEntity
import com.example.conference.exception.SendMessageException
import com.example.conference.file.Addition
import com.example.conference.server.api.ConferenceAPIProvider
import com.example.conference.server.sender.MessageSender
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*

class ConferenceMessageSender: MessageSender() {
    private val conferenceAPI = ConferenceAPIProvider.conferenceAPI

    override fun sendTextMessage(context: Context, messageText: String, conferenceID: Int) {
        val account = Account(context)

        val message = CMessageEntity(
            id = -1,
            messageText,
            date_time = Date().time,
            account.userID,
            conferenceID,
            sender_name = account.userName?: "",
            sender_surname = account.userSurname?: "",
            SenderEnum.USER.ordinal,
            MessageType.TEXT_MESSAGE.type
        )

        try {
            val messageID: Int = conferenceAPI
                .sendTextMessageInConference(message)
                .execute()
                .body()?: -1
            if (messageID == -1)
                throw SendMessageException()
        } catch (e: SocketException) {
            throw SendMessageException()
        } catch (e: ConnectException) {
            throw SendMessageException()
        } catch (e: SocketTimeoutException) {
            throw SendMessageException()
        }
    }

    override fun sendMessageWithPhoto(
        context: Context,
        photo: ByteArray?,
        messageText: String,
        conferenceID: Int
    ) {
        val accountData = Account(context)
        val message = CMessageEntity(
            id = -1,
            messageText,
            date_time = Date().time,
            sender_id = accountData.userID,
            conferenceID,
            sender_name = accountData.userName?: "",
            sender_surname = accountData.userSurname?: "",
            SenderEnum.USER.ordinal,
            MessageType.MESSAGE_WITH_PHOTO.type
        )
        val photoRequestBody: RequestBody = photo!!
            .toRequestBody("application/octet-stream".toMediaTypeOrNull(), 0, photo.size)
        val photoMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            "photo.png",
            photoRequestBody
        )
        val messageRequestBody: RequestBody = Gson()
            .toJson(message)
            .toRequestBody("application/json".toMediaTypeOrNull())
        try {
            val messageID: Int = conferenceAPI
                .sendMessageWithPhotoInConference(photoMultipart, messageRequestBody)
                .execute()
                .body()?: -1
            if (messageID == -1) {
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

    override fun sendAudioMessage(context: Context, audio: ByteArray?, conferenceID: Int) {
        val accountData = Account(context)
        val message = CMessageEntity(
            id = -1,
            "Аудиосообщение",
            Date().time,
            accountData.userID,
            conferenceID,
            accountData.userName?: "",
            accountData.userSurname?: "",
            SenderEnum.USER.ordinal,
            MessageType.AUDIO_MESSAGE.type
        )
        val audioRequestBody: RequestBody = audio!!
            .toRequestBody("application/octet-stream".toMediaTypeOrNull(), 0, audio.size)
        val audioMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "audio",
            "audio.3gp",
            audioRequestBody
        )
        val messageRequestBody: RequestBody = Gson()
            .toJson(message)
            .toRequestBody("application/json".toMediaTypeOrNull())

        try {
            val messageID: Int =
                conferenceAPI
                    .sendAudioMessageInConference(audioMultipart, messageRequestBody)
                    .execute()
                    .body()?: -1
            if (messageID == -1) {
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

    override fun sendMessageWithFile(context: Context, addition: Addition, conferenceID: Int) {
        val accountData = Account(context)
        val message = CMessageEntity(
            id = -1,
            addition.name,
            Date().time,
            accountData.userID,
            conferenceID,
            accountData.userName?: "",
            accountData.userSurname?: "",
            SenderEnum.USER.ordinal,
            MessageType.MESSAGE_WITH_FILE.type
        )
        val fileRequestBody: RequestBody = addition.file
            .toRequestBody("application/octet-stream".toMediaTypeOrNull(), 0, addition.file.size)
        val fileMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "file",
            addition.name,
            fileRequestBody
        )
        val messageRequestBody: RequestBody = Gson()
            .toJson(message)
            .toRequestBody("application/json".toMediaTypeOrNull())
        try {
            val messageID: Int =
                conferenceAPI
                    .sendMessageWithFileInConference(fileMultipart, messageRequestBody)
                    .execute()
                    .body()?: -1
            if (messageID == -1) {
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
}