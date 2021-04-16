package com.example.conference.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.entity.DialogueEntity

class DialoguesViewModel(application: Application) : AndroidViewModel(application) {
    private val dialogueDao = ConferenceRoomDatabase.getDatabase(application).dialogueDao()

    suspend fun getDialogues(): List<DialogueEntity> = dialogueDao.getAll()
    suspend fun addDialogue(d: DialogueEntity) = dialogueDao.insert(d)
}