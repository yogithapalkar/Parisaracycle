package com.yogitha.parisaracycle

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class CO2Activity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var totalDistance = 0.0
    private var co2Today = 0.0
    private var co2Month = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_co2)

        db = FirebaseFirestore.getInstance()

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadCO2Data()

        findViewById<Button>(R.id.btnAddRide).setOnClickListener {
            logNewRide()
        }
    }

    private fun loadCO2Data() {
        db.collection("co2stats")
            .document("user_stats")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    co2Today = doc.getDouble("co2Today") ?: 0.0
                    co2Month = doc.getDouble("co2Month") ?: 0.0
                    totalDistance = doc.getDouble("totalDistance") ?: 0.0
                    updateDisplay()
                }
            }
    }

    private fun logNewRide() {
        val rideDistance = 1.0
        val co2Saved = rideDistance * 120

        totalDistance += rideDistance
        co2Today += co2Saved
        co2Month += co2Saved

        val stats = hashMapOf(
            "co2Today" to co2Today,
            "co2Month" to co2Month,
            "totalDistance" to totalDistance,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("co2stats")
            .document("user_stats")
            .set(stats)
            .addOnSuccessListener {
                Toast.makeText(this,
                    "Ride logged! +${co2Saved}g CO2 saved",
                    Toast.LENGTH_SHORT).show()
                updateDisplay()
            }
            .addOnFailureListener {
                Toast.makeText(this,
                    "Failed to save. Check internet.",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateDisplay() {
        findViewById<TextView>(R.id.tvTodayCO2).text = "${co2Today}g"
        findViewById<TextView>(R.id.tvMonthlyCO2).text = "${co2Month}g"
        findViewById<TextView>(R.id.tvDistance).text = "${totalDistance} km"
        findViewById<ProgressBar>(R.id.progressCO2).progress = co2Month.toInt()
    }
}