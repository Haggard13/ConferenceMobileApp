package com.example.conference.server.websocket.dto

data class Answer(val id: String, val conferenceId: Int, val answererId: Int, val userId: Int, val answer: String)