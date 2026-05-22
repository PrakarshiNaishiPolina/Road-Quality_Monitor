package com.example.roadquality

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import java.io.File

class AdminActivity : ComponentActivity() {

    private lateinit var dataText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin)

        dataText = findViewById(R.id.dataText)

        val refreshBtn = findViewById<Button>(R.id.refreshBtn)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)
        val deleteBtn = findViewById<Button>(R.id.deleteBtn)

        loadData()

        refreshBtn.setOnClickListener {
            loadData()
        }
        deleteBtn.setOnClickListener {

            val file =
                File(getExternalFilesDir(null),
                    "road_anomalies.csv")

            if (file.exists()) {
                file.delete()
            }

            getSharedPreferences("config", MODE_PRIVATE)
                .edit()
                .clear()
                .apply()

            dataText.text = "All data deleted"
        }

        logoutBtn.setOnClickListener {
            val prefs = getSharedPreferences("user", MODE_PRIVATE)
            prefs.edit()
                .putBoolean("loggedIn", false)
                .apply()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadData() {

        try {
            val file = File(getExternalFilesDir(null), "road_anomalies.csv")

            if (!file.exists()) {
                dataText.text = "No data available"
                return
            }
            val prefs =
                getSharedPreferences("config", MODE_PRIVATE)

            val vehicle =
                prefs.getString("vehicle", "N/A")

            val pothole =
                prefs.getFloat("pothole", 0f)

            val bump =
                prefs.getFloat("bump", 0f)

            val rough =
                prefs.getFloat("rough", 0f)

            val speed =
                prefs.getFloat("speed", 0f)

            val content = file.readText()

            val configText =

                "===== CURRENT CONFIGURATION =====\n\n" +

                        "Vehicle Type: $vehicle\n\n" +

                        "Pothole Threshold: $pothole\n" +

                        "Bump Threshold: $bump\n" +

                        "Rough Threshold: $rough\n" +

                        "Minimum Speed: $speed m/s\n\n" +

                        "===== DETECTION LOGS =====\n\n"

            dataText.text = configText + content

        } catch (e: Exception) {
            dataText.text = "Error loading data"
        }
    }
}