package com.example.findroommate

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.example.findroommate.databinding.ActivityProfileBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class Profile : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding
    private lateinit var btnChangePhoto : Button
    private lateinit var circularImageView : CircleImageView
    private lateinit var btnUpdateProfile : Button
    private lateinit var etDepartment : EditText
    private lateinit var etGrade : EditText
    private lateinit var etPhoneNumber : EditText
    private lateinit var etState : EditText
    private lateinit var etDistance : EditText
    private lateinit var etPeriod : EditText
    private lateinit var imageUri : String
    private lateinit var tvFullName : TextView
    private lateinit var tvEmail : TextView

    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        btnChangePhoto = binding.profileBtnChangePhoto
        circularImageView = binding.profilePhoto
        btnUpdateProfile = binding.profileBtnUpdateUser
        etDepartment = binding.profileEtDepartment
        etDistance = binding.profileEtDistance
        etState = binding.profileEtState
        etGrade = binding.profileEtGrade
        etPhoneNumber = binding.profileEtPhoneNumber
        etPeriod = binding.profileEtPeriod
        tvEmail = binding.tvEmail
        tvFullName = binding.tvFullName

        auth = Firebase.auth

        getUserFromDatabase()

        btnChangePhoto.setOnClickListener(){
            uploadImage()
        }

        btnUpdateProfile.setOnClickListener(){
            updateUserData()
        }


    }

    private fun getUserFromDatabase(){
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(uid!!)

        databaseReference.get().addOnSuccessListener {
            etDepartment.setText(it.child("department").value?.toString())
            etDistance.setText(it.child("distance").value?.toString())
            etState.setText(it.child("state").value?.toString())
            etPhoneNumber.setText(it.child("phoneNumber").value?.toString())
            etPeriod.setText(it.child("period").value?.toString())
            etGrade.setText(it.child("grade").value?.toString())
            tvFullName.setText(it.child("fullName").value?.toString())
            tvEmail.setText(it.child("email").value?.toString())
            Picasso.get().load(it.child("profilePhoto").value.toString().toUri()).into(circularImageView)
        }
    }

    private fun updateUserData(){
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(uid!!)

        databaseReference.get().addOnSuccessListener {
            val email = it.child("email").value?.toString()
            val password = it.child("password").value?.toString()
            val fullName = it.child("fullName").value?.toString()
            val university = it.child("university").value?.toString()
            val department = etDepartment.text.toString()
            val grade = etGrade.text.toString()
            val phoneNumber = etPhoneNumber.text.toString()
            val state = etState.text.toString()
            val distanceText = etDistance.text.toString()
            val distance = distanceText.toDoubleOrNull()
            val period = etPeriod.text.toString()


            val user = User(email,password,fullName,department,grade,phoneNumber, imageUri , state, period, distance)
            databaseReference.setValue(user)
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf<CharSequence>("Kamera", "Galeri")

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Fotoğraf Kaynağını Seçin")
        builder.setItems(options) { _, item ->
            when (item) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, 2)
    }

    private fun openGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    private fun uploadImage() {
        showImageSourceDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1 -> {
                    circularImageView.setImageURI(data?.data)
                    data?.data?.let { addImageToFirebaseStorage(it) }
                }
                2 -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    circularImageView.setImageBitmap(imageBitmap)
                    data?.data?.let { addImageToFirebaseStorage(it) }
                }
            }
        }
    }

    private fun addImageToFirebaseStorage(fileUri: Uri){
        val uid = auth.currentUser?.uid
        if (fileUri != null) {
            val fileName = uid.toString() +".jpg"

            val refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")

            refStorage.putFile(fileUri)
                .addOnSuccessListener(
                    OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                            imageUri = it.toString()
                        }
                    })

                ?.addOnFailureListener(OnFailureListener { e ->
                    print(e.message)
                })
        }
    }

}