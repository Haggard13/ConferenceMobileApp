package com.example.conference.server.users

import com.example.conference.db.entity.ContactEntity
import com.example.conference.exception.UserFindingException
import com.example.conference.exception.UserNotFoundException
import com.example.conference.server.conferenceapi.ConferenceAPIProvider
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

class UsersManager {
    private val conferenceAPI =  ConferenceAPIProvider.conferenceAPI

    fun findUser(email: String): ContactEntity {
        try {
            val response = conferenceAPI.getUserInfo(email).execute()

            if (!response.isSuccessful) {
                throw UserNotFoundException()
            }

            return response.body() ?: throw UserNotFoundException()
        } catch (e: Exception) {
            when(e) {
                is SocketException, is ConnectException, is SocketTimeoutException ->
                    throw UserFindingException()
                else ->
                    throw e
            }
        }
    }
}