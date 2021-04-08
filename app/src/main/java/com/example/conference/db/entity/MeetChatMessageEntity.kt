package com.example.conference.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meet_chat_messages_table")
data class MeetChatMessageEntity(
    @PrimaryKey
    var id: Int,

    var text: String,
    var date_time: Long,
    var sender_id: Int,
    var conference_id: Int,
    var sender_name: String,
    var sender_surname: String,
    var sender_enum: Int,
    var type: Int
)
