package com.example.conference.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.adapter.ContactsRecyclerViewAdapter
import com.example.conference.db.ConferenceRoomDatabase
import kotlinx.android.synthetic.main.activity_choose_user.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChooseUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_user)

        Toast.makeText(this, "Выберите пользователя долгим нажатием", Toast.LENGTH_LONG).show()

        chooseUserBackIB.setOnClickListener {setResult(RESULT_CANCELED); finish()}

        GlobalScope.launch {
            addUserRV.layoutManager = LinearLayoutManager(
                this@ChooseUserActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            addUserRV.adapter = ContactsRecyclerViewAdapter(
                ConferenceRoomDatabase.getDatabase(this@ChooseUserActivity).contactDao().getAll()
            ) {
                val i = Intent()
                i.putExtra("user_id", it.hashCode())
                setResult(RESULT_OK, i)
                finish()
            }
        }
    }
}