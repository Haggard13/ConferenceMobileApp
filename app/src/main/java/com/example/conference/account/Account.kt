package com.example.conference.account

import android.content.Context
import android.content.SharedPreferences

class Account(
    context: Context,
    sharedPreferences: SharedPreferences = context.getSharedPreferences(
    "user_info",
    Context.MODE_PRIVATE)
) {
    val id = sharedPreferences.getInt("user_id", 0)
    val name = sharedPreferences.getString("user_name", "")
    val surname = sharedPreferences.getString("user_surname", "")
    val email = sharedPreferences.getString("user_email", "")
}