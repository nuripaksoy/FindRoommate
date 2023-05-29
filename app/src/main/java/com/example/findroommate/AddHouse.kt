package com.example.findroommate

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.findroommate.databinding.ActivityAddHouseBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class AddHouse : FragmentActivity() , OnMapReadyCallback {

    private lateinit var binding : ActivityAddHouseBinding
    private lateinit var searchView: androidx.appcompat.widget.SearchView
    private lateinit var marker : Marker
    private lateinit var gMap : GoogleMap
    private lateinit var map : FrameLayout
    private lateinit var currentLocation : Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101
    private lateinit var btnAddHouse : Button

    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHouseBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        map = binding.map
        searchView = binding.search
        searchView.clearFocus()

        btnAddHouse = binding.btnAddHouse
        auth = Firebase.auth

        btnAddHouse.setOnClickListener(){
            val latitude = marker.position.latitude
            val longitude = marker.position.longitude
            val uid = auth.currentUser?.uid

            showAddHousePopup {text ->
                databaseReference = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Houses").child(uid!!)
                val databaseReferenceUser = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("Users")
                    .child(uid)

                databaseReferenceUser.get().addOnSuccessListener {
                    val fullName = it.child("fullName").value?.toString()!!
                    val house = House(latitude,longitude,uid!!, fullName, text)
                    databaseReference.setValue(house)
                }
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), permissionCode
            )
            return
        }

        val getLocation = fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                location ->
            if(location != null ) {
                currentLocation = location
                Toast.makeText(applicationContext, currentLocation.latitude.toString() + "" +
                        currentLocation.longitude.toString(), Toast.LENGTH_LONG).show()

                val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)

            }
        }
    }

    override fun onMapReady(googleMap : GoogleMap) {
        this.gMap = googleMap

        //Selected Location
        gMap.setOnMapClickListener { latLng ->
            if (::marker.isInitialized) {
                marker.remove()
            }
            val markerOptions = MarkerOptions().position(latLng).title("Seçilen Konum")
            markerOptions.icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
            marker = gMap.addMarker(markerOptions)
        }

        //Current Location
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("Şu anki konum")
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,7f))
        googleMap.addMarker(markerOptions)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            permissionCode -> if(grantResults.isNotEmpty() && grantResults[0]==
                PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }
        }
    }

    private fun showAddHousePopup(callback: (String) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.add_house_popup, null)
        val editText = dialogView.findViewById<EditText>(R.id.etBody)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val dialog = dialogBuilder.create()

        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
            val text = editText.text.toString()
            callback(text)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }



}