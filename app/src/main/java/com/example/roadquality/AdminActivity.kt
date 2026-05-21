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

        loadData()

        refreshBtn.setOnClickListener {
            loadData()
        }

        logoutBtn.setOnClickListener {
            val prefs = getSharedPreferences("user", MODE_PRIVATE)
            prefs.edit().clear().apply()

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

            val content = file.readText()

            dataText.text = content

        } catch (e: Exception) {
            dataText.text = "Error loading data"
        }
    }
}