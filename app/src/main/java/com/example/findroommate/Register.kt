package com.example.findroommate

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.findroommate.databinding.ActivityLoginBinding
import com.example.findroommate.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.oAuthCredential
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app

class Register : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var registerButton : Button
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()
        registerButton = binding.registerButton

        registerButton.setOnClickListener(){
            register()
        }

    }

    private fun register(){
        val email = binding.registerEmailEt.text.toString()
        val password = binding.registerPasswordEt.text.toString()

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
            if(it.isSuccessful){
                createUser()
                Toast.makeText(this, "Kayıt başarılı bir şekilde oluşturuldu.", Toast.LENGTH_SHORT).show()
                emailVerification()
                val intent = Intent(this,Login::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener{
            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun emailVerification(){
        val user = auth.currentUser

        if(user != null){
            user.sendEmailVerification().addOnCompleteListener{
                if(it.isSuccessful){
                    Toast.makeText(this, "Email doğrulama maili gönderildi.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createUser(){
        val uid = auth.currentUser?.uid
        val fullName = binding.registerNameEt.text.toString() + " " + binding.registerLastNameEt.text.toString()
        val email = binding.registerEmailEt.text.toString()
        val password = binding.registerPasswordEt.text.toString()
        val phoneNumber = binding.registerPhoneNumberEt.text.toString()
        val department = binding.registerDepartmentEt.text.toString()
        val grade = binding.registerGradeEt.text.toString()
        val state = binding.registerStateEt.text.toString()
        val period = binding.registerPeriodEt.text.toString()

        databaseReference = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")

        val user = User(email,password,fullName,department,grade,phoneNumber, Uri.EMPTY.toString(),state, period, 0.0)

        if(uid!=null){
            databaseReference.child(uid).setValue(user)
        }
    }
}