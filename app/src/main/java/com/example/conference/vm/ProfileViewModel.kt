package com.example.conference.vm

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.adapter.ContactsRecyclerViewAdapter
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.dao.ContactDao
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(val app: Application): AndroidViewModel(app) {
    private val contactDao: ContactDao = ConferenceRoomDatabase.getDatabase(app).contactDao()
    lateinit var adapter: ContactsRecyclerViewAdapter

    private suspend fun getAllContacts() = contactDao.getAll()
    private suspend fun deleteContact(email: String) = contactDao.deleteCortege(email)
    suspend fun countContacts() = contactDao.count()
    fun getUserID() = sp.getInt("user_id", 0)
    val sp: SharedPreferences = app.getSharedPreferences("user_info", MODE_PRIVATE)

    suspend fun showToast(text: String) = withContext(Main) { Toast.makeText(app, text, LENGTH_LONG).show() }

    fun databaseClear() =
        ConferenceRoomDatabase.getDatabase(app).clearAllTables()

    suspend fun updateContacts() {
        adapter.contacts = getAllContacts()
        adapter.notifyDataSetChanged()
    }

    fun initAdapter(activity: FragmentActivity, rv: RecyclerView) {
        this.viewModelScope.launch {
            adapter =
                ContactsRecyclerViewAdapter(getAllContacts()) { deleteContactDialog(activity, it) }
            withContext(Main) {
                rv.layoutManager = LinearLayoutManager(activity)
                rv.adapter = adapter
            }
        }
    }

    private fun deleteContactDialog(activity: FragmentActivity, email: String) {
        val deleteContactDialog = AlertDialog.Builder(activity)
        deleteContactDialog.setTitle("Удалить контакт?")
            .setPositiveButton("Да") { _, _ ->
                viewModelScope.launch {
                    deleteContact(email)
                    adapter.contacts = getAllContacts()
                    adapter.notifyDataSetChanged()
                }
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.cancel()
            }
            .create().show()
    }
}