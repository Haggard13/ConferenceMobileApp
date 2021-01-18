package com.example.conference.vm

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.conference.adapter.ConferencesRecyclerViewAdapter
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.entity.CMessageMinimal
import com.example.conference.db.entity.ConferenceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val conferenceDao = ConferenceRoomDatabase.getDatabase(application).conferenceDao()
    private val cMessageDao = ConferenceRoomDatabase.getDatabase(application).cMessageDao()
    lateinit var adapter: ConferencesRecyclerViewAdapter

    suspend fun getConferences(): List<ConferenceEntity> = conferenceDao.getAll()
    suspend fun conferenceCount() = conferenceDao.count()
    suspend fun getLastID() = if (conferenceDao.count() != 0) conferenceDao.getLastID() else 0
    suspend fun addConference(c: ConferenceEntity) = conferenceDao.insert(c)

    private suspend fun getLastMessage(id: Int) =
        if (getMessageCount(id) == 0)
            CMessageMinimal("Вас добавили в конференцию", 0.toLong())
        else {
            cMessageDao.getLastMessage(id)
        }

    private suspend fun getMessageCount(id: Int) = cMessageDao.getMessagesCount(id)

    suspend fun showToast(context: Context, text: String) =
        withContext(Dispatchers.Main) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }

    suspend fun getLastMessages(confs: List<ConferenceEntity>): HashMap<Int, CMessageMinimal> {
        val lastMessages = HashMap<Int, CMessageMinimal>()
        confs.forEach {
            lastMessages[it.id] = getLastMessage(it.id)
        }
        return lastMessages
    }

    fun adapterIsInitialized() = ::adapter.isInitialized

    suspend fun updateAdapterConferences() {
        adapter.conferences = getConferences()
    }

    suspend fun updateAdapterLastMessages() {
        adapter.lastMessages = getLastMessages(adapter.conferences)
    }
}