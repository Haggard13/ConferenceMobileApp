package com.example.conference.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.R
import com.example.conference.application.ConferenceApplication
import com.example.conference.db.entity.DMessageEntity
import com.example.conference.exception.LoadFileException
import com.example.conference.exception.LoadImageException
import com.example.conference.exception.SendMessageException
import com.example.conference.service.Server
import com.example.conference.vm.DialogueViewModel
import kotlinx.android.synthetic.main.activity_conference.*
import kotlinx.android.synthetic.main.activity_dialogue.*
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.*
import com.example.conference.file.File as MyFile

class DialogueActivity : AppCompatActivity() {
    lateinit var vm: DialogueViewModel
    private var updatePossible = false
    private var photo: ByteArray? = null
    private var audio: ByteArray? = null
    private var file: MyFile? = null
    private var messageType = MESSAGE_WITH_TEXT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialogue)

        vm = ViewModelProvider(this).get(DialogueViewModel::class.java)
        vm.dialogueID = intent.getIntExtra("dialogue_id", -1)

        vm.viewModelScope.launch {
            companionNameTV.text = vm.getDialogue().second_user_name
            vm.initMessages()
            vm.initAdapter(this@DialogueActivity)
            dialogueRV.layoutManager = LinearLayoutManager(this@DialogueActivity, LinearLayoutManager.VERTICAL, true)
            dialogueRV.adapter = vm.adapter
        }

        dialogueAddFileIB.setOnClickListener(this::onAddFileClick)
        dialBackIB.setOnClickListener { finish() }
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
        (applicationContext as ConferenceApplication).dialogue_id = vm.dialogueID
    }

    override fun onPause() {
        super.onPause()
        updatePossible = false
        (applicationContext as ConferenceApplication).dialogue_id = 0
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
                    dialogueAddFileIB.setImageResource(R.drawable.photo)
                    dialogueAddFileIB.isEnabled = false
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
                    messageType = MESSAGE_WITH_FILE
                    dialogueAddFileIB.setImageResource(R.drawable.file)
                    dialogueAddFileIB.isEnabled = false
                } catch (e: IOException) {
                    vm.showToast("Не удалось загрузить файл")
                }
            }
        }
    }

    private fun onAddFileClick(v: View) = showPopupMenu(v)

    fun onEnterMessageButtonClick(v: View) {
        var messageText = dialogueMessageET.text.toString()
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
                withContext(Dispatchers.Main) {
                    if (messageType != MESSAGE_WITH_AUDIO && messageType != MESSAGE_WITH_FILE)
                        dialogueMessageET.setText("")
                }
                messageType = MESSAGE_WITH_TEXT
            } else if (messageType == MESSAGE_WITH_AUDIO || messageType == MESSAGE_WITH_FILE) {
                messageType = MESSAGE_WITH_TEXT
            }
        }
    }

    private suspend fun sendMessage(messageText: String, date: Long): DMessageEntity? {
        var message: DMessageEntity? = null

        try {
            when(messageType) {
                MESSAGE_WITH_PHOTO -> {
                    val r = Server.sendDialogueMessagePhoto(
                        photo,
                        vm.createDMessageEntity(0, messageText, date,2)
                    )
                    if (!r.isSuccessful)
                        throw SendMessageException()
                    if (r.headers["message_id"]?.toInt() == -1)
                        throw LoadImageException()
                    message = vm.createDMessageEntity(
                        r.headers["message_id"]!!.toInt(),
                        messageText,
                        date,
                        2
                    )
                }
                MESSAGE_WITH_AUDIO -> {
                    val r = Server.sendDialogueMessageAudio(audio,
                        vm.createDMessageEntity(0,"Аудиосообщение", date, 3))
                    if (!r.isSuccessful)
                        throw SendMessageException()
                    if (r.headers["message_id"]?.toInt() == -1)
                        throw SendMessageException()
                    message = vm.createDMessageEntity(
                        r.headers["message_id"]!!.toInt(),
                        "",
                        date,
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
                    message = vm.createDMessageEntity(messageId, messageText, date,  1)
                }
                MESSAGE_WITH_FILE -> {
                    val r = Server.sendDialogueFile(file,
                    vm.createDMessageEntity(0, file!!.name, date, 4))
                    if (!r.isSuccessful)
                        throw SendMessageException()
                    if (r.headers["message_id"]?.toInt() == -1)
                        throw SendMessageException()
                    message = vm.createDMessageEntity(
                        r.headers["message_id"]!!.toInt(),
                        messageText,
                        date,
                        4
                    )
                }
            }
            vm.updateMessages()
            photo = null
            audio = null
            withContext(Dispatchers.Main) {
                dialogueAddFileIB.setImageResource(R.drawable.add)
                dialogueAddFileIB.isEnabled = true
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
        startActivityForResult(pickIntent, PHOTOGRAPHY_LOAD)
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
                dialogueMessageET.isEnabled = false
                dialogueAddFileIB.isEnabled = false
                dialogueEnterMessageBtn.setOnClickListener { v ->
                    GlobalScope.launch {
                        mr.stop()
                        val fileStream =
                            contentResolver.openInputStream(Uri.fromFile(File(path)) )
                        audio = fileStream!!.readBytes()
                        messageType = MESSAGE_WITH_AUDIO
                        withContext(Dispatchers.Main) {
                            with(dialogueMessageET) {
                                isEnabled = true
                                hint = "Введите сообщение..."
                                clearAnimation()
                            }

                            dialogueAddFileIB.isEnabled = true

                            v.setOnClickListener(this@DialogueActivity::onEnterMessageButtonClick)
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
        mr.setAudioSource(MediaRecorder.AudioSource.MIC)
        mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mr.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        return mr
    }

    private fun setAudioRecordAnimation() {
        val a = AnimationUtils.loadAnimation(
            this, R.anim.audio_recording_anim
        )
        a.repeatMode = Animation.REVERSE
        a.repeatCount = Animation.INFINITE
        dialogueMessageET.startAnimation(a)
    }

    private fun checkPermission() =
        ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        ), 1)
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

    companion object {
        const val PHOTOGRAPHY_LOAD = 0
        const val FILE_LOAD = 5

        const val MESSAGE_WITH_TEXT = 1
        const val MESSAGE_WITH_PHOTO = 2
        const val MESSAGE_WITH_AUDIO = 3
        const val MESSAGE_WITH_FILE = 4
    }
}