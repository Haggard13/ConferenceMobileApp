package com.example.conference.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.R
import com.example.conference.adapter.CreateDialogueRecyclerViewAdapter
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.exception.CreateDialogueException
import com.example.conference.json.Dialogue
import com.example.conference.server.Server
import com.example.conference.vm.CreateDialogueViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_create_dialogue.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URLEncoder

class CreateDialogueActivity : AppCompatActivity() {
    lateinit var vm: CreateDialogueViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_dialogue)

        vm = ViewModelProvider(this).get(CreateDialogueViewModel::class.java)

        createDiaBackIB.setOnClickListener { finish() }

        vm.viewModelScope.launch {
            createDialogueRV.layoutManager = LinearLayoutManager(this@CreateDialogueActivity)
            createDialogueRV.adapter = CreateDialogueRecyclerViewAdapter(
                contacts = ConferenceRoomDatabase.getDatabase(this@CreateDialogueActivity).contactDao().getAll()
            ) { email: String, name: String, surname: String ->
                addDialogue(email, name, surname)
            }
        }
    }

    private fun addDialogue(email: String, name: String, surname: String) {
        GlobalScope.launch {
            val json = Gson()
                .toJson(
                    Dialogue(
                        first_user_id = vm.getUserID(),
                        second_user_id = email.hashCode(),
                        first_user_email = vm.getUserEmail()!!,
                        second_user_email = email,
                        first_user_name = vm.getUserName()!!,
                        second_user_name = name,
                        first_user_surname = vm.getUserSurname()!!,
                        second_user_surname = surname))
            try {
                val r = Server.get(
                    String.format(
                        "/dialogue/createNewDialogue/?dialogue_info=%s",
                        URLEncoder.encode(json, "UTF-8")
                    )
                )
                if (!r.isSuccessful || r.body!!.string().toInt() == -1)
                    throw CreateDialogueException()
                vm.showToast(this@CreateDialogueActivity, "Диалог успешно создан")
                finish()
            } catch (e: ConnectException) {
                vm.showToast(this@CreateDialogueActivity, "Ошибка соединения")
            } catch (e: SocketTimeoutException) {
                vm.showToast(this@CreateDialogueActivity, "Ошибка соединения")
            } catch (e: CreateDialogueException) {
                vm.showToast(this@CreateDialogueActivity, "Что-то пошло не так. Возможно, диалог уже существует")
            }
        }
    }
}