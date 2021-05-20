package com.example.conference.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.conference.R
import com.example.conference.account.Account
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.util.*


class StartActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Main).launch {
            delay(1000)

            val account = Account(this@StartActivity.applicationContext)
            FirebaseMessaging.getInstance().run {
                subscribeToTopic("u${account.id}")
            }

            val currentUser: FirebaseUser? = auth.currentUser
            if (currentUser != null) {
                startActivityIntent(MainActivity::class.java)
                this@StartActivity.finish()
            } else {
                startActivityIntent(MainActivity::class.java) //fixme
                this@StartActivity.finish()
            }
        }
    }

    private fun startActivityIntent(activity: Class<out Any>) =
        startActivity(Intent(this, activity))
}