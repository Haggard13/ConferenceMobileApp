package com.example.conference.vm

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.conference.activity.PhotoReviewerActivity
import com.example.conference.adapter.DialogueRecyclerViewAdapter
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.data.SenderEnum
import com.example.conference.db.entity.DMessageEntity
import com.example.conference.db.entity.DialogueEntity
import com.example.conference.exception.MessageUpdateException
import com.example.conference.json.DMessageList
import com.example.conference.service.Http
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URLEncoder
import kotlin.properties.Delegates

class DialogueViewModel (val app: Application) : AndroidViewModel(app){
    private val dialogueDao = ConferenceRoomDatabase.getDatabase(app).dialogueDao()
    private val dMessageDao = ConferenceRoomDatabase.getDatabase(app). dMessageDao()

    lateinit var messages: List<DMessageEntity>
    lateinit var adapter: DialogueRecyclerViewAdapter
    var dialogueID by Delegates.notNull<Int>()
    private val sp: SharedPreferences = app.getSharedPreferences("user_info", MODE_PRIVATE)

    //region DB Methods
    suspend fun getDialogue(): DialogueEntity = dialogueDao.getDialogue(dialogueID)
    private suspend fun getMessages(): List<DMessageEntity> = dMessageDao.getMessages(dialogueID)
    suspend fun saveMessage(m: DMessageEntity) = dMessageDao.insert(m)
    private suspend fun getLastMessageID(dialogue_id: Int) = dMessageDao.getLastMessageID(dialogue_id)
    private suspend fun getMessagesCount(dialogue_id: Int) = dMessageDao.getMessagesCount(dialogue_id)
    //endregion

    suspend fun updateMessages() {
        try {
            val r = Http.get(
                String.format(
                    "/dialogue/getNewMessages/?dialogue_id=%s&" +
                            "last_message_id=%s&" +
                            "user_id=%s",
                    dialogueID,
                    if (getMessagesCount(dialogueID) == 0)
                        0
                    else
                        getLastMessageID(dialogueID),
                    sp.getInt("user_id", 0)
                )
            )
            val result = r.body?.string() ?: ""

            if (r.isSuccessful && result.isNotBlank()) {
                val json = Gson().fromJson(result, DMessageList::class.java)
                json.list.forEach {
                    dMessageDao.insert(it)
                }
            }
            updateRV()
        } catch (e: ConnectException) {
        } catch (e: SocketTimeoutException) {
        } catch (e: MessageUpdateException) {
        }
    }

    fun messagesIsInitialize() = ::messages.isInitialized

    suspend fun updateRV() {
        messages = dMessageDao.getMessages(dialogueID)
        adapter.messages = messages
        withContext(Main) {
            adapter.notifyDataSetChanged()
        }
    }

    suspend fun initMessages() {
        messages = getMessages()
    }

    fun initAdapter(context: Context) {
        adapter = DialogueRecyclerViewAdapter(messages, context) {
            val intent = Intent(context, PhotoReviewerActivity::class.java)
            intent.putExtra("photo_id", it)
            intent.putExtra("is_conference", false)
            context.startActivity(intent)
        }
    }

    fun generateURL(messageText: String, date: Long) = String.format("/dialogue/sendMessage/?text=%s&" +
            "dialogue_id=%s&" +
            "date_time=%s&" +
            "sender_id=%s&" +
            "sender_name=%s&" +
            "sender_surname=%s",
        URLEncoder.encode(messageText, "UTF-8"),
        dialogueID,
        date,
        getUserID(),
        URLEncoder.encode(getUserName(), "UTF-8"),
        URLEncoder.encode(getUserSurname(), "UTF-8")
    )

    fun createDMessageEntity(messageId: Int, messageText: String, date: Long, type: Int) = DMessageEntity(
        messageId,
        messageText,
        date,
        getUserID(),
        dialogueID,
        getUserName()?: "",
        getUserSurname()?: "",
        SenderEnum.USER.ordinal,
        type
    )

    suspend fun showToast(text: String) = withContext(Main) { Toast.makeText(app, text, Toast.LENGTH_LONG).show() }

    private fun getUserID(): Int = sp.getInt("user_id", 0)
    private fun getUserName(): String? = sp.getString("user_name", "")
    private fun getUserSurname(): String? = sp.getString("user_surname", "")
}