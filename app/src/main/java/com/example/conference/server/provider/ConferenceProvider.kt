package com.example.conference.server.provider

import android.content.Context
import com.example.conference.account.Account
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.exception.*
import com.example.conference.file.Addition
import com.example.conference.json.ConferenceMember
import com.example.conference.json.ContactEntityWithStatus
import com.example.conference.server.api.ConferenceAPI
import com.example.conference.server.api.ConferenceAPIProvider
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

class ConferenceProvider {
    private val conferenceAPI: ConferenceAPI = ConferenceAPIProvider.conferenceAPI

    fun getAllConferences(context: Context): List<ConferenceEntity> {
        try {
            val account = Account(context)
            return conferenceAPI
                .getAllConferences(account.id)
                .execute()
                .body() ?: ArrayList()
        } catch (e: Exception) {
            when (e) {
                is ConnectException, is SocketTimeoutException, is SocketException ->
                    throw ConferencesGettingException()
                else ->
                    throw e
            }
        }
    }

    fun createNewConference(conference: ConferenceEntity, members: List<ConferenceMember>): Boolean {
        try {
            val conferenceRequestBody: RequestBody =
                Gson()
                    .toJson(conference)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            val membersRequestBody: RequestBody =
                Gson()
                    .toJson(members)
                    .toRequestBody("application/json".toMediaTypeOrNull())
            return conferenceAPI
                .createNewConference(conferenceRequestBody, membersRequestBody)
                .execute()
                .body() ?: false
        } catch (e: Exception) {
            when (e) {
                is ConnectException, is SocketTimeoutException, is SocketException ->
                    throw CreateConferenceException()
                else ->
                    throw e
            }
        }
    }

    fun getConferenceMembers(conferenceID: Int): List<ContactEntityWithStatus> {
        try {
            return conferenceAPI
                .getConferenceMembers(conferenceID)
                .execute()
                .body() ?: ArrayList()
        } catch(e: Exception) {
            when (e) {
                is ConnectException, is SocketTimeoutException, is SocketException ->
                    throw LoadConferenceMembersException()
                else ->
                    throw e
            }
        }
    }

    fun addUserInConference(conferenceID: Int, memberID: Int, userID: Int): Boolean {
        try {
            return conferenceAPI
                .addUserInConference(conferenceID, memberID, userID)
                .execute()
                .body() ?: false
        } catch (e: Exception) {
            when(e) {
                is ConnectException, is SocketTimeoutException, is SocketException ->
                    throw AddConferenceMemberException()
                else ->
                    throw e
            }
        }
    }

    fun deleteUserFromConference(conferenceID: Int, memberID: Int, userID: Int): Boolean {
        try {
            return conferenceAPI
                .deleteUserFromConference(conferenceID, memberID, userID)
                .execute()
                .body() ?: false
        } catch (e: Exception) {
            when(e) {
                is ConnectException, is SocketTimeoutException, is SocketException ->
                    throw DeleteUserException()
                else ->
                    throw e
            }
        }
    }

    fun changeConferenceAvatar(image: Addition): Boolean {
        try {
            val file: MultipartBody.Part =
                MultipartBody.Part
                    .createFormData(
                        "file",
                        "${image.name}.png",
                        image.file.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                    )
            return conferenceAPI
                .changeConferenceAvatar(file)
                .execute()
                .body() ?: false
        } catch (e: Exception) {
            when(e) {
                is ConnectException, is SocketTimeoutException, is SocketException ->
                    throw LoadImageException()
                else ->
                    throw e
            }
        }
    }

    fun renameConference(id: Int, name: String): Boolean {
        try {
            return conferenceAPI
                .renameConference(id, name)
                .execute()
                .body() ?: false
        } catch (e: Exception) {
            when(e) {
                is ConnectException, is SocketTimeoutException, is SocketException ->
                    throw ConferenceRenameException()
                else ->
                    throw e
            }
        }
    }
}
