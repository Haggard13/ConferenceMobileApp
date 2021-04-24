package com.example.conference.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.databinding.ContactItemViewBinding
import com.example.conference.db.entity.ContactEntity
import com.example.conference.server.Server
import com.squareup.picasso.Picasso

class CreateConferenceRecyclerViewAdapter(
    var contacts: List<ContactEntity>,
    private var callbackAddMember: (email: String) -> Unit,
    private var callbackRemoveMember: (email: String) -> Unit
) : RecyclerView.Adapter<CreateConferenceRecyclerViewAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ContactItemViewBinding.inflate(inflater, parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int = contacts.size

    inner class ContactViewHolder(private val binding: ContactItemViewBinding
    ) : RecyclerView.ViewHolder(binding.root){

        fun bind(contact: ContactEntity) =
            binding.apply {
                contactNameTv.text = contact.name
                contactSurnameTv.text = contact.surname
                contactEmailTv.text = contact.email

                contactChoseImg.isVisible = false
                root.setOnClickListener {
                    when (contactChoseImg.isVisible) {
                        true -> {
                            callbackRemoveMember.invoke(contact.email)
                            contactChoseImg.isVisible = false
                        }
                        false -> {
                            callbackAddMember.invoke(contact.email)
                            contactChoseImg.isVisible = true
                        }
                    }
                }
                Picasso.get()
                    .load("${Server.baseURL}/user/avatar/download/?id=${contact.email.hashCode()}")
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .fit()
                    .centerCrop()
                    .into(binding.contactAvatarIv)
            }
    }
}