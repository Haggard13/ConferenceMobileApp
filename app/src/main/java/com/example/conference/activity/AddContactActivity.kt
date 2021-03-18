package com.example.conference.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.conference.R
import com.example.conference.db.entity.ContactEntity
import com.example.conference.exception.GetUserInfoException
import com.example.conference.json.UserInfo
import com.example.conference.service.Server
import com.example.conference.vm.AddContactViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_add_contact.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.*

class AddContactActivity : AppCompatActivity() {

    private lateinit var vm: AddContactViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        vm = ViewModelProvider(this).get(AddContactViewModel::class.java)

        addContactBackIB.setOnClickListener {
            finish()
        }

        addContactBtn.setOnClickListener {
            GlobalScope.launch {
                try {
                    val email = contactEmailET.text.toString().toLowerCase(Locale.ROOT)
                    if (vm.existContact(email) > 0) {
                        vm.showToast(this@AddContactActivity, "Контакт уже существует")
                        return@launch
                    }
                    val r = Server.get("/user/info/?id=" + email.hashCode())
                    val getResult = r.body?.string()
                    if (!r.isSuccessful || getResult == "null")
                        throw GetUserInfoException()
                    val contactInfo: UserInfo =
                        Gson().fromJson(getResult, UserInfo::class.java)
                    val contact = ContactEntity(email, contactInfo.name, contactInfo.surname)
                    vm.addContact(contact)
                    vm.showToast(this@AddContactActivity, "Успешно")
                } catch (e: ConnectException) {
                    vm.showToast(this@AddContactActivity, "Проверьте подключение к сети")
                } catch (e: SocketTimeoutException) {
                    vm.showToast(this@AddContactActivity, "Проверьте подключение к сети")
                } catch (e: GetUserInfoException) {
                    vm.showToast(this@AddContactActivity, "Данный аккаунт не зарегистрирован")
                }
            }
        }
    }
}