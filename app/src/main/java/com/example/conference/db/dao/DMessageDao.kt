package com.example.conference.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.conference.db.entity.DMessageEntity

@Dao
interface DMessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(m: DMessageEntity)

    @Query("SELECT * FROM dialogue_messages_table WHERE dialogue_id = :id ORDER BY date_time DESC")
    suspend fun getMessages(id: Int): List<DMessageEntity>

    @Query("SELECT MAX(id) FROM dialogue_messages_table WHERE dialogue_id = :dialogue_id")
    suspend fun getLastMessageID(dialogue_id: Int): Int

    @Query("SELECT COUNT(*) FROM dialogue_messages_table WHERE dialogue_id = :dialogue_id")
    suspend fun getMessagesCount(dialogue_id: Int): Int
}