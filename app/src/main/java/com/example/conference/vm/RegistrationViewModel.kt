package com.example.conference.vm

import android.app.Application
import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

class RegistrationViewModel(val app: Application): AndroidViewModel(app) {
    fun showToast(text: String) = Toast.makeText(app, text, LENGTH_LONG).show()
    suspend fun suspendShowToast(text: String) = withContext(Main) { Toast.makeText(app, text, LENGTH_LONG).show() }
}