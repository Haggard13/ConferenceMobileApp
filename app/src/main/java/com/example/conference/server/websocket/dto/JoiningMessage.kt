package com.example.conference.server.websocket.dto

data class JoiningMessage(val id: String, val conferenceId: Int, val userId: Int)
