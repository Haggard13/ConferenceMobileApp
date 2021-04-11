package com.example.conference.server.conferenceapi

import com.example.conference.db.entity.CMessageEntity
import com.example.conference.db.entity.ContactEntity
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ConferenceAPI {

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

    @GET("/conference/get_new_messages")
    fun getNewConferenceMessages(
        @Query("conference_id") conferenceID: Int,
        @Query("last_message_id") lastMessageID: Int,
        @Query("user_id") userID: Int
    ): Call<List<CMessageEntity>>

    @POST("/user/send_firebase_messaging_token")
    fun sendFirebaseMessagingToken(@Body token: String): Call<ResponseBody>

    @GET("/user/get_user_info")
    fun getUserInfo(@Query("email") email: String): Call<ContactEntity>
}