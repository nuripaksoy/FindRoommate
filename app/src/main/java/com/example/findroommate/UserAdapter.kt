package com.example.findroommate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class UserAdapter(private val userList : ArrayList<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var onClickListener: OnClickListener? = null

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userImage: ShapeableImageView = itemView.findViewById(R.id.userListItemImage)
        val userText: TextView = itemView.findViewById(R.id.userListItemTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_list_item,parent,false)
        return UserViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: User)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentItem = userList[position]
        holder.userText.text = currentItem.fullName
        if(currentItem.profilePhoto!=""){
            Picasso.get().load(currentItem.profilePhoto).into(holder.userImage);
        }

        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, currentItem)
            }
        }
    }
}