package com.example.conference.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.R
import com.example.conference.adapter.CreateConferenceRecyclerViewAdapter
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.exception.CreateConferenceException
import com.example.conference.exception.CreateDialogueException
import com.example.conference.json.Conference
import com.example.conference.json.ConferenceMember
import com.example.conference.service.Http
import com.example.conference.vm.CreateConferenceViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_create_conference.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList

class  CreateConferenceActivity : AppCompatActivity() {
    private lateinit var vm: CreateConferenceViewModel
    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_conference)

        vm = ViewModelProvider(this).get(CreateConferenceViewModel::class.java)

        createConfBackIB.setOnClickListener { finish() }

        vm.viewModelScope.launch {
            vm.initContacts()
            vm.initAdapter()
            with(createConfereneRV) {
                layoutManager = LinearLayoutManager(this@CreateConferenceActivity)
                adapter = vm.adapter
            }
        }
    }

    fun onCreateConferenceClickListener(v: View) {
        name = conferenceNameET.text.toString()
        if (!conferencePropertiesIsCorrectly())
            return
        addConference()
    }

    private fun conferencePropertiesIsCorrectly(): Boolean = when {
            (name.isBlank()) -> {
                Toast.makeText(this, "Введите название конференции", Toast.LENGTH_LONG).show()
                false
            }
            (vm.conferenceMembers.size < 2) -> {
                Toast.makeText(this, "Недостаточное количество участников", Toast.LENGTH_LONG)
                    .show()
                false
            }
            else -> true
        }

    private fun addConference() {
        GlobalScope.launch {
            val json = Gson().toJson(Conference(
                members = getAllMembers(),
                name = name,
                count = vm.conferenceMembers.size + 1
            ))
            try {
                val r = Http.get(
                    String.format(
                        "/conference/createNewConference/?conference_info=%s",
                        URLEncoder.encode(json, "UTF-8")
                    )
                )
                if (!r.isSuccessful || r.body!!.string().toInt() == -1)
                    throw CreateConferenceException()
                vm.showToast(this@CreateConferenceActivity, "Конференция успешно создана")
                finish()
            } catch (e: ConnectException) {
                vm.showToast(this@CreateConferenceActivity, "Ошибка соединения")
            } catch (e: SocketTimeoutException) {
                vm.showToast(this@CreateConferenceActivity, "Ошибка соединения")
            } catch (e: CreateDialogueException) {
                vm.showToast(this@CreateConferenceActivity, "Что-то пошло не так")
            }
        }
    }

    private fun getAllMembers(): ArrayList<ConferenceMember> {
        val c = ArrayList<ConferenceMember>();
        vm.conferenceMembers.forEach {
            c.add(ConferenceMember(it.hashCode(), 0))
        }
        c.add(ConferenceMember(getSharedPreferences("user_info", MODE_PRIVATE).getInt("user_id", 0), 1))
        return c
    }
}