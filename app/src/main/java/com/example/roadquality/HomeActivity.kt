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

        val startBtn =
            findViewById<Button>(R.id.startBtn)

        val logoutBtn =
            findViewById<Button>(R.id.logoutBtn)

        // START BUTTON

        startBtn.setOnClickListener {

            try {

                startActivity(
                    Intent(
                        this,
                        ConfigActivity::class.java
                    )
                )

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Unable to open configuration",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // LOGOUT BUTTON

        logoutBtn.setOnClickListener {

            try {

                val sessionPrefs =
                    getSharedPreferences(
                        "session",
                        MODE_PRIVATE
                    )

                sessionPrefs.edit()
                    .putBoolean("loggedIn", false)
                    .apply()

                startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    )
                )

                finish()

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Logout Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}