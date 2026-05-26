package com.example.roadquality

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import java.io.File

class AdminActivity : ComponentActivity() {

    private lateinit var tableLayout: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin)

        tableLayout =
            findViewById(R.id.tableLayout)

        val refreshBtn =
            findViewById<Button>(R.id.refreshBtn)

        val logoutBtn =
            findViewById<Button>(R.id.logoutBtn)

        val deleteBtn =
            findViewById<Button>(R.id.deleteBtn)

        val deleteUserBtn =
            findViewById<Button>(R.id.deleteUserBtn)

        val deleteUserInput =
            findViewById<EditText>(R.id.deleteUserInput)

        loadData()

        // REFRESH

        refreshBtn.setOnClickListener {

            loadData()
        }

        // DELETE ALL DETECTION DATA

        deleteBtn.setOnClickListener {

            val file = File(
                getExternalFilesDir(null),
                "road_anomalies.csv"
            )

            if (file.exists()) {
                file.delete()
            }

            getSharedPreferences(
                "config",
                MODE_PRIVATE
            ).edit()
                .clear()
                .apply()

            tableLayout.removeAllViews()

            Toast.makeText(
                this,
                "All Detection Data Deleted",
                Toast.LENGTH_SHORT
            ).show()
        }

        // DELETE USER ACCOUNT

        deleteUserBtn.setOnClickListener {

            val empId =
                deleteUserInput.text.toString().trim()

            if (empId.isEmpty()) {

                Toast.makeText(
                    this,
                    "Enter Employee ID",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val usersPrefs =
                getSharedPreferences(
                    "users",
                    MODE_PRIVATE
                )

            val savedPass =
                usersPrefs.getString(
                    "${empId}_password",
                    null
                )

            if (savedPass == null) {

                Toast.makeText(
                    this,
                    "User Not Found",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            usersPrefs.edit()
                .remove("${empId}_password")
                .remove("${empId}_role")
                .apply()

            Toast.makeText(
                this,
                "User Account Deleted",
                Toast.LENGTH_SHORT
            ).show()

            deleteUserInput.setText("")
        }

        // LOGOUT

        logoutBtn.setOnClickListener {

            val sessionPrefs =
                getSharedPreferences(
                    "session",
                    MODE_PRIVATE
                )

            sessionPrefs.edit()
                .putBoolean("loggedIn", false)
                .apply()

            Toast.makeText(
                this,
                "Logged Out",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finish()
        }
    }

    private fun loadData() {

        try {

            tableLayout.removeAllViews()

            val prefs =
                getSharedPreferences(
                    "config",
                    MODE_PRIVATE
                )

            val vehicle =
                prefs.getString(
                    "vehicle",
                    "N/A"
                )

            val pothole =
                prefs.getFloat(
                    "pothole",
                    0f
                )

            val bump =
                prefs.getFloat(
                    "bump",
                    0f
                )

            val rough =
                prefs.getFloat(
                    "rough",
                    0f
                )

            val speed =
                prefs.getFloat(
                    "speed",
                    0f
                )

            // CONFIGURATION SECTION

            addRow(
                arrayOf(
                    "Vehicle",
                    vehicle.toString()
                ),
                true
            )

            addRow(
                arrayOf(
                    "Pothole Value",
                    pothole.toString()
                ),
                false
            )

            addRow(
                arrayOf(
                    "Bump Value",
                    bump.toString()
                ),
                false
            )

            addRow(
                arrayOf(
                    "Road Vibration",
                    rough.toString()
                ),
                false
            )

            addRow(
                arrayOf(
                    "Minimum Speed",
                    "$speed m/s"
                ),
                false
            )

            // SPACE

            addRow(
                arrayOf("", ""),
                false
            )

            // LOG HEADER

            addRow(
                arrayOf(
                    "Detection Logs"
                ),
                true
            )

            val file = File(
                getExternalFilesDir(null),
                "road_anomalies.csv"
            )

            if (!file.exists()) {

                addRow(
                    arrayOf(
                        "No Data Available"
                    ),
                    false
                )

                return
            }

            val lines = file.readLines()

            for ((index, line) in lines.withIndex()) {

                val columns =
                    line.split(",")

                addRow(
                    columns.toTypedArray(),
                    index == 0
                )
            }

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Error Loading Data",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun addRow(
        data: Array<String>,
        isHeader: Boolean
    ) {

        val row = TableRow(this)

        for (text in data) {

            val textView = TextView(this)

            textView.text = text

            textView.setPadding(
                12,
                12,
                12,
                12
            )

            textView.gravity = Gravity.CENTER

            textView.maxWidth = 300

            textView.setTextColor(
                android.graphics.Color.parseColor(
                    "#111827"
                )
            )

            if (isHeader) {

                textView.setTypeface(
                    null,
                    Typeface.BOLD
                )
            }

            row.addView(textView)
        }

        tableLayout.addView(row)
    }
}