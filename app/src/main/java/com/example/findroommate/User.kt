package com.example.findroommate

import android.net.Uri

data class User(val email : String?="", val password : String?="", val fullName : String?="",
                val department : String?="", val grade : String?="", val phoneNumber : String?=""
                , val profilePhoto : String?="", val state : String?="", val period : String?="", val distance : Double?=0.0):java.io.Serializable
