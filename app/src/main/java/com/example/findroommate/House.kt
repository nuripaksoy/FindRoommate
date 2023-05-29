package com.example.findroommate

data class House(val latitude : Double? = 0.0 , val longitude : Double? = 0.0, val ownerUid : String?="",
                    val ownerFullName : String?="", val body : String?="", val houseMateUid : String?="") :java.io.Serializable
