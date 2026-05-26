package com.example.roadquality

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usersPrefs =
            getSharedPreferences("users", MODE_PRIVATE)

        val sessionPrefs =
            getSharedPreferences("session", MODE_PRIVATE)

        // AUTO LOGIN

        if (sessionPrefs.getBoolean("loggedIn", false)) {

            val role =
                sessionPrefs.getString("role", "User")

            if (role == "Admin") {

                startActivity(
                    Intent(this, AdminActivity::class.java)
                )

            } else {

                startActivity(
                    Intent(this, HomeActivity::class.java)
                )
            }

            finish()
        }

        setContentView(R.layout.activity_login)

        val username =
            findViewById<EditText>(R.id.username)

        val password =
            findViewById<EditText>(R.id.password)

        val roleSpinner =
            findViewById<Spinner>(R.id.roleSpinner)

        val actionBtn =
            findViewById<Button>(R.id.actionBtn)

        val toggleText =
            findViewById<TextView>(R.id.toggleText)

        val forgotText =
            findViewById<TextView>(R.id.forgotText)

        val roles = arrayOf(
            "User",
            "Admin"
        )

        roleSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                roles
            )

        var isLogin = true

        actionBtn.setOnClickListener {

            val empId =
                username.text.toString().trim()

            val pass =
                password.text.toString().trim()

            val role =
                roleSpinner.selectedItem.toString()

            if (
                empId.isEmpty() ||
                pass.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Enter all fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val savedPass =
                usersPrefs.getString(
                    "${empId}_password",
                    null
                )

            val savedRole =
                usersPrefs.getString(
                    "${empId}_role",
                    null
                )

            if (isLogin) {

                // LOGIN

                if (
                    pass == savedPass &&
                    role == savedRole
                ) {

                    sessionPrefs.edit()
                        .putBoolean("loggedIn", true)
                        .putString("employeeId", empId)
                        .putString("role", role)
                        .apply()

                    Toast.makeText(
                        this,
                        "Login Successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (role == "Admin") {

                        startActivity(
                            Intent(
                                this,
                                AdminActivity::class.java
                            )
                        )

                    } else {

                        startActivity(
                            Intent(
                                this,
                                HomeActivity::class.java
                            )
                        )
                    }

                    finish()

                } else {

                    Toast.makeText(
                        this,
                        "Invalid Credentials",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {

                // SIGNUP

                if (savedPass != null) {

                    Toast.makeText(
                        this,
                        "Employee ID already exists",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                usersPrefs.edit()
                    .putString(
                        "${empId}_password",
                        pass
                    )
                    .putString(
                        "${empId}_role",
                        role
                    )
                    .apply()

                Toast.makeText(
                    this,
                    "Account Created Successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // AUTO SWITCH TO LOGIN

                isLogin = true

                actionBtn.text = "Login"

                toggleText.text =
                    "Don't have an account? Signup"
            }
        }

        // TOGGLE LOGIN/SIGNUP

        toggleText.setOnClickListener {

            isLogin = !isLogin

            actionBtn.text =
                if (isLogin)
                    "Login"
                else
                    "Signup"

            toggleText.text =
                if (isLogin)
                    "Don't have an account? Signup"
                else
                    "Already have an account? Login"
        }

        // FORGOT PASSWORD

        forgotText.setOnClickListener {

            val empId =
                username.text.toString().trim()

            if (empId.isEmpty()) {

                Toast.makeText(
                    this,
                    "Enter Employee ID",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val savedPass =
                usersPrefs.getString(
                    "${empId}_password",
                    null
                )

            if (savedPass != null) {

                Toast.makeText(
                    this,
                    "Password: $savedPass",
                    Toast.LENGTH_LONG
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Account not found",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}