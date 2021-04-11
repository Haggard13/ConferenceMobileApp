package com.example.conference.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.conference.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
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
            var token = ""

            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                token = task.result!!
            })

            //ConferenceAPIProvider.conferenceAPI.sendFirebaseMessagingToken(token).execute() fixme

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