package com.example.conference.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.conference.adapter.DialogueRecyclerViewAdapter
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.entity.DMessageEntity
import com.example.conference.db.entity.DialogueEntity
import kotlin.properties.Delegates

class DialogueViewModel(val app: Application) : AndroidViewModel(app) {

    private val dialogueDao = ConferenceRoomDatabase.getDatabase(app).dialogueDao()
    private val dMessageDao = ConferenceRoomDatabase.getDatabase(app).dMessageDao()
    lateinit var adapter: DialogueRecyclerViewAdapter
    var dialogueID by Delegates.notNull<Int>()

    suspend fun getDialogue(): DialogueEntity = dialogueDao.getDialogue(dialogueID)[0]

    suspend fun getLastMessageID() =
        if (dMessageDao.getMessagesCount(dialogueID) != 0)
            dMessageDao.getLastMessageID(dialogueID)
        else
            0

    suspend fun saveMessageInDataBase(message: DMessageEntity) =
        dMessageDao.insert(message)


    suspend fun getMessages(): List<DMessageEntity> =
        dMessageDao.getMessages(dialogueID)
}