package com.example.conference.server.api

import com.example.conference.db.entity.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ConferenceAPI {

    //region Conference Messaging
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
    //endregion

    //region Dialogue Messaging
    @POST("/dialogue/send_message")
    fun sendTextMessageInDialogue(@Body message: DMessageEntity): Call<Int>

    @Multipart
    @POST("/dialogue/send_message_with_photo")
    fun sendMessageWithPhotoInDialogue(
        @Part photo: MultipartBody.Part,
        @Part("message") message: RequestBody
    ): Call<Int>

    @Multipart
    @POST("/dialogue/send_audio_message")
    fun sendAudioMessageInDialogue(
        @Part audioMessage: MultipartBody.Part,
        @Part("message") message: RequestBody
    ): Call<Int>

    @Multipart
    @POST("/dialogue/send_message_with_file")
    fun sendMessageWithFileInDialogue(
        @Part file: MultipartBody.Part,
        @Part("message") message: RequestBody
    ): Call<Int>

    /**
     * Возвращает сообщения, которых еще нет на устройстве пользователя
     */
    @GET("/dialogue/get_new_messages")
    fun getNewDialogueMessages(
        @Query("dialogue_id") dialogueID: Int,
        @Query("last_message_id") lastMessageID: Int,
        @Query("user_id") userID: Int
    ): Call<List<DMessageEntity>>

    @GET("/dialogue/check_new_messages/")
    fun checkNewDialogueMessages(
        @Query("dialogue_id") dialogueID: Int,
        @Query("last_message_id") lastMessageID: Int
    ): Call<Boolean>
    //endregion

    //region Users
    @POST("/user/send_firebase_messaging_token")
    fun sendFirebaseMessagingToken(@Body tokenWithID: String): Call<ResponseBody>

    @GET("/user/get_user_info")
    fun getUserInfo(@Query("email") email: String): Call<ContactEntity>

    @Multipart
    @POST("/user/avatar/upload")
    fun changeUserAvatar(@Part photo: MultipartBody.Part): Call<ResponseBody>
    //endregion

    @GET("/conference/get_all_conferences")
    fun getAllConferences(@Query("user_id") userID: Int): Call<List<ConferenceEntity>>

    @GET("/dialogue/get_all_dialogues")
    fun getAllDialogues(@Query("user_id") userID: Int): Call<List<DialogueEntity>>

    @Multipart
    @POST("/conference/create_new_conference")
    fun createNewConference(@Part("conference") conference: RequestBody,
                            @Part("members") members: RequestBody
    ): Call<Boolean>
}