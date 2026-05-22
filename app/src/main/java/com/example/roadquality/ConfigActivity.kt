
package com.example.roadquality

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity

class ConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_config)

        val vehicleSpinner = findViewById<Spinner>(R.id.vehicleSpinner)

        val potholeInput = findViewById<EditText>(R.id.potholeInput)
        val bumpInput = findViewById<EditText>(R.id.bumpInput)
        val roughInput = findViewById<EditText>(R.id.roughInput)
        val speedInput = findViewById<EditText>(R.id.speedInput)

        val startBtn = findViewById<Button>(R.id.startMonitorBtn)

        val vehicles = arrayOf(
            "Bike",
            "Car",
            "Bus",
            "Truck",
            "EV",
            "Walking"
        )

        vehicleSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            vehicles
        )

        vehicleSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {

                    when (vehicles[position]) {

                        "Bike" -> {
                            potholeInput.setText("12")
                            bumpInput.setText("8")
                            roughInput.setText("5")
                            speedInput.setText("2")
                        }

                        "Car" -> {
                            potholeInput.setText("10")
                            bumpInput.setText("7")
                            roughInput.setText("4")
                            speedInput.setText("2")
                        }

                        "Bus" -> {
                            potholeInput.setText("8")
                            bumpInput.setText("5")
                            roughInput.setText("3")
                            speedInput.setText("1")
                        }

                        "Truck" -> {
                            potholeInput.setText("7")
                            bumpInput.setText("5")
                            roughInput.setText("3")
                            speedInput.setText("1")
                        }

                        "EV" -> {
                            potholeInput.setText("9")
                            bumpInput.setText("6")
                            roughInput.setText("4")
                            speedInput.setText("1")
                        }

                        "Walking" -> {
                            potholeInput.setText("15")
                            bumpInput.setText("10")
                            roughInput.setText("7")
                            speedInput.setText("0")
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        startBtn.setOnClickListener {

            val pothole =
                potholeInput.text.toString().toFloat()

            val bump =
                bumpInput.text.toString().toFloat()

            val rough =
                roughInput.text.toString().toFloat()

            val minSpeed =
                speedInput.text.toString().toFloat()

            val vehicle =
                vehicleSpinner.selectedItem.toString()

            val prefs =
                getSharedPreferences("config", MODE_PRIVATE)

            prefs.edit()
                .putString("vehicle", vehicle)
                .putFloat("pothole", pothole)
                .putFloat("bump", bump)
                .putFloat("rough", rough)
                .putFloat("speed", minSpeed)
                .apply()

            val intent =
                Intent(this, MainActivity::class.java)

            intent.putExtra("vehicle", vehicle)
            intent.putExtra("pothole", pothole)
            intent.putExtra("bump", bump)
            intent.putExtra("rough", rough)
            intent.putExtra("speed", minSpeed)

            startActivity(intent)
        }
    }
}