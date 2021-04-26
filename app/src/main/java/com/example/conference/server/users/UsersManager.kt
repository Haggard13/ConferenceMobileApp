package com.example.conference.server.users

import com.example.conference.db.entity.ContactEntity
import com.example.conference.exception.ChangeAvatarException
import com.example.conference.exception.UserFindingException
import com.example.conference.exception.UserNotFoundException
import com.example.conference.file.Addition
import com.example.conference.server.api.ConferenceAPIProvider
import com.example.conference.server.api.UserAPI
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

class UsersManager {
    private val userAPI: UserAPI = ConferenceAPIProvider.userAPI

    fun findUser(email: String): ContactEntity {
        try {
            val response = userAPI.getUserInfo(email).execute()

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

    fun changeUserAvatar(photo: Addition) {
        try {
            val photoRequestBody: MultipartBody.Part = MultipartBody.Part.createFormData(
                "file" , "${photo.name}.png",
                    photo.file.toRequestBody("application/octet-stream".toMediaTypeOrNull(), 0, photo.file.size)
                )
            userAPI
                .changeUserAvatar(photoRequestBody)
                .execute()
        } catch (e: Exception) {
            when(e) {
                is SocketException, is ConnectException, is SocketTimeoutException ->
                    throw ChangeAvatarException()
                else ->
                    throw e
            }
        }
    }
}