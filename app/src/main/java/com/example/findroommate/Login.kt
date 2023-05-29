package com.example.findroommate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.findroommate.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var loginButton : Button
    private lateinit var textView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loginButton = binding.loginButton
        textView = binding.loginTextView2
        auth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener(){
            login()
        }

        textView.setOnClickListener(){
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun login(){
        val email = binding.loginEmail.text.toString()
        val password = binding.loginPassword.text.toString()

        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{task ->
            if(task.isSuccessful){
                val intent = Intent(this, MainActivity::class.java)
                Toast.makeText(applicationContext, "Giriş işlemi başarılı.", Toast.LENGTH_LONG).show()
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener{ exception ->
            Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }
}