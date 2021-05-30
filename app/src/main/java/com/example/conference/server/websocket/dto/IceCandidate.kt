package com.example.conference.server.websocket.dto

data class IceCandidate(val id: String, val conferenceId: Int, val userId: Int, val senderId: Int, val iceCandidate: String)
