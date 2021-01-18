package com.example.conference.db.dao

import android.provider.ContactsContract
import androidx.room.*
import com.example.conference.db.entity.ContactEntity

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(ce: ContactEntity)

    @Query("SELECT * FROM contact_table")
    suspend fun getAll(): List<ContactEntity>

    @Query("DELETE FROM contact_table WHERE email=:email")
    suspend fun deleteCortege(email: String)

    @Query("SELECT COUNT(*) FROM contact_table")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM contact_table WHERE email = :email")
    suspend fun existContact(email: String): Int
}