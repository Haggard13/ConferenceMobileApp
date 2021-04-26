package com.example.conference.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dialogues_table")
data class DialogueEntity (
    @PrimaryKey
    var id: Int,

    var second_user_id: Int,
    var second_user_email: String,
    var second_user_name: String,
    var second_user_surname: String,
    var last_message: String,
    var last_message_time: Long
)
