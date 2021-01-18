package com.example.conference.vm

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import com.example.conference.adapter.CreateConferenceRecyclerViewAdapter
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.db.entity.ContactEntity
import com.example.conference.json.ConferenceMember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateConferenceViewModel(application: Application) : AndroidViewModel(application) {
    private val contactDao = ConferenceRoomDatabase.getDatabase(application).contactDao()
    private suspend fun getContacts(): List<ContactEntity> = contactDao.getAll()

    var conferenceMembers: ArrayList<String> = ArrayList()
    lateinit var contacts: List<ContactEntity>
    lateinit var adapter: CreateConferenceRecyclerViewAdapter

    suspend fun initContacts() {
        contacts = getContacts()
    }

    fun initAdapter() {
        adapter = CreateConferenceRecyclerViewAdapter(contacts,
            {
                conferenceMembers.add(it)
            },
            {
                conferenceMembers.remove(it)
            })
    }

    suspend fun showToast(context: Context, text: String) = withContext(Dispatchers.Main) { Toast.makeText(context, text, Toast.LENGTH_LONG).show() }

}