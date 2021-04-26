package com.example.conference.server.api

import com.example.conference.db.entity.CMessageEntity
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ConferenceMessageAPI {

    @POST("/conference/send_message")
    fun sendTextMessageInConference(@Body message: CMessageEntity): Call<Int>

    @Multipart
    @POST("/conference/send_message_with_photo")
    fun sendMessageWithPhotoInConference(
        @Part photo: MultipartBody.Part,
        @Part("message") message: RequestBody
    ): Call<Int>

    @Multipart
    @POST("/conference/send_audio_message")
    fun sendAudioMessageInConference(
        @Part audioMessage: MultipartBody.Part,
        @Part("message") message: RequestBody
    ): Call<Int>

    @Multipart
    @POST("/conference/send_message_with_file")
    fun sendMessageWithFileInConference(
        @Part file: MultipartBody.Part,
        @Part("message") message: RequestBody
    ): Call<Int>

    /**
     * Возвращает сообщения, которых еще нет на устройстве пользователя
     */
    @GET("/conference/get_new_messages")
    fun getNewConferenceMessages(
        @Query("conference_id") conferenceID: Int,
        @Query("last_message_id") lastMessageID: Int,
        @Query("user_id") userID: Int
    ): Call<List<CMessageEntity>>

    @GET("/conference/check_new_messages/")
    fun checkNewConferenceMessages(
        @Query("conference_id") conferenceID: Int,
        @Query("last_message_id") lastMessageID: Int
    ): Call<Boolean>
}
