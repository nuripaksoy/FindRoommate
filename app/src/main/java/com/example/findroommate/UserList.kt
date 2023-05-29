package com.example.findroommate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.findroommate.databinding.ActivityLoginBinding
import com.example.findroommate.databinding.ActivityUserListBinding
import com.google.firebase.database.*

class UserList : AppCompatActivity() {
    private lateinit var binding : ActivityUserListBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userList : ArrayList<User>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        userList = arrayListOf<User>()
        getUserList()

    }

    private fun getUserList(){
        databaseReference = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        val user = userSnapshot.getValue(User::class.java)
                        userList.add(user!!)
                    }
                    val adapter = UserAdapter(userList)
                    recyclerView.adapter = adapter
                    getUserDetailView(adapter)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getUserDetailView(adapter: UserAdapter){
        adapter.setOnClickListener(object : UserAdapter.OnClickListener{
            override fun onClick(position: Int, model: User) {
                val intent = Intent(this@UserList, UserDetailView::class.java)
                intent.putExtra(NEXT_SCREEN, model)
                startActivity(intent)
            }
        })
    }

    companion object{
        val NEXT_SCREEN="details_screen"
    }
}