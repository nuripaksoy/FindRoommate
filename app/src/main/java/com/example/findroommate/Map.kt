package com.example.findroommate

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.widget.SearchView.OnQueryTextListener
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.findroommate.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.io.IOException
import java.util.*

class Map : FragmentActivity() , OnMapReadyCallback, OnMarkerClickListener, OnInfoWindowClickListener {

    private lateinit var binding : ActivityMapBinding
    private lateinit var gMap : GoogleMap
    private lateinit var map : FrameLayout
    private lateinit var currentLocation : Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101
    private lateinit var searchView: androidx.appcompat.widget.SearchView
    private lateinit var marker : Marker
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        map = findViewById(R.id.map)
        searchView = findViewById(R.id.search)
        searchView.clearFocus()
        auth = Firebase.auth

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                var loc = searchView.query.toString()
                if(loc == null){
                    Toast.makeText(applicationContext, "Location Not Found", Toast.LENGTH_SHORT).show()
                }else{
                    val geocoder = Geocoder(applicationContext, Locale.getDefault())
                    try {
                        val addressList: List<Address> = geocoder.getFromLocationName(loc, 1) as List<Address>
                        if (addressList.isNotEmpty()) {
                            var latLng =
                                LatLng(addressList.get(0).latitude, addressList.get(0).longitude)
                            if (marker != null) {
                                marker.remove()
                            }
                            val markerOptions = MarkerOptions().position(latLng).title(loc)
                            markerOptions.icon(
                                BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_AZURE
                                )
                            )
                            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 5f)
                            gMap.animateCamera(cameraUpdate)
                            marker = gMap.addMarker(markerOptions)
                        }
                    }catch (e : IOException){
                        e.printStackTrace()
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Arama metni değiştiğinde yapılacak işlemler
                return false
            }
        })
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
                currentLocation.longitude.toString(),Toast.LENGTH_LONG).show()

                val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)

            }
        }
    }


    override fun onMapReady(googleMap : GoogleMap) {
        this.gMap = googleMap

        //Current Location
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("Current Location")
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,7f))
        googleMap.addMarker(markerOptions)

        marker = googleMap.addMarker(markerOptions) // marker'ı başlat

        // Locations in Database
        val housesRef = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Houses")
        housesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val house = snapshot.getValue(House::class.java)
                    if (house != null) {
                        val houseLatLng = LatLng(house.latitude!!, house.longitude!!)

                        getProfilePhoto(house.ownerUid.toString()) { profilePhotoUrl ->
                            if (profilePhotoUrl != null) {
                                val markerOptions = MarkerOptions().position(houseLatLng).title(house.ownerFullName.toString()).snippet(house.body)
                                Glide.with(this@Map)
                                    .asBitmap()
                                    .load(profilePhotoUrl)
                                    .circleCrop()
                                    .into(object : CustomTarget<Bitmap>() {
                                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                            val resizedBitmap = Bitmap.createScaledBitmap(resource, 200, 200, false)
                                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
                                            val marker = googleMap.addMarker(markerOptions)
                                            marker.tag = house.ownerUid
                                        }
                                        override fun onLoadCleared(placeholder: Drawable?) {
                                        }
                                    })
                            } else {
                                val markerOptions = MarkerOptions().position(houseLatLng).title(house.ownerFullName).snippet(house.body)
                                val marker = googleMap.addMarker(markerOptions)
                                marker.tag = house.ownerUid
                            }
                        }

                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                val view = LayoutInflater.from(this@Map).inflate(R.layout.custom_info_window, null)

                val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
                val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)
                val sendOfferButton = view.findViewById<Button>(R.id.sendOfferButton)

                titleTextView.text = marker.title
                descriptionTextView.text = marker.snippet
                val ownerUid = marker.tag as String

                sendOfferButton.setOnClickListener {
                    val offererUid = auth.currentUser?.uid
                    val offer = Offer(ownerUid,offererUid,false)
                    databaseReference = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Offers").child(ownerUid!!)
                    databaseReference.setValue(offer)
                    databaseReference = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Offers").child(offererUid!!)
                    databaseReference.setValue(offer)
                        .addOnSuccessListener(){
                            Toast.makeText(this@Map, "Teklif Başarıyla gönderildi.", Toast.LENGTH_LONG).show()
                        }
                }
                return view
            }
        })

        googleMap.setOnInfoWindowClickListener(this)
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

    private fun getProfilePhoto(uid: String, callback: (String?) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("Users")
            .child(uid)

        databaseReference.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            val profilePhotoUrl = user?.profilePhoto
            callback(profilePhotoUrl)
        }.addOnFailureListener {
            callback(null)
        }
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onInfoWindowClick(p0: Marker?) {
        val offererUid = auth.currentUser?.uid
        val ownerUid = marker?.tag as String?
        val offer = Offer(ownerUid,offererUid,false)
        databaseReference = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Offers")
        databaseReference.setValue(offer)
        databaseReference = FirebaseDatabase.getInstance("https://findroommate-9c49a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Offers").child(offererUid!!)
        databaseReference.setValue(offer)
            .addOnSuccessListener(){
                Toast.makeText(this@Map, "Teklif Başarıyla gönderildi.", Toast.LENGTH_LONG).show()
            }
    }


}