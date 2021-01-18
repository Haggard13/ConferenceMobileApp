package com.example.conference.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.db.entity.DialogueEntity

@Dao
interface DialogueDao {
    @Insert
    suspend fun insert(d: DialogueEntity)

    @Query("SELECT * FROM dialogues_table WHERE id = :id")
    suspend fun getDialogue(id: Int) : DialogueEntity

    @Query("SELECT * FROM dialogues_table")
    suspend fun getAll() : List<DialogueEntity>

    @Query("SELECT COUNT(*) FROM dialogues_table")
    suspend fun count() : Int

    @Query("SELECT MAX(id) FROM dialogues_table")
    suspend fun getLastID() : Int
}