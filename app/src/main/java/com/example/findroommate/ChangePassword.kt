package com.example.findroommate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ChangePassword : AppCompatActivity() {

    private lateinit var etOldPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var buttonChangePassword: Button

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        etOldPassword = findViewById(R.id.old_password)
        etNewPassword = findViewById(R.id.new_password)
        buttonChangePassword = findViewById(R.id.buttonChangePassword)

        auth = Firebase.auth

        buttonChangePassword.setOnClickListener(){
            changePassword()
        }
    }

    private fun changePassword(){
        if(etOldPassword.text.toString().isNotEmpty() && etNewPassword.text.toString().isNotEmpty()){
            val user = auth.currentUser
            if(user != null){
                val credential = EmailAuthProvider.getCredential(user.email!!,etOldPassword.text.toString())
                user?.reauthenticate(credential)?.addOnCompleteListener(){
                    user?.updatePassword(etNewPassword.text.toString())?.addOnCompleteListener{task->
                        if(task.isSuccessful){
                            Toast.makeText(this,"Şifre başarıyla değiştirildi.", Toast.LENGTH_SHORT)
                            auth.signOut()
                            startActivity(Intent(this@ChangePassword,Login::class.java))
                            finish()
                        }
                    }
                }
            }
        }
    }

}