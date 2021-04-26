package com.example.conference.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.adapter.CreateDialogueRecyclerViewAdapter
import com.example.conference.databinding.ActivityCreateDialogueBinding
import com.example.conference.db.entity.DialogueEntity
import com.example.conference.exception.CreateDialogueException
import com.example.conference.server.provider.DialogueProvider
import com.example.conference.vm.CreateDialogueViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_create_dialogue.*
import kotlinx.android.synthetic.main.fragment_dialogues.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class CreateDialogueActivity : AppCompatActivity() {

    private lateinit var viewModel: CreateDialogueViewModel
    private val dialogueProvider = DialogueProvider()
    private lateinit var binding: ActivityCreateDialogueBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateDialogueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(CreateDialogueViewModel::class.java)

        viewModel.viewModelScope.launch {
            binding.contactsRv.apply {
                layoutManager = LinearLayoutManager(this@CreateDialogueActivity)
                adapter =
                    CreateDialogueRecyclerViewAdapter(
                        contacts = viewModel.getAllContacts(),
                        callback = this@CreateDialogueActivity::createDialogue
                    )
            }
        }
    }

    fun onBackClick(v: View) = finish()


    private fun createDialogue(email: String, name: String, surname: String) {
        CoroutineScope(Main).launch {
            try {
                binding.dialogueCreatingPb.isVisible = true
                val isCreated =
                    withContext(IO) {
                        dialogueProvider.createNewDialogue(
                            DialogueEntity(
                                id = -1,
                                second_user_id = email.hashCode(),
                                second_user_email = email,
                                second_user_name = name,
                                second_user_surname = surname,
                                last_message = "Диалог создан",
                                last_message_time = Date().time
                            ),
                            viewModel.account
                        )
                    }
                if (isCreated) {
                    showSnackBar("Диалог создан")
                    delay(500)
                    finish()
                } else {
                    showSnackBar("Ошибка создания диалога")
                }
            } catch (e: CreateDialogueException) {
                showSnackBar("Проверьте подключение к сети")
            } finally {
                binding.dialogueCreatingPb.isVisible = false
            }
        }
    }

    private fun showSnackBar(text: String) {
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
    }
}