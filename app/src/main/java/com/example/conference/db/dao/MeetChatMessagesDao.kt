package com.example.conference.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.conference.db.entity.CMessageEntity
import com.example.conference.db.entity.MeetChatMessageEntity

@Dao
interface MeetChatMessagesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(m: MeetChatMessageEntity)

    @Query("SELECT * FROM meet_chat_messages_table WHERE conference_id = :id ORDER BY date_time DESC")
    suspend fun getMessages(id: Int): List<CMessageEntity>

    @Query("SELECT COUNT(*) FROM meet_chat_messages_table WHERE conference_id = :id")
    suspend fun getMessagesCount(id: Int): Int

    @Query("SELECT MAX(id) FROM meet_chat_messages_table WHERE conference_id = :conference_id")
    suspend fun getLastMessageID(conference_id: Int): Int
}