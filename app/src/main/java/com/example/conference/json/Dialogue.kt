package com.example.conference.json

data class Dialogue(
    var first_user_id: Int,
    var second_user_id: Int,
    var first_user_email: String,
    var second_user_email: String,
    var first_user_name: String,
    var second_user_name: String,
    var first_user_surname: String,
    var second_user_surname: String
)