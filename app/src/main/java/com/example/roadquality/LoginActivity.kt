package com.example.roadquality

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("user", MODE_PRIVATE)

        // Auto login
        if (prefs.getBoolean("loggedIn", false)) {

            val role = prefs.getString("role", "Driver")

            if (role.equals("Admin", true)) {
                startActivity(Intent(this, AdminActivity::class.java))
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
            }
            finish()
        }

        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val roleSpinner = findViewById<Spinner>(R.id.roleSpinner)
        val btn = findViewById<Button>(R.id.actionBtn)
        val toggle = findViewById<TextView>(R.id.toggleText)

        val roles = arrayOf("Driver", "Admin")
        roleSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        var isLogin = true

        btn.setOnClickListener {

            val user = username.text.toString().trim()
            val pass = password.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()


            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isLogin) {

                val savedUser = prefs.getString("username", "")
                val savedPass = prefs.getString("password", "")
                val savedRole = prefs.getString("role", "")

                if (user == savedUser && pass == savedPass && role == savedRole) {

                    prefs.edit().putBoolean("loggedIn", true).apply()

                    if (role.equals("Admin", true)) {
                        startActivity(Intent(this, AdminActivity::class.java))
                    } else {
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                    finish()

                } else {
                    Toast.makeText(this, "Invalid Login", Toast.LENGTH_SHORT).show()
                }

            } else {

                // Signup
                prefs.edit()
                    .putString("username", user)
                    .putString("password", pass)
                    .putString("role", role)
                    .putBoolean("loggedIn", true)
                    .apply()

                Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()

                if (role.equals("Admin", true)) {
                    startActivity(Intent(this, AdminActivity::class.java))
                } else {
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                finish()
            }
        }

        toggle.setOnClickListener {
            isLogin = !isLogin

            btn.text = if (isLogin) "Login" else "Signup"

            toggle.text = if (isLogin)
                "Don't have an account? Signup"
            else
                "Already have an account? Login"
        }
    }
}