package com.example.conference.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.db.entity.ContactEntity
import com.example.conference.server.Server
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.contact_item_view.view.*


class ContactsRecyclerViewAdapter(
    var contacts: List<ContactEntity>,
    private var callback: (email: String) -> Unit): RecyclerView.Adapter<ContactsRecyclerViewAdapter.ContactViewHolder>() {

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

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var avatar: ImageView = itemView.contact_avatar_iv
        var name: TextView = itemView.contact_name_tv
        var surname: TextView = itemView.contact_surname_tv
        var email: TextView = itemView.contact_email_tv
        fun bind(c: ContactEntity) {
            Picasso.get()
                .load(Server.baseURL + "/user/avatar/download/?id=" + c.email.hashCode())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            name.text = c.name
            surname.text = c.surname
            email.text = c.email
            itemView.contactChoseImg.visibility = View.INVISIBLE
            itemView.setOnLongClickListener {
                callback.invoke(c.email)
                true
            }
        }
    }
}