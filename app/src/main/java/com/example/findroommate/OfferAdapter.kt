package com.example.findroommate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class OfferAdapter(private val offerList : ArrayList<Offer>) : RecyclerView.Adapter<OfferAdapter.OfferViewHolder>() {

    private var onClickListener: OnClickListener? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference

    class OfferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userImage: ShapeableImageView = itemView.findViewById(R.id.offerListItemImage)
        val userText: TextView = itemView.findViewById(R.id.offerListItemTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.offer_list_item,parent,false)
        return OfferViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return offerList.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Offer)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val currentItem = offerList[position]
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(uid!!)

        databaseReference.get().addOnSuccessListener {
            holder.userText.setText(it.child("fullName").value?.toString())
            Picasso.get().load(it.child("profilePhoto").value.toString()).into(holder.userImage)
        }

        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, currentItem)
            }
        }
    }
}