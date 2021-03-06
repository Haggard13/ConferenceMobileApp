package com.example.conference.server.sender

import android.content.Context
import com.example.conference.account.Account
import com.example.conference.db.data.MessageType
import com.example.conference.db.data.SenderEnum
import com.example.conference.db.entity.DMessageEntity
import com.example.conference.exception.SendMessageException
import com.example.conference.file.Addition
import com.example.conference.server.api.ConferenceAPIProvider
import com.example.conference.server.api.DialogueMessageAPI
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*

class DialogueMessageSender : MessageSender() {
    private val dialogueMessageAPI: DialogueMessageAPI = ConferenceAPIProvider.dialogueMessageAPI

    override suspend fun sendTextMessage(context: Context, messageText: String, messengerID: Int) {
        val account = Account(context)

        val message = DMessageEntity(
            id = -1,
            messageText,
            date_time = Date().time,
            account.id,
            messengerID,
            sender_name = account.name?: "",
            sender_surname = account.surname?: "",
            SenderEnum.USER.ordinal,
            MessageType.TEXT_MESSAGE.type
        )

        try {
            val messageID: Int = dialogueMessageAPI
                .sendTextMessageInDialogue(message)
                .execute()
                .body()?: -1
            if (messageID == -1)
                throw SendMessageException()
        } catch (e: Exception) {
            when(e) {
                is ConnectException, is SocketTimeoutException, is SocketException ->
                    throw SendMessageException()
                else -> throw e
            }
        }
    }

    override suspend fun sendMessageWithPhoto(
        context: Context,
        photo: ByteArray?,
        messageText: String,
        messengerID: Int
    ) {
        val accountData = Account(context)
        val message = DMessageEntity(
            id = -1,
            messageText,
            date_time = Date().time,
            sender_id = accountData.id,
            messengerID,
            sender_name = accountData.name?: "",
            sender_surname = accountData.surname?: "",
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
            val messageID: Int = dialogueMessageAPI
                .sendMessageWithPhotoInDialogue(photoMultipart, messageRequestBody)
                .execute()
                .body()?: -1
            if (messageID == -1) {
                throw SendMessageException()
            }
        } catch(e: Exception) {
            when(e) {
                is ConnectException, is SocketException, is SocketTimeoutException ->
                    throw SendMessageException()
            }
        }
    }

    override suspend fun sendAudioMessage(context: Context, audio: ByteArray?, messengerID: Int) {
        val accountData = Account(context)
        val message = DMessageEntity(
            id = -1,
            "Аудиосообщение",
            Date().time,
            accountData.id,
            messengerID,
            accountData.name?: "",
            accountData.surname?: "",
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
                dialogueMessageAPI
                    .sendAudioMessageInDialogue(audioMultipart, messageRequestBody)
                    .execute()
                    .body()?: -1
            if (messageID == -1) {
                throw SendMessageException()
            }
        } catch(e: SocketException) {
            when(e) {
                is ConnectException, is SocketTimeoutException, is SocketException ->
                    throw SendMessageException()
            }
        }
    }

    override suspend fun sendMessageWithFile(context: Context, addition: Addition, messengerID: Int) {
        val accountData = Account(context)
        val message = DMessageEntity(
            id = -1,
            addition.name,
            Date().time,
            accountData.id,
            messengerID,
            accountData.name?: "",
            accountData.surname?: "",
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
                dialogueMessageAPI
                    .sendMessageWithFileInDialogue(fileMultipart, messageRequestBody)
                    .execute()
                    .body()?: -1
            if (messageID == -1) {
                throw SendMessageException()
            }
        } catch(e: Exception) {
            when(e) {
                is ConnectException, is SocketException, is SocketTimeoutException ->
                    throw SendMessageException()
            }
        }
    }
}