package com.example.conference.vm

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.dao.ContactDao
import com.example.conference.db.entity.ContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddContactViewModel(app: Application): AndroidViewModel(app) {
    private val contactDao: ContactDao = ConferenceRoomDatabase.getDatabase(app).contactDao()

    suspend fun addContact(c: ContactEntity) = contactDao.insert(c)
    suspend fun existContact(email: String) = contactDao.existContact(email)

    suspend fun showToast(context: Context, text: String) = withContext(Dispatchers.Main) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

}