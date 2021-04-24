package com.example.conference.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
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
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.R
import com.example.conference.adapter.DialogueRecyclerViewAdapter
import com.example.conference.application.ConferenceApplication
import com.example.conference.databinding.ActivityDialogueBinding
import com.example.conference.db.data.MessageType
import com.example.conference.db.entity.DMessageEntity
import com.example.conference.exception.LoadImageException
import com.example.conference.exception.SendMessageException
import com.example.conference.file.Addition
import com.example.conference.server.Server
import com.example.conference.server.provider.DialogueMessageProvider
import com.example.conference.server.sender.DialogueMessageSender
import com.example.conference.vm.DialogueViewModel
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

class DialogueActivity : AppCompatActivity() {
    private companion object {
        const val PHOTO_LOADING = 0
        const val FILE_LOADING = 1
    }

    private val messageSender = DialogueMessageSender()
    private val messageProvider = DialogueMessageProvider()
    private var checkingNewMessagesIsPossible = false
    private var addition: Addition? = null
    private var messageType = MessageType.TEXT_MESSAGE
    private var adapterIsInitialized = false
    private lateinit var binding: ActivityDialogueBinding
    lateinit var viewModel: DialogueViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialogueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(DialogueViewModel::class.java)
        viewModel.dialogueID = intent.getIntExtra("dialogue_id", -1)

        FirebaseMessaging.getInstance()
            .subscribeToTopic("d${viewModel.dialogueID}")

        CoroutineScope(Main).launch {
            binding.companionNameTv.text = withContext(IO) {
                viewModel.getDialogue().second_user_name
            }
            binding.dialogueMessagesRv.layoutManager = LinearLayoutManager(
                this@DialogueActivity,
                LinearLayoutManager.VERTICAL,
                true
            )
            val messages: List<DMessageEntity> =
                withContext(IO) {
                    val newMessages: List<DMessageEntity> =
                        messageProvider.getNewMessages(
                            viewModel.dialogueID,
                            viewModel.getLastMessageID(),
                            context = this@DialogueActivity
                    ) ?: ArrayList()
                    newMessages.forEach { viewModel.saveMessageInDataBase(it) }
                    viewModel.getMessages()
                }
            binding.dialogueMessagesRv.adapter = DialogueRecyclerViewAdapter(
                messages,
                context = this@DialogueActivity,
                this@DialogueActivity::callbackForPhoto,
                this@DialogueActivity::callbackForFile
            )
            adapterIsInitialized = true
        }
    }

    override fun onResume() {
        super.onResume()
        checkingNewMessagesIsPossible = true
        GlobalScope.launch {
            while (checkingNewMessagesIsPossible) {
                if (adapterIsInitialized) {
                    val newMessages: Boolean = messageProvider.checkNewDialogueMessages(
                        viewModel.dialogueID,
                        viewModel.getLastMessageID()
                    )
                    if (newMessages) {
                        withContext(Main) { updateRecyclerView() }
                    }
                    delay(5 * 1000)
                }
            }
        }
        (applicationContext as ConferenceApplication).dialogueID = viewModel.dialogueID
    }

    override fun onPause() {
        super.onPause()
        checkingNewMessagesIsPossible = false
        (applicationContext as ConferenceApplication).dialogueID = 0
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
                                Addition(
                                    fileStream!!.readBytes(),
                                    File(additionUri.path!!).name
                                )
                            messageType = MessageType.MESSAGE_WITH_FILE
                            binding.dialogueAddFileIb.setImageResource(R.drawable.outline_upload_file_24)
                        }

                        PHOTO_LOADING -> {
                            addition = Addition(
                                fileStream!!.readBytes(),
                                "photo"
                            )
                            messageType = MessageType.MESSAGE_WITH_PHOTO
                            binding.dialogueAddFileIb.setImageResource(R.drawable.outline_photo_camera_24)
                        }
                    }
                    binding.dialogueAddFileIb.isEnabled = false
                } catch (e: IOException) {
                    showToast("Не удалось загрузить вложение")
                }
            }
        }
    }

    fun onSendMessageButtonClick(v: View) {
        binding.dialogueMessageSendingPb.isVisible = true

        CoroutineScope(Main).launch { sendMessage() }
    }

    fun onAddFileClick(v: View) = showPopupMenu(v)

    fun onBackClick(v: View) = finish()

    private suspend fun sendMessage() {
        val messageText = binding.dialogueMessageEt.text.toString().trim()
        try {
            when(messageType) {
                MessageType.TEXT_MESSAGE -> {
                    if (messageText.isEmpty()) {
                        binding.dialogueMessageSendingPb.isVisible = false
                        return
                    }
                    withContext(IO) {
                        messageSender.sendTextMessage(
                            context = this@DialogueActivity,
                            messageText,
                            viewModel.dialogueID
                        )
                    }
                }
                MessageType.MESSAGE_WITH_PHOTO -> {
                    withContext(IO) {
                        messageSender.sendMessageWithPhoto(
                            context = this@DialogueActivity,
                            photo = addition!!.file,
                            messageText,
                            viewModel.dialogueID
                        )
                    }
                }
                MessageType.AUDIO_MESSAGE ->
                    withContext(IO) {
                        messageSender.sendAudioMessage(
                            context = this@DialogueActivity,
                            audio = addition!!.file,
                            viewModel.dialogueID
                        )
                    }

                MessageType.MESSAGE_WITH_FILE -> {
                    withContext(IO) {
                        messageSender.sendMessageWithFile(
                            context = this@DialogueActivity,
                            addition!!,
                            viewModel.dialogueID
                        )
                    }
                }
            }
            addition = null
            messageType = MessageType.TEXT_MESSAGE
            if (messageType == MessageType.TEXT_MESSAGE || messageType == MessageType.MESSAGE_WITH_PHOTO) {
                binding.dialogueMessageEt.setText("")
            }

            updateRecyclerView()

            binding.dialogueAddFileIb.apply {
                setImageResource(R.drawable.outline_add_24)
                isEnabled = true
            }
        } catch (e: SendMessageException) {
            val sendingErrorSnackBar = Snackbar.make(
                binding.dialogueMessagesRv,
                "Произошла ошибка",
                Snackbar.LENGTH_SHORT
            )
            sendingErrorSnackBar.setAction("Повторить", this::onSendMessageButtonClick)
            sendingErrorSnackBar.show()
        }
        binding.dialogueMessageSendingPb.isVisible = false
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
                binding.apply {
                    dialogueMessageEt.isEnabled = false
                    dialogueAddFileIb.isEnabled = false
                    dialogueSendMessageBtn.setOnClickListener { v ->
                        GlobalScope.launch {
                            mediaRecorder.stop()
                            val fileStream =
                                contentResolver.openInputStream(Uri.fromFile(File(path)))
                            addition = Addition(
                                fileStream!!.readBytes(),
                                "audio_message"
                            )
                            messageType = MessageType.AUDIO_MESSAGE
                            withContext(Main) {
                                binding.dialogueMessageEt.apply {
                                    isEnabled = true
                                    hint = "Введите сообщение..."
                                    clearAnimation()
                                }

                                binding.dialogueAddFileIb.isEnabled = true

                                v.setOnClickListener(this@DialogueActivity::onSendMessageButtonClick)
                                onSendMessageButtonClick(v)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
            } catch (e: IllegalStateException) {
            }
        } else requestPermission()
    }

    private fun getMediaRecorder(): MediaRecorder {
        return MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        }
    }

    private fun setAudioRecordAnimation() {
        val animation = AnimationUtils.loadAnimation(
            this, R.anim.audio_recording_anim
        )
        animation.repeatMode = Animation.REVERSE
        animation.repeatCount = Animation.INFINITE
        binding.dialogueMessageEt.startAnimation(animation)
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
        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        ), 1)
    }

    private suspend fun updateRecyclerView() {

        val messages: List<DMessageEntity>? = withContext(IO) {
            messageProvider.getNewMessages(
                viewModel.dialogueID,
                viewModel.getLastMessageID(),
                applicationContext
            )
        }
        messages?: return
        withContext(IO) {
            messages.forEach { viewModel.saveMessageInDataBase(it) }
        }
        (binding.dialogueMessagesRv.adapter as DialogueRecyclerViewAdapter).messages =
            withContext(IO) { viewModel.getMessages() }

        binding.dialogueMessagesRv.adapter?.notifyDataSetChanged()

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
                val response: Response = Server.get("/dialogue/getFile/?id=$id")

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
        withContext(Main) {
            Toast.makeText(this@DialogueActivity, text, Toast.LENGTH_LONG).show()
        }
}