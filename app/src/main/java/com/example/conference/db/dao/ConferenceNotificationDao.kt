package com.example.conference.db.dao

import androidx.room.*
import com.example.conference.db.entity.ConferenceNotificationEntity

@Dao
interface ConferenceNotificationDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(notification: ConferenceNotificationEntity)

    @Query("SELECT notification FROM CONFERENCE_NOTIFICATION WHERE conference_id = :id")
    suspend fun getNotification(id: Int): List<Int>

    @Update
    suspend fun update(entity: ConferenceNotificationEntity)
}