package com.example.conference.server.api

import com.example.conference.db.entity.DialogueEntity
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface DialogueAPI {

    @GET("/dialogue/get_all_dialogues")
    fun getAllDialogues(@Query("user_id") userID: Int): Call<List<DialogueEntity>>

    @Multipart
    @POST("/dialogue/create_new_dialogue")
    fun createNewDialogue(
        @Part("dialogue") dialogue: RequestBody,
        @Part("account") user: RequestBody
    ): Call<Boolean>
}
