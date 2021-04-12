package com.example.conference.server.sender

import android.content.Context
import com.example.conference.file.Addition

abstract class MessageSender {
    abstract fun sendTextMessage(
        context: Context,
        messageText: String,
        messengerID: Int
    )

    abstract fun sendMessageWithPhoto(
        context: Context,
        photo: ByteArray?,
        messageText: String,
        messengerID: Int
    )

    open fun sendAudioMessage(
        context: Context,
        audio: ByteArray?,
        messengerID: Int
    ) {}

    open fun sendMessageWithFile(
        context: Context,
        addition: Addition,
        messengerID: Int
    ) {}
}