package com.example.roadquality

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        val startBtn = findViewById<Button>(R.id.startBtn)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        startBtn.setOnClickListener {

            try {

                startActivity(Intent(this, ConfigActivity::class.java))

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Error opening configuration",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
}