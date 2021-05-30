package com.example.conference.server.websocket.dto

data class Offer(
    val id: String,
    val conferenceId: Int,
    val userId: Int,
    val senderId: Int,
    val offer: String
    )
