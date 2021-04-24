package com.example.conference.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.account.Account
import com.example.conference.adapter.CreateConferenceRecyclerViewAdapter
import com.example.conference.databinding.ActivityCreateConferenceBinding
import com.example.conference.db.data.ConferenceMemberStatus
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.exception.CreateConferenceException
import com.example.conference.json.ConferenceMember
import com.example.conference.server.provider.ConferenceProvider
import com.example.conference.vm.CreateConferenceViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_create_conference.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class  CreateConferenceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateConferenceBinding
    private lateinit var viewModel: CreateConferenceViewModel
    private val members = ArrayList<ConferenceMember>()
    private val conferenceProvider = ConferenceProvider()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateConferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(CreateConferenceViewModel::class.java)

        val account = Account(this)
        members.add(
            ConferenceMember(
                account.userID,
                ConferenceMemberStatus.ADMIN.ordinal
            )
        )

        viewModel.viewModelScope.launch {
            binding.contactsRv .apply {
                layoutManager = LinearLayoutManager(this@CreateConferenceActivity)
                adapter = CreateConferenceRecyclerViewAdapter(
                    contacts = viewModel.getContacts(),
                    callbackAddMember = {
                        members.add(
                            ConferenceMember(
                                id = it.hashCode(),
                                ConferenceMemberStatus.SIMPLE_USER.ordinal
                            )
                        )
                    },
                    callbackRemoveMember = { email ->
                        members.removeIf {
                            it.id == email.hashCode()
                        }
                    }
                )
            }
        }
    }

    fun onBackClick(v: View) = finish()

    fun onCreateConferenceClick(v: View) {
        val name = binding.conferenceNameEt.text.toString()
        if (!conferencePropertiesIsCorrectly(name)) {
            return
        }
        createConference(name)
    }

    private fun conferencePropertiesIsCorrectly(name: String): Boolean =
        when {
            (name.isBlank()) -> {
                showSnackBar("Введите название конференции")
                false
            }
            (members.size < 3) -> {
                showSnackBar("В конференции должно быть больше трех членов")
                false
            }
            else -> true
        }

    private fun createConference(name: String) =
        CoroutineScope(Main).launch {
            binding.confereceCreatingPb.isVisible = true
            try {
                val conference = ConferenceEntity(
                    id = -1,
                    name,
                    count = members.size,
                    last_message = "Конференция создана",
                    last_message_time = Date().time
                )
                val isCreated: Boolean = withContext(IO) {
                    conferenceProvider.createNewConference(conference, members)
                }
                if (isCreated) {
                    showSnackBar("Конференция создана")
                    finish()
                } else {
                    showSnackBar("При создании произошла ошибка")
                }
            } catch (e: CreateConferenceException) {
                showSnackBar("Проверьте подключение к сети")
            } finally {
                binding.confereceCreatingPb.isVisible = false
            }
        }

    private fun showSnackBar(text: String) =
        Snackbar
            .make(binding.root, text, Snackbar.LENGTH_SHORT)
            .show()
}