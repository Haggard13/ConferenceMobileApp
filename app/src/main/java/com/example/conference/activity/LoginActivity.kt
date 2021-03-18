package com.example.conference.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.conference.R
import com.example.conference.exception.LoginException
import com.example.conference.json.UserInfo
import com.example.conference.service.Server
import com.example.conference.vm.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.*


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var vm: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        vm = ViewModelProvider(this).get(LoginViewModel::class.java)
    }

    fun onInputBtnClick(v: View) {
        val email = emailET.text.toString().toLowerCase(Locale.ROOT)
        val password = passwordET.text.toString()
        val id = email.hashCode()

        if (email.isEmpty() || password.isEmpty()) {
            vm.showToast(this, "Введите логин и пароль")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (!auth.currentUser!!.isEmailVerified) {
                        vm.showToast(this, "Email не подтвержден")
                        auth.signOut()
                    } else {
                        GlobalScope.launch {
                            try {
                                val r = Server.get(String.format("/user/info/?id=%s", id))
                                if (!r.isSuccessful) throw LoginException()
                                val info = Gson().fromJson(r.body?.string(), UserInfo::class.java)
                                vm.saveUserInfo(info)
                                vm.startMainActivity(this@LoginActivity)
                            } catch (e: ConnectException) {
                                vm.suspendShowToast(this@LoginActivity, "Проверьте подключение к сети")
                                auth.signOut()
                            } catch (e: SocketTimeoutException) {
                                vm.suspendShowToast(this@LoginActivity, "Проверьте подключение к сети")
                                auth.signOut()
                            } catch (e: LoginException) {
                                vm.suspendShowToast(this@LoginActivity, "Что-то пошло не так")
                                auth.signOut()
                            }
                        }
                    }
                } else {
                    vm.showToast(this, "Ошибка аутентификации")
                    auth.signOut()
                }
            }
    }

    fun onRegistrationBtnClick(v: View) {
        startActivity(Intent(this, RegistrationActivity::class.java))
    }
}