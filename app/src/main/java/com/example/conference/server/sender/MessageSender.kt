package com.example.conference.server.sender

import android.content.Context
import com.example.conference.file.Addition

abstract class MessageSender {
    abstract suspend fun sendTextMessage(
        context: Context,
        messageText: String,
        messengerID: Int
    )

    abstract suspend fun sendMessageWithPhoto(
        context: Context,
        photo: ByteArray?,
        messageText: String,
        messengerID: Int
    )

    open suspend fun sendAudioMessage(
        context: Context,
        audio: ByteArray?,
        messengerID: Int
    ) {}

    open suspend fun sendMessageWithFile(
        context: Context,
        addition: Addition,
        messengerID: Int
    ) {}
}