package com.example.conference.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conferences_table")
data class ConferenceEntity(
    @PrimaryKey
    var id: Int,

    var name: String,
    var count: Int,
    var last_message: String,
    var last_message_time: Long,
)