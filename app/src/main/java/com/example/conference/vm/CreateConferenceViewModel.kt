package com.example.conference.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.entity.ContactEntity

class CreateConferenceViewModel(application: Application) : AndroidViewModel(application) {
    private val contactDao = ConferenceRoomDatabase.getDatabase(application).contactDao()
    suspend fun getContacts(): List<ContactEntity> = contactDao.getAll()
}