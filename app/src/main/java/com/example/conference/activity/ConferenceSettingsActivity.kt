package com.example.conference.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.R
import com.example.conference.adapter.ConferenceSettingsRecyclerViewAdapter
import com.example.conference.exception.*
import com.example.conference.json.ConferenceMembersList
import com.example.conference.json.ContactEntityWithStatus
import com.example.conference.server.Server
import com.example.conference.vm.ConferenceSettingsViewModel
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_conference_settings.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URLEncoder

class ConferenceSettingsActivity : AppCompatActivity() {
    lateinit var vm: ConferenceSettingsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conference_settings)

        vm = ViewModelProvider(this).get(ConferenceSettingsViewModel::class.java)
        vm.conferenceID = intent.getIntExtra("conference_id", -1)
        vm.viewModelScope.launch {
            vm.conference = vm.getConference()
            conferenceNameSettingsTV.text = vm.conference.name
            conferenceCountSettingsTV.text = vm.conference.count.toString()
            // conferenceNotificationSwitch.isChecked = vm.conference.notification == 1 fixme
        }

        conferenceNotificationSwitch.setOnClickListener(this::onNotificationSwitchClick)
        editConferenceNameIB.setOnClickListener(this::onEditNameClick)
        conferenceSettingsBackIB.setOnClickListener { finish() }
        conferenceSettingsAvatarIV.setOnLongClickListener(this::onConferenceAvatarLongClick)
        addConferenceMemberIB.setOnClickListener(this::onAddUserClick)
        GlobalScope.launch {
            try {
                val r = Server.get("/conference/${vm.conferenceID}/members")
                if (!r.isSuccessful)
                    throw LoadConferenceMembersException()
                val json = r.body!!.string()
                val conferenceMembers =
                    Gson().fromJson(json,  ConferenceMembersList::class.java)
                withContext(Main) {
                    conferenceSettingsMembersRV.layoutManager = LinearLayoutManager(this@ConferenceSettingsActivity)
                    vm.adapter = ConferenceSettingsRecyclerViewAdapter(conferenceMembers.list,
                        this@ConferenceSettingsActivity::deleteMemberDialog)
                    conferenceSettingsMembersRV.adapter = vm.adapter
                }
            } catch (e: SocketTimeoutException) {
            } catch (e: ConnectException) {
            } catch (e: LoadConferenceMembersException) {
            }
        }

        Picasso.get()
            .load("${Server.baseURL}/conference/avatar/download/?id=${vm.conferenceID}")
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .fit()
            .centerCrop()
            .into(conferenceSettingsAvatarIV)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CHANGE_AVATAR && resultCode == RESULT_OK) {
            GlobalScope.launch {
                try {
                    val imageUri = data?.data ?: throw LoadImageException()
                    val fileStream = contentResolver.openInputStream(imageUri)
                    val allBytes = fileStream!!.readBytes()
                    val result = Server.sendNewConferenceAvatar(vm.conferenceID, allBytes)

                    if (result == -1)
                        throw LoadImageException()
                    withContext(Main) {
                        Picasso.get()
                            .load(
                                Server.baseURL
                                        + "/conference/avatar/download/?id="
                                        + vm.conferenceID
                            )
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .fit()
                            .centerCrop()
                            .into(conferenceSettingsAvatarIV)
                    }
                    vm.showToast("Успешно")
                } catch (e: LoadImageException) {
                    vm.showToast("Не удалось загрузить изображение")
                } catch (e: ConnectException) {
                    vm.showToast("Не удалось загрузить изображение")
                } catch (e: SocketTimeoutException) {
                    vm.showToast("Не удалось загрузить изображение")
                } catch (e: IOException) {
                    vm.showToast("Не удалось загрузить изображение")
                }
            }
        } else if (requestCode == ADD_USER && resultCode == RESULT_OK) {
            GlobalScope.launch {
                try {
                    val r = Server.get("/conference" +
                            "/${vm.conferenceID}" +
                            "/addUser" +
                            "/${data!!.getIntExtra("user_id", 0)}" +
                            "/as" +
                            "/${getSharedPreferences("user_info", MODE_PRIVATE)
                                .getInt("user_id", 0)}")
                    if (!r.isSuccessful || r.body!!.string().toInt() == 0)
                        throw AddConferenceMemberException()
                    vm.showToast("Успешно")

                    vm.updateRV()
                } catch (e: ConnectException) {
                    vm.showToast("Проверьте подключение к сети")
                } catch (e: SocketTimeoutException) {
                    vm.showToast("Проверьте подключение к сети")
                } catch (e: AddConferenceMemberException) {
                    vm.showToast("Что-то пошло не так. Возможно, у вас недостаточно прав")
                }
            }
        }
    }

    private fun onNotificationSwitchClick(v: View) {
        vm.viewModelScope.launch {
            /*(v as Switch).isEnabled = false
            if (vm.conference.notification == 1) {
                v.isChecked = false
                vm.setNotification(0)
            } else {
                v.isChecked = true
                vm.setNotification(1)
            }
            v.isEnabled = true*/ // FIXME: 15.04.21  
        }
    }

    private fun onEditNameClick(v: View) {
        changeNameDialog()
    }

    private fun onConferenceAvatarLongClick(v: View): Boolean {
        changeAvatarDialog()
        return true
    }

    private fun changeNameDialog() {
        val conferenceName = EditText(this)
        conferenceName.text = Editable.Factory.getInstance().newEditable(vm.conference.name)
        AlertDialog.Builder(this).setTitle("Название конференции")
            .setView(conferenceName)
            .setPositiveButton("ОК") { _, _ ->
                GlobalScope.launch {
                    try {
                        var newName: String
                        withContext(Main) {
                            newName = conferenceName.text.toString()
                        }
                        val response = Server.get(
                            "/conference/rename/?id=${vm.conferenceID}&" +
                                    "new_name=${URLEncoder.encode(newName, "UTF-8")}")
                        if (!response.isSuccessful)
                            throw ConferenceRenameException()
                        vm.renameConference(newName)
                        withContext(Main) {
                            conferenceNameSettingsTV.text = newName
                        }
                        this@ConferenceSettingsActivity.sendBroadcast(Intent("NEW_CONFERENCE_NAME"))
                    }
                    catch (e: SocketTimeoutException) { vm.showToast("Произошла ошибка") }
                    catch (e: ConnectException) { vm.showToast("Произошла ошибка") }
                    catch (e: ConferenceRenameException) { vm.showToast("Произошла ошибка") }
                }
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
            .create().show()
    }

    private fun changeAvatarDialog() {
        AlertDialog.Builder(this).setTitle("Изменить фотографию?")
            .setPositiveButton("Да") { _, _ ->
                val pickIntent = Intent(Intent.ACTION_PICK)
                pickIntent.type = "image/*"
                startActivityForResult(pickIntent, CHANGE_AVATAR)
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.cancel()
            }
            .create().show()
    }

    private fun deleteMemberDialog(m: ContactEntityWithStatus) {
        AlertDialog.Builder(this).setTitle("Удалить пользователя из конференции?")
            .setPositiveButton("Да") { _, _ ->
                GlobalScope.launch {
                    try {
                        val r = Server.get("/conference" +
                                "/${vm.conferenceID}" +
                                "/deleteUser" +
                                "/${m.email.hashCode()}" +
                                "/as" +
                                "/${getSharedPreferences("user_info", MODE_PRIVATE).getInt("user_id", 0)}")
                        if (!r.isSuccessful)
                            throw DeleteUserException()
                        else if (r.body!!.string().toInt() != 1)
                            throw DeleteUserException()
                        else {
                            vm.showToast("Пользователь удален")
                            vm.updateRV()
                        }
                    } catch (e: ConnectException) {
                        vm.showToast("Проверьте подключение к сети")
                    } catch (e: SocketTimeoutException) {
                        vm.showToast("Проверьте подключение к сети")
                    } catch (e: DeleteUserException) {
                        vm.showToast("Что-то пошло не так")
                    }
                }
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.cancel()
            }
            .create().show()
    }

    private fun onAddUserClick(v: View) {
        startActivityForResult(Intent(this, ChooseUserActivity::class.java), ADD_USER)
    }

    companion object {
        const val CHANGE_AVATAR = 0
        const val ADD_USER = 1
    }
}