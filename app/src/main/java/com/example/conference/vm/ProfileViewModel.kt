package com.example.conference.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.dao.ContactDao

class ProfileViewModel(val app: Application): AndroidViewModel(app) {
    private val contactDao: ContactDao = ConferenceRoomDatabase.getDatabase(app).contactDao()
    var contactsCount = 0

    suspend fun getAllContacts() = contactDao.getAll()
    suspend fun deleteContact(email: String) = contactDao.deleteCortege(email)
    suspend fun getContactsCount() = contactDao.count()

    suspend fun databaseClear() =
        ConferenceRoomDatabase.getDatabase(app).clearAllTables()

    suspend fun updateContactsCount() =
        apply { contactsCount = getContactsCount() }
}