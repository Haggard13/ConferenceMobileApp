package com.example.conference.server.sender

import android.content.Context
import com.example.conference.file.Addition

abstract class MessageSender {
    abstract fun sendTextMessage(
        context: Context,
        messageText: String,
        conferenceID: Int
    )

    abstract fun sendMessageWithPhoto(
        context: Context,
        photo: ByteArray?,
        messageText: String,
        conferenceID: Int
    )

    open fun sendAudioMessage(
        context: Context,
        audio: ByteArray?,
        conferenceID: Int
    ) {}

    open fun sendMessageWithFile(
        context: Context,
        addition: Addition,
        conferenceID: Int
    ) {}
}