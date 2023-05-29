package com.example.findroommate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.net.toUri
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.findroommate.databinding.ActivityLoginBinding
import com.example.findroommate.databinding.ActivityMainBinding
import com.example.findroommate.databinding.ActivityUserListBinding
import com.example.findroommate.databinding.NavHeaderBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var profilePhotoImageView: CircleImageView
    private lateinit var tvFullName : TextView
    private lateinit var tvEmail : TextView
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference

    private lateinit var recyclerView: RecyclerView
    private lateinit var offerList : ArrayList<Offer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        drawerLayout = binding.drawerLayout
        navigationView = binding.navView

        recyclerView = binding.recyclerViewMain
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        offerList = arrayListOf<Offer>()

        actionBarDrawerToggle = ActionBarDrawerToggle(this,drawerLayout,R.string.nav_open,R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_map -> {
                    val intent = Intent(this, Map::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_addHouse -> {
                    val intent = Intent(this, AddHouse::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_userList -> {
                    val intent = Intent(this, UserList::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_changePassword -> {
                    val intent = Intent(this, ChangePassword::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_logout -> {
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    true
                }
            }
            true
        }
        val headerBinding = NavHeaderBinding.bind(navigationView.getHeaderView(0))
        profilePhotoImageView = headerBinding.navHeaderPhoto
        tvEmail = headerBinding.userEmail
        tvFullName = headerBinding.userFullName

        auth = Firebase.auth
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(uid!!)
        databaseReference.get().addOnSuccessListener {
            Picasso.get().load(it.child("profilePhoto").value.toString().toUri()).into(profilePhotoImageView)
            tvFullName.setText(it.child("fullName").value?.toString())
            tvEmail.setText(it.child("email").value?.toString())
        }

    }

    private fun getOfferList(){
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Offers").child(uid!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (offerSnapshot in snapshot.children){
                        val offer = offerSnapshot.getValue(Offer::class.java)
                        offerList.add(offer!!)
                    }
                    val adapter = OfferAdapter(offerList)
                    recyclerView.adapter = adapter
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}