package com.example.conference.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.databinding.ContactItemViewBinding
import com.example.conference.db.entity.ContactEntity
import com.example.conference.server.api.ConferenceAPIProvider
import com.squareup.picasso.Picasso

class CreateDialogueRecyclerViewAdapter(
    var contacts: List<ContactEntity>,
    private var callback: (email: String, name: String, surname: String) -> Unit
) : RecyclerView.Adapter<CreateDialogueRecyclerViewAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ContactItemViewBinding.inflate(inflater, parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) =
        holder.bind(contacts[position])


    override fun getItemCount() = contacts.size

    inner class ContactViewHolder(
        private val binding: ContactItemViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: ContactEntity) {
            binding.apply {
                contactNameTv.text = contact.name
                contactSurnameTv.text = contact.surname
                contactEmailTv.text = contact.email

                Picasso.get()
                    .load(
                        ConferenceAPIProvider.BASE_URL +
                                "/user/avatar/download/?id=" +
                                contact.email.hashCode()
                    )
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .fit()
                    .centerCrop()
                    .into(contactAvatarIv)
                contactChoseImg.isVisible = false
                root.setOnClickListener {
                    callback.invoke(
                        contactEmailTv.text as String,
                        contactNameTv.text as String,
                        contactSurnameTv.text as String
                    )
                }
            }
        }
    }
}