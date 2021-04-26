package com.example.conference.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.db.entity.ConferenceNotificationEntity
import kotlin.properties.Delegates

class ConferenceSettingsViewModel(val app: Application): AndroidViewModel(app) {

    private val conferenceDao = ConferenceRoomDatabase.getDatabase(app).conferenceDao()
    private val conferenceNotificationDao =
        ConferenceRoomDatabase.getDatabase(app).conferenceNotificationDao()
    lateinit var conference: ConferenceEntity
    var conferenceID by Delegates.notNull<Int>()

    suspend fun initConference() {
        conference = conferenceDao.getConference(conferenceID)[0]
    }

    suspend fun renameConference(name: String) {
        conference.name = name
        conferenceDao.update(conference)
    }

    suspend fun getNotification() = conferenceNotificationDao.getNotification(conferenceID)[0]

    suspend fun changeNotification(n: Int) =
        conferenceNotificationDao.update(ConferenceNotificationEntity(conferenceID, n))

}