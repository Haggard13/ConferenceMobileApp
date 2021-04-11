package com.example.conference.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.dao.ContactDao
import com.example.conference.db.entity.ContactEntity

class AddContactViewModel(app: Application): AndroidViewModel(app) {
    private val contactDao: ContactDao = ConferenceRoomDatabase.getDatabase(app).contactDao()

    suspend infix fun addContact(c: ContactEntity) = contactDao.insert(c)
    suspend infix fun existContact(email: String): Boolean = contactDao.existContact(email) > 0
}