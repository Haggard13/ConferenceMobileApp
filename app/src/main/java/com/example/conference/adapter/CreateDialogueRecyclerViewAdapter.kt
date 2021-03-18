package com.example.conference.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.db.entity.ContactEntity
import com.example.conference.service.Server
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.contact_item_view.view.*

class CreateDialogueRecyclerViewAdapter(
    var contacts: List<ContactEntity>,
    private var callback: (email: String, name: String, surname: String) -> Unit
    ): RecyclerView.Adapter<CreateDialogueRecyclerViewAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.contact_item_view, parent, false)
        return ContactViewHolder(v)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var name: TextView = itemView.contactNameTV
        var surname: TextView = itemView.contactSurnameTV
        var email: TextView = itemView.contactEmailTV
        var avatar: ImageView = itemView.contactAvatarIV
        fun bind(c: ContactEntity) {
            name.text = c.name
            surname.text = c.surname
            email.text = c.email
            Picasso.get()
                .load("${Server.baseURL}/user/avatar/download/?id=${c.email.hashCode()}")
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            itemView.contactChoseImg.visibility = View.INVISIBLE
            itemView.setOnClickListener {
                callback.invoke(email.text as String, name.text as String, surname.text as String)
            }
        }
    }
}