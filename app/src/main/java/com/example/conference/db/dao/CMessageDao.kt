package com.example.conference.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.conference.db.entity.CMessageEntity
import com.example.conference.db.entity.CMessageMinimal

@Dao
interface CMessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(m: CMessageEntity)

    @Query("SELECT * FROM conference_messages_table WHERE conference_id = :id ORDER BY date_time DESC")
    suspend fun getMessages(id: Int): List<CMessageEntity>

    @Query("SELECT COUNT(*) FROM conference_messages_table WHERE conference_id = :id")
    suspend fun getMessagesCount(id: Int): Int

    @Query("SELECT MAX(id) FROM conference_messages_table WHERE conference_id = :conference_id")
    suspend fun getLastMessageID(conference_id: Int): Int

    @Query("SELECT text, date_time FROM conference_messages_table WHERE id = (SELECT MAX(id) FROM conference_messages_table WHERE conference_id = :conference_id)")
    suspend fun getLastMessage(conference_id: Int): CMessageMinimal
}