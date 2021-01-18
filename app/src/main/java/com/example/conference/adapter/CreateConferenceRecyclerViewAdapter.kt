package com.example.conference.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.db.entity.ContactEntity
import com.example.conference.service.Http
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.contact_item_view.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class CreateConferenceRecyclerViewAdapter(
    var contacts: List<ContactEntity>,
    private var callbackOn: (email: String) -> Unit,
    private var callbackOff: (email: String) -> Unit): RecyclerView.Adapter<CreateConferenceRecyclerViewAdapter.ContactViewHolder>() {

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
                .load("${Http.baseURL}/user/avatar/download/?id=${c.email.hashCode()}")
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            itemView.contactChoseImg.visibility = View.INVISIBLE
            itemView.setOnClickListener {
                when (itemView.contactChoseImg.visibility) {
                    View.VISIBLE -> {
                        callbackOff.invoke(c.email)
                        itemView.contactChoseImg.visibility = View.INVISIBLE
                    }
                    View.INVISIBLE -> {
                        callbackOn.invoke(c.email)
                        itemView.contactChoseImg.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}