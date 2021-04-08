package com.example.conference.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.conference.adapter.MeetChatRecyclerViewAdapter
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.entity.MeetChatMessageEntity
import com.example.conference.json.CMessageList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MeetChatViewModel(private val app: Application) : AndroidViewModel(app) {

    private val meetChatMessagesDao = ConferenceRoomDatabase.getDatabase(app).meetChatMessageDao()
    var conferenceID: Int = 0
    var recyclerViewAdapter: MeetChatRecyclerViewAdapter? = null


    suspend fun getLastMessageID(): Int =
        if (meetChatMessagesDao.getMessagesCount(conferenceID) == 0)
            0
        else
            meetChatMessagesDao.getLastMessageID(conferenceID)

    suspend fun getMessagesFromLocalDataBase() =
        CMessageList(meetChatMessagesDao.getMessages(conferenceID))

    suspend fun updateMessageRecyclerView(newMessages: CMessageList) {
        if (newMessages.list == null) return
        newMessages.list.forEach {
            val message = MeetChatMessageEntity(
                it.id, it.text, it.date_time, it.sender_id, it.conference_id, it.sender_name,
                it.sender_surname, it.sender_enum, it.type
            )
            meetChatMessagesDao.insert(message)
        }
        recyclerViewAdapter?.messages?.list = meetChatMessagesDao.getMessages(conferenceID)
        withContext(Dispatchers.Main) {
            recyclerViewAdapter?.notifyDataSetChanged()
        }
    }
}