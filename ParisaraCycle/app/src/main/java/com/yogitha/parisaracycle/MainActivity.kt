package com.yogitha.parisaracycle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: FirebaseFirestore

    private var co2Today = 0.0
    private var co2Month = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.btnReport).setOnClickListener {
            reportHazard()
        }

        findViewById<Button>(R.id.btnPitStop).setOnClickListener {
            startActivity(Intent(this, PitStopActivity::class.java))
        }
        findViewById<Button>(R.id.btnCO2).setOnClickListener {
            startActivity(Intent(
                this, CO2Activity::class.java))
            findViewById<Button>(R.id.btnBuddy).setOnClickListener {
                startActivity(Intent(this, BuddyActivity::class.java))
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isBuildingsEnabled = true

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {

                val userLocation = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
            }
        }

        loadHazardPins()
    }

    private fun loadHazardPins() {
        db.collection("hazards")
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                mMap.clear()
                snapshots?.forEach { doc ->
                    val lat = doc.getDouble("lat") ?: return@forEach
                    val lng = doc.getDouble("lng") ?: return@forEach
                    val type = doc.getString("type") ?: "Hazard"
                    val position = LatLng(lat, lng)
                    val color = when (type) {
                        "Pothole" -> BitmapDescriptorFactory.HUE_ORANGE
                        "Dangerous Intersection" -> BitmapDescriptorFactory.HUE_RED
                        "Blocked Path" -> BitmapDescriptorFactory.HUE_YELLOW
                        else -> BitmapDescriptorFactory.HUE_RED
                    }
                    mMap.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(type)
                            .icon(BitmapDescriptorFactory.defaultMarker(color))
                    )
                }
            }
    }

    private fun reportHazard() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please enable location first!", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                Toast.makeText(this, "Could not get location. Try again.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val hazard = hashMapOf(
                "lat" to location.latitude,
                "lng" to location.longitude,
                "type" to "Pothole",
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("hazards")
                .add(hazard)
                .addOnSuccessListener {
                    Toast.makeText(this, "Hazard reported! Pin added to map.", Toast.LENGTH_SHORT).show()
                    co2Today += 120.0
                    co2Month += 120.0
                    updateCO2Display()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to report. Check internet.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateCO2Display() {
        findViewById<TextView>(R.id.tvCO2Today).text = "CO2 Saved Today: ${co2Today}g"
        findViewById<TextView>(R.id.tvCO2Month).text = "Monthly Total: ${co2Month}g"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(mMap)
        } else {
            Toast.makeText(this, "Location permission needed!", Toast.LENGTH_LONG).show()
        }
    }
}