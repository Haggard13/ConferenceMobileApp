package com.example.conference.server.api

import com.example.conference.db.entity.DMessageEntity
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface DialogueMessageAPI {

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
}