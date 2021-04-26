package com.example.conference.activity

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.R
import com.example.conference.account.Account
import com.example.conference.adapter.ConferenceSettingsRecyclerViewAdapter
import com.example.conference.databinding.ActivityConferenceSettingsBinding
import com.example.conference.exception.*
import com.example.conference.file.Addition
import com.example.conference.json.ContactEntityWithStatus
import com.example.conference.server.api.ConferenceAPIProvider
import com.example.conference.server.provider.ConferenceProvider
import com.example.conference.vm.ConferenceSettingsViewModel
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_conference_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class ConferenceSettingsActivity : AppCompatActivity() {

    companion object {
        const val CHANGE_AVATAR = 0
        const val ADD_USER = 1
    }

    private lateinit var viewModel: ConferenceSettingsViewModel
    private lateinit var binding: ActivityConferenceSettingsBinding
    private val conferenceProvider = ConferenceProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConferenceSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ConferenceSettingsViewModel::class.java)
        viewModel.conferenceID = intent.getIntExtra("conference_id", -1)

        viewModel.viewModelScope.launch {
            viewModel.initConference()
            binding.apply {
                nameTv.text = viewModel.conference.name
                countTv.text = viewModel.conference.count.toString()
                notificationSwitch.isChecked = viewModel.getNotification() == 1
            }
        }

        binding.avatarImage.setOnLongClickListener(this::onAvatarLongClick)

        CoroutineScope(Main).launch {
            try {
                val conferenceMembers: List<ContactEntityWithStatus> =
                    withContext(IO) {
                        conferenceProvider.getConferenceMembers(viewModel.conferenceID)
                    }
                if (conferenceMembers.isEmpty()) {
                    showSnackBar("Что-то пошло не так")
                    return@launch
                }
                binding.membersRv.apply {
                    layoutManager =
                        LinearLayoutManager(this@ConferenceSettingsActivity)
                    adapter =
                        ConferenceSettingsRecyclerViewAdapter(
                            conferenceMembers,
                            this@ConferenceSettingsActivity::showMemberDeletingDialog
                        )
                }
            } catch (e: LoadConferenceMembersException) {
                showSnackBar("Проверьте подключение к сети")
            }
        }

        loadAvatar()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CHANGE_AVATAR -> CoroutineScope(Main).launch {
                    try {
                        val imageUri: Uri = data?.data ?: throw LoadImageException()
                        val fileStream: InputStream? =
                            withContext(IO) {
                                contentResolver.openInputStream(imageUri)
                            }
                        val allBytes: ByteArray = withContext(IO) { fileStream!!.readBytes() }
                        val image = Addition(allBytes, "${viewModel.conferenceID}")
                        val isChanged: Boolean = withContext(IO) {
                            conferenceProvider.changeConferenceAvatar(image)
                        }
                        if (isChanged) {
                            showSnackBar("Фото изменено")
                            loadAvatar()
                        } else {
                            showSnackBar("Что-то пошло не так")
                        }
                    } catch (e: LoadImageException) {
                        showSnackBar("Не удалось отправить изображение")
                    }
                }
                ADD_USER -> CoroutineScope(Main).launch {
                    try {
                        val isAdded: Boolean =
                            withContext(IO) {
                                conferenceProvider
                                    .addUserInConference(
                                        viewModel.conferenceID,
                                        data!!.getIntExtra("user_id", -1),
                                        Account(applicationContext).id
                                    )
                            }
                        if (isAdded) {
                            showSnackBar("Пользователь добавлен в конференцию")
                            updateConferenceMembers()
                        } else
                            showSnackBar("Что-то пошло не так. Возможно, у вас нет прав")
                    } catch (e: AddConferenceMemberException) {
                        showSnackBar("Проверьте подключение к сети")
                    }
                }
            }
        }
    }

    fun onNotificationSwitchClick(v: View) {
        viewModel.viewModelScope.launch {
            (v as Switch).isEnabled = false
            if (viewModel.getNotification() == 1) {
                v.isChecked = false
                viewModel.changeNotification(0)
            } else {
                v.isChecked = true
                viewModel.changeNotification(1)
            }
            v.isEnabled = true
        }
    }

    fun onEditNameClick(v: View) {
        showNameChangingDialog()
    }

    fun onBackClick(v: View) = finish()

    fun onAddUserClick(v: View) =
        startActivityForResult(
            Intent(
                this,
                ChooseUserActivity::class.java
            ),
            ADD_USER
        )

    private fun onAvatarLongClick(v: View): Boolean {
        showAvatarChangingDialog()
        return true
    }

    private fun updateConferenceMembers() {
        CoroutineScope(Main).launch {
            try {
                val conferenceMembers: List<ContactEntityWithStatus> =
                    withContext(IO) {
                        conferenceProvider.getConferenceMembers(viewModel.conferenceID)
                    }
                if (conferenceMembers.isEmpty()) {
                    showSnackBar("Что-то пошло не так")
                    return@launch
                }
                (binding.membersRv.adapter as ConferenceSettingsRecyclerViewAdapter).apply {
                    this.conferenceMembers = conferenceMembers
                    notifyDataSetChanged()
                }

            } catch (e: LoadConferenceMembersException) {
                showSnackBar("Проверьте подключение к сети")
            }
        }
    }

    private fun showNameChangingDialog() {
        val conferenceName = EditText(this)
        conferenceName.text = Editable.Factory.getInstance().newEditable(viewModel.conference.name)
        AlertDialog.Builder(this).setTitle("Название конференции")
            .setView(conferenceName)
            .setPositiveButton("ОК") { _, _ ->
                CoroutineScope(Main).launch {
                    try {
                        val newName: String = conferenceName.text.toString()
                        val isChanged: Boolean = withContext(IO) {
                            conferenceProvider.renameConference(viewModel.conferenceID, newName)
                        }
                        if (isChanged) {
                            withContext(IO) { viewModel.renameConference(newName) }
                            binding.nameTv.text = newName
                            this@ConferenceSettingsActivity.sendBroadcast(
                                Intent("NEW_CONFERENCE_NAME")
                            )
                        } else {
                            showSnackBar("Что-то пошло не так")
                        }
                    }
                    catch (e: ConferenceRenameException) {
                        showSnackBar("Проверьте подключение к сети")
                    }
                }
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
            .create().show()
    }

    private fun showAvatarChangingDialog() {
        AlertDialog.Builder(this).setTitle("Изменить фотографию?")
            .setPositiveButton("Да") { _, _ ->
                val pickIntent = Intent(Intent.ACTION_PICK)
                pickIntent.type = "image/*"
                startActivityForResult(pickIntent, CHANGE_AVATAR)
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun showMemberDeletingDialog(member: ContactEntityWithStatus) {
        AlertDialog.Builder(this).setTitle("Удалить пользователя из конференции?")
            .setPositiveButton("Да") { _, _ ->
                CoroutineScope(Main).launch {
                    try {
                        val isDeleting: Boolean =
                            withContext(IO) {
                                conferenceProvider
                                    .deleteUserFromConference(
                                        viewModel.conferenceID,
                                        member.email.hashCode(),
                                        Account(applicationContext).id
                                    )
                            }
                        if (isDeleting) {
                            showSnackBar("Пользователь удален из конференции")
                            updateConferenceMembers()
                        }
                        else
                            showSnackBar("Что-то пошло не так. Возможно, у вас нет прав")
                    } catch (e: DeleteUserException) {
                        showSnackBar("Проверьте подключение к сети")
                    }
                }
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun loadAvatar() {
        Picasso.get()
            .load(ConferenceAPIProvider.BASE_URL +
                    "/conference/avatar/download/?id=" +
                    viewModel.conferenceID
            )
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .fit()
            .centerCrop()
            .into(binding.avatarImage)
    }

    private fun showSnackBar(text: String) =
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
}