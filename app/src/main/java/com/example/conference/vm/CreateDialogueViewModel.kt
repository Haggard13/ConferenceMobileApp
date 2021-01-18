package com.example.conference.vm

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateDialogueViewModel(app: Application): AndroidViewModel(app) {
    private val sp = app.getSharedPreferences("user_info", MODE_PRIVATE)
    fun getUserID() = sp.getInt("user_id", 0)
    fun getUserEmail() = sp.getString("user_email", "")
    fun getUserName() = sp.getString("user_name", "")
    fun getUserSurname() = sp.getString("user_surname", "")

    suspend fun showToast(context: Context, text: String) = withContext(Dispatchers.Main) { Toast.makeText(context, text, Toast.LENGTH_LONG).show() }
}