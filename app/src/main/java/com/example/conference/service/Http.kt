package com.example.conference.service

import com.example.conference.db.entity.CMessageEntity
import com.example.conference.db.entity.DMessageEntity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder


object Http {
    const val baseURL = "http://192.168.0.103:8082"

    fun get(url: String): Response {
        val client = OkHttpClient.Builder().connectionSpecs(
            listOf(
                ConnectionSpec.MODERN_TLS,
                ConnectionSpec.CLEARTEXT
            )
        ).build()
        val request = Request.Builder().url("$baseURL$url").build()
        return client.newCall(request).execute()
    }

    fun sendNewUserAvatar(user_id: Int, file: ByteArray): Int {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file" , "$user_id.png",
                file.toRequestBody("application/octet-stream".toMediaTypeOrNull(), 0, file.size)
            )
            .build()

        val request = Request.Builder()
            .url("$baseURL/user/avatar/upload")
            .post(requestBody)
            .build()
        val client = OkHttpClient.Builder().build()
        val response = client.newCall(request).execute()
        return if (!response.isSuccessful) {
            -1
        } else
            1
    }

    fun sendNewConferenceAvatar(conference_id: Int, file: ByteArray): Int {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file" , "$conference_id.png",
                file.toRequestBody("application/octet-stream".toMediaTypeOrNull(), 0, file.size)
            )
            .build()

        val request = Request.Builder()
            .url("$baseURL/conference/avatar/upload")
            .post(requestBody)
            .build()
        val client = OkHttpClient.Builder().build()
        val response = client.newCall(request).execute()
        return if (!response.isSuccessful) {
            -1
        } else
            1
    }

    fun sendConferenceMessagePhoto(
        file: ByteArray?,
        CMessageEntity: CMessageEntity
    ): Response {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file" , "file.png",
                file!!.toRequestBody("application/octet-stream".toMediaTypeOrNull(), 0, file.size)
            )
            .addFormDataPart("text", URLEncoder.encode(CMessageEntity.text, "UTF-8"))
            .build()

        val request = Request.Builder()
            .url(
                baseURL +
                    "/conference/sendPhotography" +
                    "/${CMessageEntity.conference_id}" +
                    "/${CMessageEntity.date_time}" +
                    "/${CMessageEntity.sender_id}" +
                    "/${CMessageEntity.sender_name}" +
                    "/${CMessageEntity.sender_surname}")
            .post(requestBody)
            .build()
        val client = OkHttpClient.Builder().build()
        return client.newCall(request).execute()
    }

    fun sendDialogueMessagePhoto(
        file: ByteArray?,
        DMessageEntity: DMessageEntity
    ): Response {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file" , "file.png",
                file!!.toRequestBody("application/octet-stream".toMediaTypeOrNull(), 0, file.size)
            )
            .build()

        val request = Request.Builder()
            .url(
                baseURL +
                    "/dialogue/sendPhotography" +
                    "/${URLEncoder.encode(DMessageEntity.text, "UTF-8")}" +
                    "/${DMessageEntity.dialogue_id}" +
                    "/${DMessageEntity.date_time}" +
                    "/${DMessageEntity.sender_id}" +
                    "/${DMessageEntity.sender_name}" +
                    "/${DMessageEntity.sender_surname}")
            .post(requestBody)
            .build()
        val client = OkHttpClient.Builder().build()
        return client.newCall(request).execute()
    }

    fun sendConferenceMessageAudio(
        file: ByteArray?,
        CMessageEntity: CMessageEntity
    ): Response {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file" , "file.3gp",
                file!!.toRequestBody("application/octet-stream".toMediaTypeOrNull(), 0, file.size)
            )
            .build()

        val request = Request.Builder()
            .url(
                baseURL +
                    "/conference/sendAudioMessage" +
                    "/${URLEncoder.encode(CMessageEntity.text, "UTF-8")}" +
                    "/${CMessageEntity.conference_id}" +
                    "/${CMessageEntity.date_time}" +
                    "/${CMessageEntity.sender_id}" +
                    "/${CMessageEntity.sender_name}" +
                    "/${CMessageEntity.sender_surname}")
            .post(requestBody)
            .build()
        val client = OkHttpClient.Builder().build()
        return client.newCall(request).execute()
    }

    fun sendDialogueMessageAudio(
        file: ByteArray?,
        DMessageEntity: DMessageEntity
    ): Response {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file" , "file.3gp",
                file!!.toRequestBody("application/octet-stream".toMediaTypeOrNull(), 0, file.size)
            )
            .build()

        val request = Request.Builder()
            .url(
                baseURL +
                        "/dialogue/sendAudioMessage" +
                        "/${URLEncoder.encode(DMessageEntity.text, "UTF-8")}" +
                        "/${DMessageEntity.dialogue_id}" +
                        "/${DMessageEntity.date_time}" +
                        "/${DMessageEntity.sender_id}" +
                        "/${DMessageEntity.sender_name}" +
                        "/${DMessageEntity.sender_surname}")
            .post(requestBody)
            .build()
        val client = OkHttpClient.Builder().build()
        return client.newCall(request).execute()
    }
}