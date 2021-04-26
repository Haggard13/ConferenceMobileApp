package com.example.conference.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.db.entity.ConferenceNotificationEntity

class ConferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val conferenceDao = ConferenceRoomDatabase.getDatabase(application).conferenceDao()
    private val conferenceNotificationDao =
        ConferenceRoomDatabase.getDatabase(application).conferenceNotificationDao()

    suspend fun getConferences(): List<ConferenceEntity> = conferenceDao.getAll()
    suspend fun addConference(c: ConferenceEntity) = conferenceDao.insert(c)
    suspend fun addConferenceNotification(id: Int) =
        conferenceNotificationDao.insert(ConferenceNotificationEntity(id))
}