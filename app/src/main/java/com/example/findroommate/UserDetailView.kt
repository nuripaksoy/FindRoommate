package com.example.findroommate

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.net.toUri
import com.example.findroommate.databinding.ActivityUserDetailViewBinding
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserDetailView : AppCompatActivity() {

    private lateinit var binding : ActivityUserDetailViewBinding
    private lateinit var tvDepartment : TextView
    private lateinit var tvGrade : TextView
    private lateinit var tvPhoneNumber : TextView
    private lateinit var tvState : TextView
    private lateinit var tvDistance : TextView
    private lateinit var tvPeriod : TextView
    private lateinit var tvFullName : TextView
    private lateinit var profilePhoto : CircleImageView
    private lateinit var tvEmail : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        tvDepartment = binding.userDTOTvDepartment
        tvDistance = binding.userDTOTvDistance
        tvEmail = binding.tvEmail
        tvFullName = binding.tvFullName
        tvGrade = binding.userDTOTvGrade
        tvPeriod = binding.userDTOTvPeriod
        tvPhoneNumber = binding.userDTOTvPhoneNumber
        tvState = binding.userDTOTvState
        profilePhoto = binding.profilePhoto

        var userList : User?=null

        if(intent.hasExtra(UserList.NEXT_SCREEN)){
            userList = intent.getSerializableExtra(UserList.NEXT_SCREEN) as User
        }

        if(userList!=null){
            tvEmail.setText(userList.email)
            tvPhoneNumber.setText(userList.phoneNumber)
            tvPeriod.setText(userList.period)
            tvGrade.setText(userList.grade)
            tvFullName.setText(userList.fullName)
            tvDistance.setText(userList.distance.toString())
            tvDepartment.setText(userList.department)
            Picasso.get().load(userList.profilePhoto).into(profilePhoto)
            tvState.setText(userList.state)
            Picasso.get().load(userList.profilePhoto.toString().toUri()).into(profilePhoto);
        }

        tvEmail.setOnClickListener(){
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse( "mailto:"+ tvEmail.text.toString())
                putExtra(Intent.EXTRA_SUBJECT, "Konu")
                putExtra(Intent.EXTRA_TEXT, "İçerik")
            }
            startActivity(intent)
        }

        tvPhoneNumber.setOnClickListener(){
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${tvPhoneNumber.text.toString()}")
            }
            startActivity(intent)
        }

    }
}