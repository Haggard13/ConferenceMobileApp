package com.example.conference.server.api

import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.json.ContactEntityWithStatus
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.webrtc.SessionDescription
import retrofit2.Call
import retrofit2.http.*

interface ConferenceAPI {

    @GET("/conference/get_all_conferences")
    fun getAllConferences(@Query("user_id") userID: Int): Call<List<ConferenceEntity>>

    @Multipart
    @POST("/conference/create_new_conference")
    fun createNewConference(
        @Part("conference") conference: RequestBody,
        @Part("members") members: RequestBody
    ): Call<Boolean>

    @GET("/conference/get_conference_members")
    fun getConferenceMembers(
        @Query("conference_id") conferenceID: Int
    ): Call<List<ContactEntityWithStatus>>

    @POST("/conference/{conference_id}/add_user/{member_id}/{user_id}")
    fun addUserInConference(
        @Path("conference_id") conferenceID: Int,
        @Path("member_id") memberID: Int,
        @Path("user_id") userID: Int
    ): Call<Boolean>

    @POST("/conference/{conference_id}/delete_user/{member_id}/{user_id}")
    fun deleteUserFromConference(
        @Path("conference_id") conferenceID: Int,
        @Path("member_id") memberID: Int,
        @Path("user_id") userID: Int
    ): Call<Boolean>

    @Multipart
    @POST("/conference/avatar/upload")
    fun changeConferenceAvatar(@Part image: MultipartBody.Part): Call<Boolean>

    @POST("/conference/rename/{id}/{name}")
    fun renameConference(
        @Path("id") id: Int,
        @Path("name") name: String
    ): Call<Boolean>

    @POST("/offer")
    fun offer(@Body a: String): Call<Boolean>
}