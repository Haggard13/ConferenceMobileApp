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
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.R
import com.example.conference.adapter.ConferenceRecyclerViewAdapter
import com.example.conference.application.ConferenceApplication
import com.example.conference.db.data.MessageType.*
import com.example.conference.db.entity.CMessageEntity
import com.example.conference.exception.LoadImageException
import com.example.conference.exception.SendMessageException
import com.example.conference.file.Addition
import com.example.conference.server.Server
import com.example.conference.server.provider.ConferenceMessageProvider
import com.example.conference.server.sender.ConferenceMessageSender
import com.example.conference.vm.ConferenceViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_conference.*
import kotlinx.android.synthetic.main.activity_dialogue.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList

class ConferenceActivity : AppCompatActivity() {
    private companion object {
        const val PHOTO_LOADING = 0
        const val FILE_LOADING = 1
    }

    private val messageSender = ConferenceMessageSender()
    private val messageProvider = ConferenceMessageProvider()
    private var checkingNewMessagesIsPossible = false
    private var addition: Addition? = null
    private var messageType = TEXT_MESSAGE
    private var adapterIsInitialized = false
    lateinit var viewModel: ConferenceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conference)

        viewModel = ViewModelProvider(this).get(ConferenceViewModel::class.java)
        viewModel.conferenceID = intent.getIntExtra("conference_id", -1)

        checkConferenceName()

        FirebaseMessaging.getInstance().subscribeToTopic(viewModel.conferenceID.toString())

        CoroutineScope(Main).launch {
            conference_name_tv.text = withContext(IO) { viewModel.getConference().name }
            conference_messages_rv.layoutManager = LinearLayoutManager(
                this@ConferenceActivity,
                LinearLayoutManager.VERTICAL,
                true
            )
            val messages: List<CMessageEntity> =
                withContext(IO) {
                    val newMessages: List<CMessageEntity> = messageProvider.getNewMessages(
                        viewModel.conferenceID,
                        viewModel.getLastMessageID(),
                        context = this@ConferenceActivity
                    ) ?: ArrayList()
                    newMessages.forEach { viewModel.saveMessageInDataBase(it) }
                    viewModel.getMessages()
                }
            conference_messages_rv.adapter = ConferenceRecyclerViewAdapter(
                messages,
                context = this@ConferenceActivity,
                this@ConferenceActivity::callbackForPhoto,
                this@ConferenceActivity::callbackForFile
            )
            adapterIsInitialized = true
        }

        this.registerReceiver(
            this.NewNameBroadcastReceiver(),
            IntentFilter("NEW_CONFERENCE_NAME")
        )
    }

    override fun onResume() {
        super.onResume()
        checkingNewMessagesIsPossible = true
        GlobalScope.launch {
            while (checkingNewMessagesIsPossible) {
                if (adapterIsInitialized) {
                    val newMessages: Boolean = messageProvider.checkNewConferenceMessages(
                        viewModel.conferenceID,
                        viewModel.getLastMessageID()
                    )
                    if (newMessages) {
                        updateRecyclerView()
                    }
                    delay(5 * 1000)
                }
            }
        }
        (applicationContext as ConferenceApplication).conferenceID = viewModel.conferenceID
    }

    override fun onPause() {
        super.onPause()
        checkingNewMessagesIsPossible = false
        (applicationContext as ConferenceApplication).conferenceID = 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            viewModel.viewModelScope.launch {
                try {
                    val additionUri = data?.data ?: throw LoadImageException()
                    val fileStream = contentResolver.openInputStream(additionUri)
                    when (requestCode) {
                        FILE_LOADING -> {
                            addition =
                                Addition(fileStream!!.readBytes(), File(additionUri.path!!).name)
                            messageType = MESSAGE_WITH_FILE
                            conference_add_addition_ib.setImageResource(R.drawable.outline_upload_file_24)
                        }

                        PHOTO_LOADING -> {
                            addition = Addition(fileStream!!.readBytes(), "photo")
                            messageType = MESSAGE_WITH_PHOTO
                            conference_add_addition_ib.setImageResource(R.drawable.outline_photo_camera_24)
                        }
                    }
                    conference_add_addition_ib.isEnabled = false
                } catch (e: IOException) {
                    showToast("Не удалось загрузить вложение")
                }
            }
        }
    }

    fun onSendMessageButtonClick(v: View) {
        conference_message_sending_pb.visibility = View.VISIBLE

        GlobalScope.launch { sendMessage() }
    }

    fun onAddFileClick(v: View) = showPopupMenu(v)

    fun onStartConferenceClick(v: View) {
        val intent = Intent(this, MeetActivity::class.java)
        intent.putExtra("conferenceID", viewModel.conferenceID)
        startActivity(intent)
    }

    fun onBackClick(v: View) = finish()

    fun onResultCardClick(v: View) {
        val i = Intent(this, ResultCardsActivity::class.java)
        i.putExtra("conference_id", viewModel.conferenceID)
        startActivity(i)
    }

    fun onSettingsConferenceClick(v: View) {
        startActivity(
            Intent(this, ConferenceSettingsActivity::class.java)
                .putExtra("conference_id", viewModel.conferenceID)
        )
    }

    private suspend fun sendMessage() {
        val messageText = conference_message_et.text.toString().trim()
        try {
            when(messageType) {
                TEXT_MESSAGE -> {
                    if (messageText.isEmpty()) {
                        withContext(Main) {
                            conference_message_sending_pb.visibility = View.INVISIBLE }
                        return
                    }
                    messageSender.sendTextMessage(
                        context = this,
                        messageText,
                        viewModel.conferenceID
                    )
                }
                MESSAGE_WITH_PHOTO -> {
                    messageSender.sendMessageWithPhoto(
                        context = this,
                        photo = addition!!.file,
                        messageText,
                        viewModel.conferenceID
                    )
                }
                AUDIO_MESSAGE ->
                    messageSender.sendAudioMessage(
                        context = this,
                        audio = addition!!.file,
                        viewModel.conferenceID
                    )

                MESSAGE_WITH_FILE -> {
                    messageSender.sendMessageWithFile(
                        context = this,
                        addition!!,
                        viewModel.conferenceID
                    )
                }
            }
            addition = null
            messageType = TEXT_MESSAGE
            if (messageType == TEXT_MESSAGE || messageType == MESSAGE_WITH_PHOTO) {
                withContext(Main) {
                    conference_message_et.setText("") }
            }

            updateRecyclerView()

            withContext(Main) {
                conference_add_addition_ib.setImageResource(R.drawable.outline_add_24)
                conference_add_addition_ib.isEnabled = true
            }
        } catch (e: SendMessageException) {
            val sendingErrorSnackBar = Snackbar.make(
                conference_messages_rv,
                "Произошла ошибка",
                Snackbar.LENGTH_SHORT
            )
            sendingErrorSnackBar.setAction("Повторить", this::onSendMessageButtonClick)
            sendingErrorSnackBar.show()
        }
        conference_message_sending_pb.visibility = View.INVISIBLE
    }

    private fun loadPhoto() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.type = "image/*"
        startActivityForResult(pickIntent, 0)
    }

    private fun loadFile() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.type = "*/*"
        startActivityForResult(pickIntent, FILE_LOADING)
    }

    private fun recordAudioMessage() {
        if (checkPermission()) {
            val mediaRecorder = getMediaRecorder()
            val path =
                "${Environment.getExternalStorageDirectory().absolutePath}/${Date().time}.3gp"
            mediaRecorder.setOutputFile(path)
            try {
                mediaRecorder.prepare()
                mediaRecorder.start()
                setAudioRecordAnimation()
                conference_message_et.isEnabled = false
                conference_add_addition_ib.isEnabled = false
                conference_send_message_btn.setOnClickListener { v ->
                    GlobalScope.launch {
                        mediaRecorder.stop()
                        val fileStream =
                            contentResolver.openInputStream(Uri.fromFile(File(path)) )
                        addition = Addition(fileStream!!.readBytes(), "audio_message")
                        messageType = AUDIO_MESSAGE
                        withContext(Main) {
                            conference_message_et.isEnabled = true
                            conference_message_et.hint = "Введите сообщение..."
                            conference_message_et.clearAnimation()

                            conference_add_addition_ib.isEnabled = true

                            v.setOnClickListener(this@ConferenceActivity::onSendMessageButtonClick)
                            onSendMessageButtonClick(v)
                        }
                    }
                }
            } catch (e: IOException) {
            } catch (e: IllegalStateException) {
            }
        } else requestPermission()
    }

    private fun getMediaRecorder(): MediaRecorder {
        val mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MIC)
        mediaRecorder.setOutputFormat(THREE_GPP)
        mediaRecorder.setAudioEncoder(AMR_NB)
        return mediaRecorder
    }

    private fun checkConferenceName() {
        GlobalScope.launch {
            try {
                val response = Server.get("/conference/getConferenceName/?id=${viewModel.conferenceID}")
                if (!response.isSuccessful) return@launch
                val name = response.body!!.string()
                if (name == "") return@launch
                val conference = viewModel.getConference()
                if (name != conference.name) {
                    conference.name = name
                    viewModel.updateConferenceData(conference)
                    conference_name_tv.text = name
                    showToast("Изменено название конференции")
                }
            } catch (e: SocketTimeoutException) {}
            catch (e: ConnectException) {}
        }
    }

    private fun setAudioRecordAnimation() {
        val animation = AnimationUtils.loadAnimation(
            this, R.anim.audio_recording_anim
        )
        animation.repeatMode = Animation.REVERSE
        animation.repeatCount = Animation.INFINITE
        conference_message_et.startAnimation(animation)
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

    private suspend fun updateRecyclerView() {
        val messages: List<CMessageEntity>? = messageProvider.getNewMessages(
            viewModel.conferenceID,
            viewModel.getLastMessageID(),
            applicationContext
        )
        messages?: return
        messages.forEach { viewModel.saveMessageInDataBase(it) }
        (conference_messages_rv.adapter as ConferenceRecyclerViewAdapter).messages =
            viewModel.getMessages()
        withContext(Main) {
            (conference_messages_rv.adapter as ConferenceRecyclerViewAdapter).notifyDataSetChanged()
        }
    }

    private fun callbackForPhoto(id: Int) {
        val intent = Intent(this, PhotoReviewerActivity::class.java)
        intent.putExtra("photo_id", id)
        intent.putExtra("is_conference", true)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callbackForFile(id: Int, name: String) {
        GlobalScope.launch {
            try {
                val response: Response = Server.get("/conference/getFile/?id=$id")

                response.body!!.byteStream().use {
                    Files.write(
                        Paths.get(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)!!.path +
                                    "/$name"
                        ), it.readBytes()
                    )
                }
                showToast("Файл сохранен на устройство")
            } catch (e: SocketTimeoutException) {
                showToast("Возникла ошибка при скачивании")
            } catch (e: ConnectException) {
                showToast("Возникла ошибка при скачивании")
            } catch (e: SocketException) {
                showToast("Возникла ошибка при скачивании")
            }
        }
    }

    suspend fun showToast(text: String) =
        withContext(Main) { Toast.makeText(this@ConferenceActivity, text, Toast.LENGTH_LONG).show() }

    inner class NewNameBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.viewModelScope.launch {
                conference_name_tv.text = viewModel.getConference().name
            }
        }
    }
}