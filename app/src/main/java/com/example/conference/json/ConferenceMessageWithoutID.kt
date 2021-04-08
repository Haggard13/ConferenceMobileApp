package com.example.conference.json

data class ConferenceMessageWithoutID (
    val text: String,
    val date_time: Long,
    val sender_id: Int,
    val conference_id: Int,
    val sender_name: String,
    val sender_surname: String,
    val sender_enum: Int,
    val type: Int
)