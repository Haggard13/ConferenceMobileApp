package com.example.conference.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.conference.adapter.ConferenceRecyclerViewAdapter
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.entity.CMessageEntity
import com.example.conference.db.entity.ConferenceEntity
import kotlin.properties.Delegates

class ConferenceViewModel(val app: Application) : AndroidViewModel(app) {

    private val conferenceDao = ConferenceRoomDatabase.getDatabase(app).conferenceDao()
    private val cMessageDao = ConferenceRoomDatabase.getDatabase(app).cMessageDao()
    lateinit var adapter: ConferenceRecyclerViewAdapter
    var conferenceID by Delegates.notNull<Int>()

    suspend fun getConference(): ConferenceEntity = conferenceDao.getConference(conferenceID)[0]

    suspend fun getMessages(): List<CMessageEntity> = cMessageDao.getMessages(conferenceID)

    suspend fun getLastMessageID() =
        if (cMessageDao.getMessagesCount(conferenceID) != 0)
            cMessageDao.getLastMessageID(conferenceID)
        else
            0

    suspend fun updateConferenceData(c: ConferenceEntity) = conferenceDao.update(c)

    suspend fun saveMessageInDataBase(cMessageEntity: CMessageEntity) =
        cMessageDao.insert(cMessageEntity)

}