package com.example.conference.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.entity.ConferenceEntity

class ConferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val conferenceDao = ConferenceRoomDatabase.getDatabase(application).conferenceDao()

    suspend fun getConferences(): List<ConferenceEntity> = conferenceDao.getAll()
    suspend fun addConference(c: ConferenceEntity) = conferenceDao.insert(c)
}