package com.example.conference.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.conference.db.entity.ConferenceEntity

@Dao
interface ConferenceDao {
    @Insert
    suspend fun insert(c: ConferenceEntity): Long

    @Query("SELECT * FROM conferences_table WHERE id = :id")
    suspend fun getConference(id: Int) : List<ConferenceEntity>

    @Query("SELECT * FROM conferences_table")
    suspend fun getAll() : List<ConferenceEntity>

    @Query("SELECT COUNT(*) FROM conferences_table ")
    suspend fun count() : Int

    @Query("SELECT MAX(id) FROM conferences_table")
    suspend fun getLastID() : Int


    @Update
    suspend fun update(c: ConferenceEntity)
}