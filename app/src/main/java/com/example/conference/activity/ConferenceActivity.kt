package com.example.conference.activity

import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.media.MediaRecorder.AudioSource.MIC
import android.media.MediaRecorder.OutputFormat.AMR_NB
import android.media.MediaRecorder.OutputFormat.THREE_GPP
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.R
import com.example.conference.application.ConferenceApplication
import com.example.conference.db.entity.CMessageEntity
import com.example.conference.exception.LoadFileException
import com.example.conference.exception.LoadImageException
import com.example.conference.exception.SendMessageException
import com.example.conference.service.Server
import com.example.conference.vm.ConferenceViewModel
import kotlinx.android.synthetic.main.activity_conference.*
import kotlinx.android.synthetic.main.activity_dialogue.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.*
import com.example.conference.file.File as MyFile

class ConferenceActivity : AppCompatActivity() {
    lateinit var vm: ConferenceViewModel
    private var updatePossible = false
    private var photo: ByteArray? = null
    private var audio: ByteArray? = null
    private var file: MyFile? = null
    private var messageType = MESSAGE_WITH_TEXT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conference)

        vm = ViewModelProvider(this).get(ConferenceViewModel::class.java)
        vm.conferenceID = intent.getIntExtra("conference_id", -1)

        checkConferenceName()

        vm.viewModelScope.launch {
            conferenceNameTV.text = vm.getConference().name
            vm.initMessages()
            vm.initAdapter(this@ConferenceActivity)
            conferenceRV.layoutManager = LinearLayoutManager(
                this@ConferenceActivity,
                LinearLayoutManager.VERTICAL,
                true
            )
            conferenceRV.adapter = vm.adapter
        }

        this.registerReceiver(
            NewNameBroadcastReceiver(),
            IntentFilter("NEW_CONFERENCE_NAME")
        )
    }

    override fun onResume() {
        super.onResume()
        updatePossible = true
        GlobalScope.launch {
            while (updatePossible) {
                if (vm.messagesIsInitialize()) {
                    vm.updateMessages()
                }
                delay(5 * 1000)
            }
        }
        (applicationContext as ConferenceApplication).conference_id = vm.conferenceID
    }

    override fun onPause() {
        super.onPause()
        updatePossible = false
        (applicationContext as ConferenceApplication).conference_id = 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTOGRAPHY_LOAD && resultCode == RESULT_OK) {
            vm.viewModelScope.launch {
                try {
                    val imageUri = data?.data ?: throw LoadImageException()
                    val fileStream = contentResolver.openInputStream(imageUri)
                    photo = fileStream!!.readBytes()
                    messageType = MESSAGE_WITH_PHOTO
                    addFileConferenceIB.setImageResource(R.drawable.photo)
                    addFileConferenceIB.isEnabled = false
                } catch (e: IOException) {
                    vm.showToast("Не удалось загрузить изображение")
                }
            }
        } else if (requestCode == FILE_LOAD && resultCode == RESULT_OK) {
            vm.viewModelScope.launch {
                try {
                    val fileUri = data?.data ?: throw LoadFileException()
                    val fileStream = contentResolver.openInputStream(fileUri)
                    file = MyFile(fileStream!!.readBytes(), File(fileUri.path!!).name)
                    messageType = DialogueActivity.MESSAGE_WITH_FILE
                    dialogueAddFileIB.setImageResource(R.drawable.file)
                    dialogueAddFileIB.isEnabled = false
                } catch (e: IOException) {
                    vm.showToast("Не удалось загрузить файл")
                }
            }
        }
    }

    fun onEnterMessageButtonClick(v: View) {
        var messageText = conferenceMessageET.text.toString()
        if (messageText.isBlank() && messageType != MESSAGE_WITH_AUDIO
            && messageType != MESSAGE_WITH_FILE)
            return

        messageText = messageText.trim()
        val date = Date().time

        GlobalScope.launch {
            val sendingResult = sendMessage(messageText, date)

            if (sendingResult != null) {
                vm.saveMessage(sendingResult)
                vm.updateRV()
                withContext(Main) {
                    if (messageType != MESSAGE_WITH_AUDIO && messageType != MESSAGE_WITH_FILE)
                        conferenceMessageET.setText("")
                }
                messageType = MESSAGE_WITH_TEXT
            } else if (messageType == MESSAGE_WITH_AUDIO || messageType == MESSAGE_WITH_FILE) {
                messageType = MESSAGE_WITH_TEXT
            }
        }
    }

    fun onAddFileClick(v: View) = showPopupMenu(v)

    fun onStartConferenceClick(v: View) {
        val intent = Intent(this, MeetActivity::class.java)
        startActivity(intent)
    }

    fun onBackClick(v: View) = finish()

    fun onResultCardClick(v: View) {
        val i = Intent(this, ResultCardsActivity::class.java)
        i.putExtra("conference_id", vm.conferenceID)
        startActivity(i)
    }

    fun onSettingsConferenceClick(v: View) {
        startActivity(
            Intent(this, ConferenceSettingsActivity::class.java)
                .putExtra("conference_id", vm.conferenceID)
        )
    }

    private suspend fun sendMessage(messageText: String, date: Long): CMessageEntity? {
        var message: CMessageEntity? = null

        try {
            when(messageType) {
                MESSAGE_WITH_PHOTO -> {
                    val r = Server.sendConferenceMessagePhoto(
                        photo,
                        vm.createCMessageEntity(messageText, date, 0, 2)
                    )
                    if (!r.isSuccessful) {
                        throw SendMessageException()
                    }
                    if (r.headers["message_id"]?.toInt() == -1)
                        throw LoadImageException()
                    message = vm.createCMessageEntity(
                        messageText,
                        date,
                        r.headers["message_id"]!!.toInt(),
                        2
                    )
                }
                MESSAGE_WITH_AUDIO -> {
                    val r = Server.sendConferenceMessageAudio(audio,
                    vm.createCMessageEntity("AudioMessage", date, 0, 3))
                    if (!r.isSuccessful) {
                        throw SendMessageException()
                    }
                    if (r.headers["message_id"]?.toInt() == -1) {
                        throw SendMessageException()
                    }
                    message = vm.createCMessageEntity("",
                        date,
                        r.headers["message_id"]!!.toInt(),
                        3
                    )
                }
                MESSAGE_WITH_TEXT -> {
                     val r = Server.get(vm.generateURL(messageText, date))
                     if (!r.isSuccessful) {
                         throw SendMessageException()
                     }
                     val messageId = r.body!!.string().toInt()
                     if (messageId == -1) {
                         throw SendMessageException()
                     }
                     message = vm.createCMessageEntity(messageText, date, messageId, 1)
                }
                MESSAGE_WITH_FILE -> {
                    val r = Server.sendConferenceFile(file,
                    vm.createCMessageEntity(file!!.name, date, 0,4))
                    if (!r.isSuccessful)
                        throw SendMessageException()
                    if (r.headers["message_id"]?.toInt() == -1)
                        throw SendMessageException()
                    message = vm.createCMessageEntity(
                        file!!.name,
                        date,
                        r.headers["message_id"]!!.toInt(),
                        4
                    )
                }
            }
            vm.updateMessages()
            photo = null
            audio = null
            withContext(Main) {
                addFileConferenceIB.setImageResource(R.drawable.add)
                addFileConferenceIB.isEnabled = true
            }
        } catch (e: SendMessageException) {
            vm.showToast("Ошибка отправки")
        } catch (e: ConnectException) {
            vm.showToast("Проверьте подключение к сети")
        } catch (e: SocketTimeoutException) {
            vm.showToast("Проверьте подключение к сети")
        } catch (e: LoadImageException) {
            vm.showToast("Не удалось отправить фотографию")
        }
        return message
    }

    private fun loadPhoto() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.type = "image/*"
        startActivityForResult(pickIntent, 0)
    }

    private fun loadFile() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.type = "*/*"
        startActivityForResult(pickIntent, FILE_LOAD)
    }

    private fun recordAudioMessage() {
        if (checkPermission()) {
            val mr = getMediaRecorder()
            val path =
                "${Environment.getExternalStorageDirectory().absolutePath}/${Date().time}.3gp"
            mr.setOutputFile(path)
            try {
                mr.prepare()
                mr.start()
                setAudioRecordAnimation()
                conferenceMessageET.isEnabled = false
                addFileConferenceIB.isEnabled = false
                enterMessageIB.setOnClickListener { v ->
                    GlobalScope.launch {
                        mr.stop()
                        val fileStream =
                            contentResolver.openInputStream(Uri.fromFile(File(path)) )
                        audio = fileStream!!.readBytes()
                        messageType = MESSAGE_WITH_AUDIO
                        withContext(Main) {
                            conferenceMessageET.isEnabled = true
                            conferenceMessageET.hint = "Введите сообщение..."
                            conferenceMessageET.clearAnimation()

                            addFileConferenceIB.isEnabled = true

                            v.setOnClickListener(this@ConferenceActivity::onEnterMessageButtonClick)
                            onEnterMessageButtonClick(v)
                        }
                    }
                }
            } catch (e: IOException) {
            } catch (e: IllegalStateException) {
            }
        } else requestPermission()
    }

    private fun getMediaRecorder(): MediaRecorder {
        val mr = MediaRecorder()
        mr.setAudioSource(MIC)
        mr.setOutputFormat(THREE_GPP)
        mr.setAudioEncoder(AMR_NB)
        return mr
    }

    private fun checkConferenceName() {
        GlobalScope.launch {
            try {
                val response = Server.get("/conference/getConferenceName/?id=${vm.conferenceID}")
                if (!response.isSuccessful) return@launch
                val name = response.body!!.string()
                if (name == "") return@launch
                val conference = vm.getConference()
                if (name != conference.name) {
                    conference.name = name
                    vm.update(conference)
                    vm.showToast("Изменено название конференции")
                }
            } catch (e: SocketTimeoutException) {}
            catch (e: ConnectException) {}
        }
    }

    private fun setAudioRecordAnimation() {
        val a = AnimationUtils.loadAnimation(
            this, R.anim.audio_recording_anim
        )
        a.repeatMode = Animation.REVERSE
        a.repeatCount = Animation.INFINITE
        conferenceMessageET.startAnimation(a)
    }

    private fun showPopupMenu(v: View) {
        val popupMenu = PopupMenu(this, v)
        popupMenu.inflate(R.menu.add_file_menu)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.photo -> {
                    loadPhoto()
                    true
                }
                R.id.audioMessage -> {
                    recordAudioMessage()
                    true
                }
                R.id.file -> {
                    loadFile()
                    true
                }
                else -> {
                    false
                }
            }
        }

        popupMenu.show()
    }

    private fun checkPermission() =
        ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE, RECORD_AUDIO), 1)
    }

    companion object {
        const val PHOTOGRAPHY_LOAD = 0
        const val FILE_LOAD = 5

        const val MESSAGE_WITH_TEXT = 1
        const val MESSAGE_WITH_PHOTO = 2
        const val MESSAGE_WITH_AUDIO = 3
        const val MESSAGE_WITH_FILE = 4
    }

    inner class NewNameBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            vm.viewModelScope.launch {
                conferenceNameTV.text = vm.getConference().name
            }
        }
    }
}