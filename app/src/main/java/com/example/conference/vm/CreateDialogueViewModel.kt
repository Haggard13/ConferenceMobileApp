package com.example.conference.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.conference.account.Account
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.dao.ContactDao
import com.example.conference.db.entity.ContactEntity

class CreateDialogueViewModel(app: Application): AndroidViewModel(app) {

    val account = Account(app)
    private val contactDao: ContactDao = ConferenceRoomDatabase
        .getDatabase(app)
        .contactDao()

    suspend fun getAllContacts(): List<ContactEntity> = contactDao.getAll()
}