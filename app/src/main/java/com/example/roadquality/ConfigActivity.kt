package com.example.roadquality

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity

class ConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_config)

        val vehicleSpinner =
            findViewById<Spinner>(R.id.vehicleSpinner)

        val potholeSlider =
            findViewById<SeekBar>(R.id.potholeSlider)

        val bumpSlider =
            findViewById<SeekBar>(R.id.bumpSlider)

        val roughSlider =
            findViewById<SeekBar>(R.id.roughSlider)

        val speedSlider =
            findViewById<SeekBar>(R.id.speedSlider)

        val potholeValue =
            findViewById<TextView>(R.id.potholeValue)

        val bumpValue =
            findViewById<TextView>(R.id.bumpValue)

        val roughValue =
            findViewById<TextView>(R.id.roughValue)

        val speedValue =
            findViewById<TextView>(R.id.speedValue)

        val startBtn =
            findViewById<Button>(R.id.startMonitorBtn)

        // VEHICLES

        val vehicles = arrayOf(
            "Bike",
            "Car",
            "Bus",
            "Truck",
            "EV"
        )

        vehicleSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                vehicles
            )

        // SLIDER SETUP

        setupSlider(
            potholeSlider,
            potholeValue
        )

        setupSlider(
            bumpSlider,
            bumpValue
        )

        setupSlider(
            roughSlider,
            roughValue
        )

        setupSlider(
            speedSlider,
            speedValue,
            " m/s"
        )

        // VEHICLE PRESETS

        vehicleSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    when (vehicles[position]) {

                        // BIKE

                        "Bike" -> {

                            potholeSlider.progress = 12
                            bumpSlider.progress = 8
                            roughSlider.progress = 5
                            speedSlider.progress = 3
                        }

                        // CAR

                        "Car" -> {

                            potholeSlider.progress = 10
                            bumpSlider.progress = 7
                            roughSlider.progress = 4
                            speedSlider.progress = 3
                        }

                        // BUS

                        "Bus" -> {

                            potholeSlider.progress = 8
                            bumpSlider.progress = 5
                            roughSlider.progress = 3
                            speedSlider.progress = 2
                        }

                        // TRUCK

                        "Truck" -> {

                            potholeSlider.progress = 7
                            bumpSlider.progress = 5
                            roughSlider.progress = 3
                            speedSlider.progress = 2
                        }

                        // EV

                        "EV" -> {

                            potholeSlider.progress = 9
                            bumpSlider.progress = 6
                            roughSlider.progress = 4
                            speedSlider.progress = 2
                        }
                    }
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {}
            }

        // START MONITORING

        startBtn.setOnClickListener {

            val pothole =
                potholeSlider.progress.toFloat()

            val bump =
                bumpSlider.progress.toFloat()

            val rough =
                roughSlider.progress.toFloat()

            val minSpeed =
                speedSlider.progress.toFloat()

            val vehicle =
                vehicleSpinner.selectedItem.toString()

            // SAVE CONFIG

            val prefs =
                getSharedPreferences(
                    "config",
                    MODE_PRIVATE
                )

            prefs.edit()
                .putString("vehicle", vehicle)
                .putFloat("pothole", pothole)
                .putFloat("bump", bump)
                .putFloat("rough", rough)
                .putFloat("speed", minSpeed)
                .apply()

            // OPEN MAIN ACTIVITY

            val intent =
                Intent(
                    this,
                    MainActivity::class.java
                )

            intent.putExtra(
                "vehicle",
                vehicle
            )

            intent.putExtra(
                "pothole",
                pothole
            )

            intent.putExtra(
                "bump",
                bump
            )

            intent.putExtra(
                "rough",
                rough
            )

            intent.putExtra(
                "speed",
                minSpeed
            )

            startActivity(intent)
        }
    }

    // SLIDER FUNCTION

    private fun setupSlider(
        slider: SeekBar,
        valueText: TextView,
        suffix: String = ""
    ) {

        valueText.text =
            slider.progress.toString() + suffix

        slider.setOnSeekBarChangeListener(

            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    valueText.text =
                        progress.toString() + suffix
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {}

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {}
            }
        )
    }
}