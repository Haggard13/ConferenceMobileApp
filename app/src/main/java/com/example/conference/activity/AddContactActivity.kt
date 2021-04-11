package com.example.conference.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.conference.R
import com.example.conference.db.entity.ContactEntity
import com.example.conference.exception.UserFindingException
import com.example.conference.exception.UserNotFoundException
import com.example.conference.server.Server
import com.example.conference.server.users.UsersManager
import com.example.conference.vm.AddContactViewModel
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_contact.*
import kotlinx.android.synthetic.main.contact_item_view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.util.*

class AddContactActivity : AppCompatActivity() {

    private lateinit var vm: AddContactViewModel
    private var userViewStubIsInflate = false
    private val usersManager = UsersManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        vm = ViewModelProvider(this).get(AddContactViewModel::class.java)

        add_contact_back_ib.setOnClickListener { finish() }
    }

    fun onAddUserClick(v: View) {
        find_user_pb.visibility = View.VISIBLE
        val email: String = contact_email_et.text.toString().toLowerCase(Locale.ROOT)
        CoroutineScope(Main).launch {
            try {
                if (vm existContact email) {
                    Snackbar
                        .make(add_contact_sv, "Контакт уже существует", Snackbar.LENGTH_SHORT)
                        .show()
                    return@launch
                }
                val contact: ContactEntity = withContext(IO) {
                    return@withContext usersManager.findUser(email)
                }
                vm addContact contact
                Snackbar
                    .make(add_contact_sv, "Контакт создан", Snackbar.LENGTH_SHORT)
                    .show()
            } catch (e: UserFindingException) {
                Snackbar
                    .make(add_contact_sv, "Проверьте подключение к сети", Snackbar.LENGTH_SHORT)
                    .show()
            } catch (e: UserNotFoundException) {
                Snackbar
                    .make(add_contact_sv, "Пользователь не найден", Snackbar.LENGTH_SHORT)
                    .show()
            } finally {
                find_user_pb.visibility = View.INVISIBLE
            }
        }
    }

    fun onFindUserClick(v: View) {
        find_user_pb.visibility = View.VISIBLE
        val email: String = contact_email_et.text.toString().toLowerCase(Locale.ROOT)
        val scope = CoroutineScope(Main)
        scope.launch {
            try {
                val user: ContactEntity
                withContext(IO) { user = usersManager.findUser(email) }
                if (!userViewStubIsInflate) {
                    user_vs.inflate()
                    contactChoseImg.visibility = View.INVISIBLE
                    userViewStubIsInflate = true
                }
                contact_name_tv.text = user.name
                contact_surname_tv.text = user.surname
                contact_email_tv.text = user.email
                Picasso.get()
                    .load("${Server.baseURL}/user/avatar/download/?id=${user.email.hashCode()}")
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .fit()
                    .centerCrop()
                    .into(contact_avatar_iv)
            } catch(e: UserNotFoundException) {
                Snackbar
                    .make(add_contact_sv, "Пользователь не найден", Snackbar.LENGTH_LONG)
                    .show()
            } catch (e: UserFindingException) {
                Snackbar
                    .make(add_contact_sv, "Проверьте подключение к сети", Snackbar.LENGTH_LONG)
                    .show()
            } finally {
                find_user_pb.visibility = View.INVISIBLE
            }
        }
    }
}