package com.example.conference.vm

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.conference.adapter.ConferenceSettingsRecyclerViewAdapter
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.exception.LoadConferenceMembersException
import com.example.conference.json.ConferenceMembersList
import com.example.conference.server.Server
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import kotlin.properties.Delegates

class ConferenceSettingsViewModel(val app: Application): AndroidViewModel(app) {
    var conferenceID by Delegates.notNull<Int>()
    private val conferenceDao = ConferenceRoomDatabase.getDatabase(app).conferenceDao()
    lateinit var conference: ConferenceEntity
    lateinit var adapter: ConferenceSettingsRecyclerViewAdapter

    suspend fun getConference(): ConferenceEntity = conferenceDao.getConference(conferenceID)[0]

    suspend fun showToast(text: String) = withContext(Dispatchers.Main) { Toast.makeText(app, text, Toast.LENGTH_LONG).show() }
    suspend fun renameConference(name: String) {
        conference.name = name
        conferenceDao.update(conference)
    }

    suspend fun updateRV() {
        try {
            val r = Server.get("/conference/$conferenceID/members")
            if (!r.isSuccessful)
                throw LoadConferenceMembersException()
            val json = r.body!!.string()
            val conferenceMembers =
                Gson().fromJson(json,  ConferenceMembersList::class.java)
            withContext(Dispatchers.Main) {
                adapter.conferenceMembers = conferenceMembers.list
                adapter.notifyDataSetChanged()
            }
        } catch (e: SocketTimeoutException) {
        } catch (e: ConnectException) {
        } catch (e: LoadConferenceMembersException) {
        }
    }
}