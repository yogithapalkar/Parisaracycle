package com.yogitha.parisaracycle

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BuddyActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val database = FirebaseDatabase.getInstance()
    private val buddiesRef = database.getReference("buddies")
    private var isSharing = false
    private val myUserId = "user_${System.currentTimeMillis()}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buddy)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Back button
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            stopSharing()
            finish()
        }

        // Start sharing button
        findViewById<Button>(R.id.btnShare).setOnClickListener {
            startSharing()
        }

        // Stop sharing button
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopSharing()
        }

        // Initialize map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.buddyMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Listen for other buddies
        listenForBuddies()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                }
            }
        }
    }

    private fun startSharing() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission needed!", Toast.LENGTH_SHORT).show()
            return
        }

        isSharing = true

        // Update UI
        findViewById<TextView>(R.id.tvStatus).text = "🟢 Sharing location..."
        findViewById<TextView>(R.id.tvStatus).setTextColor(
            resources.getColor(android.R.color.holo_green_dark, null))
        findViewById<Button>(R.id.btnShare).visibility = View.GONE
        findViewById<Button>(R.id.btnStop).visibility = View.VISIBLE

        // Start location updates every 10 seconds
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                // Save to Firebase Realtime Database
                val locationData = mapOf(
                    "lat" to location.latitude,
                    "lng" to location.longitude,
                    "timestamp" to System.currentTimeMillis()
                )
                buddiesRef.child(myUserId).setValue(locationData)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        Toast.makeText(this, "Now sharing! Buddies can see you.", Toast.LENGTH_SHORT).show()
    }

    private fun stopSharing() {
        if (!isSharing) return
        isSharing = false

        // Remove from Firebase
        buddiesRef.child(myUserId).removeValue()

        // Stop location updates
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }

        // Update UI
        findViewById<TextView>(R.id.tvStatus).text = "🔴 Not sharing location"
        findViewById<TextView>(R.id.tvStatus).setTextColor(
            resources.getColor(android.R.color.holo_red_dark, null))
        findViewById<Button>(R.id.btnShare).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnStop).visibility = View.GONE

        Toast.makeText(this, "Stopped sharing location.", Toast.LENGTH_SHORT).show()
    }

    private fun listenForBuddies() {
        buddiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!::mMap.isInitialized) return

                // Clear old buddy markers
                mMap.clear()

                var buddyCount = 0

                for (child in snapshot.children) {
                    // Skip own location
                    if (child.key == myUserId) continue

                    val lat = child.child("lat").getValue(Double::class.java) ?: continue
                    val lng = child.child("lng").getValue(Double::class.java) ?: continue

                    // Add blue marker for each buddy
                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(lat, lng))
                            .title("Buddy Cyclist")
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_BLUE))
                    )
                    buddyCount++
                }

                // Update buddy count
                val countText = if (buddyCount == 0) "No buddies nearby"
                else "$buddyCount buddy cyclist(s) nearby! 🚲"
                findViewById<TextView>(R.id.tvBuddyCount).text = countText
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BuddyActivity,
                    "Error loading buddies.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSharing()
    }
}