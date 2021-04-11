package com.example.conference.db.data

enum class MessageType(val type: Int) {
    TEXT_MESSAGE(1),
    MESSAGE_WITH_PHOTO(2),
    AUDIO_MESSAGE(3),
    MESSAGE_WITH_FILE(4)
}