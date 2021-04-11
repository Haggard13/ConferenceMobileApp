package com.example.conference.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.conference.R
import com.example.conference.exception.RegistrationException
import com.example.conference.server.Server
import com.example.conference.vm.RegistrationViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.util.*

class RegistrationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var vm: RegistrationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        auth = FirebaseAuth.getInstance()
        vm = ViewModelProvider(this).get(RegistrationViewModel::class.java)
    }

    fun onRegistrationBtnClick(v: View) {
        val email = emailRegET.text.toString().toLowerCase(Locale.ROOT)
        val password = passwordRegET.text.toString()
        val passwordRepeat = passwordRepeatRegET.text.toString()
        val name = nameTE.text.toString()
        val surname = surnameTE.text.toString()
        val id = email.hashCode()

        if (email.isEmpty() || password.isEmpty() || passwordRepeat.isEmpty()) {
            vm.showToast("Введите данные")
            return
        }

        if (password != passwordRepeat) {
            vm.showToast("Пароли не совпадают")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    GlobalScope.launch {
                        try {
                            val r = Server.get(
                                String.format(
                                    "/user/registration/?id=%s&email=%s&name=%s&surname=%s",
                                    id, URLEncoder.encode(email, "UTF-8"), name, surname
                                )
                            )
                            if (!r.isSuccessful || r.body!!.string().toInt() != 1)
                                throw RegistrationException()
                            vm.suspendShowToast(
                                "На ваш E-Mail отправленно письмо для подтверждения почты"
                            )
                            auth.signOut()
                            this@RegistrationActivity.finish()
                        } catch (e: ConnectException) {
                            vm.suspendShowToast(
                                "Проверьте подкючение к сети")
                            auth.signOut()
                        } catch (e: SocketTimeoutException) {
                            vm.suspendShowToast(
                                "Проверьте подкючение к сети")
                            auth.signOut()
                        } catch (e: RegistrationException) {
                            vm.suspendShowToast(
                                "Ошибка регистрации")
                            auth.currentUser!!.delete()
                            auth.signOut()
                        }
                        this@RegistrationActivity.finish()
                    }
                    auth.currentUser!!.sendEmailVerification()
                } else {
                    vm.showToast("Ошибка регистрации")
                }
                auth.signOut()
            }
    }
}