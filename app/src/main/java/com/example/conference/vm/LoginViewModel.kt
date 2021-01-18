package com.example.conference.vm

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import com.example.conference.activity.MainActivity
import com.example.conference.json.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginViewModel(val app: Application): AndroidViewModel(app) {

    fun showToast(context: Context, text:String) = Toast.makeText(context, text, Toast.LENGTH_LONG).show()

    suspend fun suspendShowToast(context: Context, text:String) = withContext(Dispatchers.Main) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    fun saveUserInfo(info: UserInfo) {
        val sp = app.getSharedPreferences("user_info", AppCompatActivity.MODE_PRIVATE)
        val ed = sp.edit()
        ed.putInt("user_id", info.id)
        ed.putString("user_name", info.name)
        ed.putString("user_surname", info.surname)
        ed.putString("user_email", info.email)
        ed.apply()
    }

    suspend fun startMainActivity(context: Context) =
        withContext(Dispatchers.Main) { context.startActivity(Intent(context, MainActivity::class.java)) }
}