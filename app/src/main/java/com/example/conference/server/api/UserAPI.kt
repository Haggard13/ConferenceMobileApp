package com.example.conference.server.api

import com.example.conference.db.entity.ContactEntity
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface UserAPI {

    @POST("/user/send_firebase_messaging_token")
    fun sendFirebaseMessagingToken(@Body tokenWithID: String): Call<ResponseBody>

    @GET("/user/get_user_info")
    fun getUserInfo(@Query("email") email: String): Call<ContactEntity>

    @Multipart
    @POST("/user/avatar/upload")
    fun changeUserAvatar(@Part photo: MultipartBody.Part): Call<ResponseBody>
}
