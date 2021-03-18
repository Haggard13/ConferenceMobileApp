package com.example.conference.fragment

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.conference.R
import com.example.conference.activity.AddContactActivity
import com.example.conference.activity.LoginActivity
import com.example.conference.exception.LoadImageException
import com.example.conference.service.Server
import com.example.conference.vm.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.ConnectException
import java.net.SocketTimeoutException


class ProfileFragment : Fragment() {

    private lateinit var vm: ProfileViewModel
    private var countContacts: Int = 0
    private lateinit var email: String
    private lateinit var name: String
    private lateinit var surname: String
    private lateinit var avatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(this).get(ProfileViewModel::class.java)
        userInfoInit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_profile, container, false)

        v.findViewById<Button>(R.id.exitBtn).setOnClickListener(this::onExitClick)

        v.findViewById<TextView>(R.id.emailTV).text = email
        v.findViewById<TextView>(R.id.surnameTV).text = surname
        v.findViewById<TextView>(R.id.nameTV).text = name

        v.findViewById<ImageButton>(R.id.addContactIB).setOnClickListener(this::onAddContactClick)

        avatar = v.findViewById(R.id.avatarIV)
        avatar.setOnLongClickListener(this::onChangeAvatarLongClick)
        Picasso.get()
            .load(Server.baseURL + "/user/avatar/download/?id=" + vm.sp.getInt("user_id", 0))
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .fit()
            .centerCrop()
            .into(avatar)

        vm.initAdapter(activity!!, v.findViewById(R.id.contactRV))

        return v
    }

    override fun onResume() {
        super.onResume()
        vm.viewModelScope.launch {
            if (vm.countContacts() != countContacts) {
                vm.updateContacts()
                countContacts = vm.countContacts()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == RESULT_OK) {
            GlobalScope.launch {
                try {
                    val imageUri = data?.data ?: throw LoadImageException()
                    val fileStream = activity!!.contentResolver.openInputStream(imageUri)
                    val allBytes = fileStream!!.readBytes()
                    val result = Server.sendNewUserAvatar(vm.getUserID(), allBytes)

                    if (result == -1)
                        throw LoadImageException()
                    withContext(Main) {
                        Picasso.get()
                            .load(
                                Server.baseURL + "/user/avatar/download/?id=" + vm.sp.getInt(
                                    "user_id",
                                    0
                                )
                            )
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .fit()
                            .centerCrop()
                            .into(avatar)
                    }
                    vm.showToast("Успешно")
                } catch (e: LoadImageException) {
                    vm.showToast("Не удалось загрузить изображение")
                } catch (e: ConnectException) {
                    vm.showToast("Не удалось загрузить изображение")
                } catch (e: SocketTimeoutException) {
                    vm.showToast("Не удалось загрузить изображение")
                } catch (e: IOException) {
                    vm.showToast("Не удалось загрузить изображение")
                }
            }
        }
    }

    private fun userInfoInit() {
        email = vm.sp.getString("user_email", "").toString()
        name = vm.sp.getString("user_name", "").toString()
        surname = vm.sp.getString("user_surname", "").toString()
    }

    private fun changeAvatarDialog() {
        AlertDialog.Builder(activity!!).setTitle("Изменить фотографию?")
            .setPositiveButton("Да") { _, _ ->
                val pickIntent = Intent(Intent.ACTION_PICK)
                pickIntent.type = "image/*"
                startActivityForResult(pickIntent, 0)
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.cancel()
            }
            .create().show()
    }

    private fun onExitClick(v: View) {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        vm.sp.edit().clear().apply()
        GlobalScope.launch {
            vm.databaseClear()
            startActivity(Intent(activity, LoginActivity::class.java))
            activity?.finish()
        }
    }

    private fun onAddContactClick(v: View) {
        startActivity(Intent(activity, AddContactActivity::class.java))
    }

    private fun onChangeAvatarLongClick(v: View): Boolean {
        changeAvatarDialog()
        return true
    }
}