package com.example.conference.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.conference.db.dao.*
import com.example.conference.db.entity.*

@Database(
    entities = [
        ContactEntity::class,
        ConferenceEntity::class,
        DialogueEntity::class,
        CMessageEntity::class,
        DMessageEntity::class,
        MeetChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ConferenceRoomDatabase: RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun conferenceDao(): ConferenceDao
    abstract fun dialogueDao(): DialogueDao
    abstract fun cMessageDao(): CMessageDao
    abstract fun dMessageDao(): DMessageDao
    abstract fun meetChatMessageDao(): MeetChatMessagesDao

    companion object{
        @Volatile
        private var INSTANCE: ConferenceRoomDatabase? = null

        fun getDatabase(context: Context): ConferenceRoomDatabase {

            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {

                val instance = Room.databaseBuilder(context, ConferenceRoomDatabase::class.java, "conference_database").build()
                INSTANCE = instance
                return instance
            }
        }
    }
}