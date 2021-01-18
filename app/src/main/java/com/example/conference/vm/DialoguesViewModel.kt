package com.example.conference.vm

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.conference.adapter.DialoguesRecyclerViewAdapter
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.entity.CMessageMinimal
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.db.entity.DMessageMinimal
import com.example.conference.db.entity.DialogueEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DialoguesViewModel(application: Application) : AndroidViewModel(application) {
    private val dialogueDao = ConferenceRoomDatabase.getDatabase(application).dialogueDao()
    private val dMessageDao = ConferenceRoomDatabase.getDatabase(application).dMessageDao()
    lateinit var adapter: DialoguesRecyclerViewAdapter

    suspend fun getDialogues(): List<DialogueEntity> = dialogueDao.getAll()
    suspend fun dialogueCount() = dialogueDao.count()
    suspend fun getLastID() = if (dialogueDao.count() != 0) dialogueDao.getLastID() else 0
    suspend fun addDialogue(d: DialogueEntity) = dialogueDao.insert(d)

    private suspend fun getLastMessage(id: Int) =
        if (getMessageCount(id) == 0)
            DMessageMinimal("Диалог создан", 0.toLong())
        else {
            dMessageDao.getLastMessage(id)
        }

    suspend fun getLastMessages(confs: List<DialogueEntity>): HashMap<Int, DMessageMinimal> {
        val lastMessages = HashMap<Int, DMessageMinimal>()
        confs.forEach {
            lastMessages[it.id] = getLastMessage(it.id)
        }
        return lastMessages
    }

    private suspend fun getMessageCount(id: Int) = dMessageDao.getMessagesCount(id)

    suspend fun showToast(context: Context, text: String) =
        withContext(Dispatchers.Main) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }

    suspend fun updateAdapterDialogues() {
        adapter.dialogues = getDialogues()
    }

    suspend fun updateAdapterLastMessages() {
        adapter.lastMessages = getLastMessages(adapter.dialogues)
    }
}