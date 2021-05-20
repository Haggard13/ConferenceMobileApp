package com.example.conference.fragment

import android.app.Activity.MODE_PRIVATE
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.R
import com.example.conference.account.Account
import com.example.conference.activity.AddContactActivity
import com.example.conference.activity.LoginActivity
import com.example.conference.adapter.ContactsRecyclerViewAdapter
import com.example.conference.databinding.FragmentProfileBinding
import com.example.conference.db.entity.ContactEntity
import com.example.conference.exception.ChangeAvatarException
import com.example.conference.exception.LoadImageException
import com.example.conference.file.Addition
import com.example.conference.server.Server
import com.example.conference.server.api.ConferenceAPIProvider
import com.example.conference.server.users.UsersManager
import com.example.conference.vm.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


class ProfileFragment : Fragment() {

    private val usersManager= UsersManager()
    private var binding: FragmentProfileBinding? = null
    private lateinit var vm: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val account = Account(activity!!.applicationContext)

        binding?.apply {
            exitIb.setOnClickListener(this@ProfileFragment::onExitClick)

            //emailTV.text = account.email
            //nameTV.text = account.name
            //surnameTV.text = account.surname

            addContactIB.setOnClickListener(this@ProfileFragment::onAddContactClick)

            avatarIV.setOnLongClickListener(this@ProfileFragment::onChangeAvatarLongClick)
            Picasso.get()
                .load(Server.baseURL + "/user/avatar/download/?id=" + account.id)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatarIV)

            contactRV.apply {
                layoutManager = LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
                vm.viewModelScope.launch {
                    val contacts: List<ContactEntity> = vm.getAllContacts()
                    adapter = ContactsRecyclerViewAdapter(contacts, this@ProfileFragment::showContactDeletingDialog)
                    vm.updateContactsCount()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.viewModelScope.launch {
            if (vm.getContactsCount() != vm.contactsCount) {
                (binding?.contactRV?.adapter as ContactsRecyclerViewAdapter).apply {
                    contacts = vm.getAllContacts()
                    notifyDataSetChanged()
                }
                vm.updateContactsCount()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == RESULT_OK) {
            CoroutineScope(Main).launch {
                try {
                    val imageUri = data?.data ?: throw LoadImageException()
                    withContext(IO) {
                        val fileStream = activity!!.contentResolver.openInputStream(imageUri)
                        val allBytes = fileStream!!.readBytes()
                        usersManager.changeUserAvatar(Addition(allBytes, "${Account(activity!!.applicationContext).id}"))
                    }

                    withContext(Main) {
                        Picasso.get()
                            .load(
                                ConferenceAPIProvider.BASE_URL +
                                        "/user/avatar/download/?id=" +
                                        Account(activity!!.applicationContext).id
                            )
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .fit()
                            .centerCrop()
                            .into(binding?.avatarIV)
                    }
                    binding?.root?.let {
                        Snackbar.make(it, "Фотография изменена", Snackbar.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    when (e) {
                        is IOException, is LoadImageException ->
                            binding?.root?.let {
                                Snackbar.make(it, "Ошибка загрузки фотографии", Snackbar.LENGTH_SHORT).show()
                            }
                        is ChangeAvatarException ->
                            binding?.root?.let {
                                Snackbar.make(it, "Проверьте подключение к сети", Snackbar.LENGTH_SHORT).show()
                            }
                        else ->
                            throw e
                    }
                }
            }
        }
    }

    private fun showChangeAvatarDialog() {
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
        activity?.getSharedPreferences("user_info", MODE_PRIVATE)?.edit()?.clear()?.apply()
        vm.viewModelScope.launch {
            vm.databaseClear()
            startActivity(Intent(activity, LoginActivity::class.java))
            activity?.finish()
        }
    }

    private fun onAddContactClick(v: View) =
        startActivity(Intent(activity, AddContactActivity::class.java))


    private fun onChangeAvatarLongClick(v: View): Boolean {
        showChangeAvatarDialog()
        return true
    }

    private fun showContactDeletingDialog(email: String) {
        val deleteContactDialog = AlertDialog.Builder(activity)
        deleteContactDialog.setTitle("Удалить контакт?")
            .setPositiveButton("Да") { _, _ ->
                vm.viewModelScope.launch {
                    vm.deleteContact(email)
                    (binding?.contactRV?.adapter as ContactsRecyclerViewAdapter).apply {
                        contacts = vm.getAllContacts()
                        notifyDataSetChanged()
                    }
                }
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.cancel()
            }
            .create().show()
    }
}