package com.example.conference.account

import android.content.Context

class Account(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(
        "user_info",
        Context.MODE_PRIVATE
    )

    val userID = sharedPreferences.getInt("user_id", 0)
    val userName = sharedPreferences.getString("user_name", "")
    val userSurname = sharedPreferences.getString("user_surname", "")
    val userEmail = sharedPreferences.getString("user_email", "")
}