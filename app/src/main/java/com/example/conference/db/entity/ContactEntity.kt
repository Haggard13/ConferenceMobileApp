package com.example.conference.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_table")
data class ContactEntity (
    @PrimaryKey
    var email: String,

    var name: String,
    var surname: String
    )