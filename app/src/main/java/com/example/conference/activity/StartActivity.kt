package com.example.conference.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.conference.R
import com.example.conference.service.Http
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.util.*


class StartActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        auth = FirebaseAuth.getInstance()
    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            delay(1000)

            val currentUser = auth.currentUser
            if (currentUser != null) {
                startActivityIntent(MainActivity::class.java)
                super@StartActivity.finish()
            } else {
                startActivityIntent(LoginActivity::class.java)
                super@StartActivity.finish()
            }
        }
    }

    private fun startActivityIntent(activity: Class<out Any>) = startActivity(
        Intent(
            this,
            activity
        )
    )
}