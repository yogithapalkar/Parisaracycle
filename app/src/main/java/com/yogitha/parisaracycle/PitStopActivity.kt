package com.yogitha.parisaracycle

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.gms.location.LocationServices

data class PitStop(
    val name: String,
    val distance: String,
    val lat: Double,
    val lng: Double
)

class PitStopActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pitstop)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadPitStops()
    }

    private fun loadPitStops() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission needed!", Toast.LENGTH_SHORT).show()
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                showPitStops(location)
            } else {
                findViewById<TextView>(R.id.tvLoading).text = "Could not get location. Try again."
            }
        }
    }

    private fun showPitStops(userLocation: Location) {
        val pitStops = listOf(
            PitStop("City Cycle Works", "0.3 km away", userLocation.latitude + 0.002, userLocation.longitude + 0.001),
            PitStop("Kumar Cycle Shop", "0.7 km away", userLocation.latitude - 0.003, userLocation.longitude + 0.002),
            PitStop("Sri Cycle Repair", "1.1 km away", userLocation.latitude + 0.005, userLocation.longitude - 0.003),
            PitStop("Drinking Water Point", "0.5 km away", userLocation.latitude + 0.003, userLocation.longitude + 0.004),
            PitStop("Namma Cycle Store", "1.5 km away", userLocation.latitude - 0.006, userLocation.longitude + 0.005),
            PitStop("Green Cycle Hub", "2.0 km away", userLocation.latitude + 0.008, userLocation.longitude - 0.006)
        )

        findViewById<TextView>(R.id.tvLoading).text = "Found ${pitStops.size} nearby stops ✅"

        val recycler = findViewById<RecyclerView>(R.id.recyclerPitStops)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = PitStopAdapter(pitStops)
    }

    inner class PitStopAdapter(private val items: List<PitStop>) :
        RecyclerView.Adapter<PitStopAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvName)
            val tvDistance: TextView = view.findViewById(R.id.tvDistance)
            val btnGo: Button = view.findViewById(R.id.btnGo)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pitstop, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvName.text = item.name
            holder.tvDistance.text = item.distance
            holder.btnGo.setOnClickListener {
                val uri = Uri.parse("google.navigation:q=${item.lat},${item.lng}")
                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${item.lat},${item.lng}")
                    startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                }
            }
        }

        override fun getItemCount() = items.size
    }
}