package com.example.conference.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conference_notification")
data class ConferenceNotificationEntity(
    @PrimaryKey
    val conference_id: Int,
    val notification: Int = 1
)
