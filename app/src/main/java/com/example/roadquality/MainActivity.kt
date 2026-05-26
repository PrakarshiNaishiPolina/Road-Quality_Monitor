package com.example.roadquality

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity :
    ComponentActivity(),
    SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private lateinit var status: TextView

    private lateinit var fusedLocationClient:
            FusedLocationProviderClient

    private var currentLocation: Location? = null

    private lateinit var locationCallback:
            LocationCallback

    // TIME CONTROL

    private var lastTime = 0L

    // WINDOW FOR SMOOTHING

    private val window =
        mutableListOf<Float>()

    // THRESHOLDS

    private var potholeThreshold = 10f
    private var bumpThreshold = 7f
    private var roughThreshold = 4f

    private var minSpeed = 3f

    private var vehicleType = "Car"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        status =
            findViewById(R.id.statusText)

        // SENSOR

        sensorManager =
            getSystemService(
                Context.SENSOR_SERVICE
            ) as SensorManager

        accelerometer =
            sensorManager?.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            )

        if (accelerometer == null) {

            status.text =
                "Accelerometer Not Available"
        }

        // LOCATION

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        getLocation()

        // CONFIG VALUES

        vehicleType =
            intent.getStringExtra("vehicle")
                ?: "Car"

        potholeThreshold =
            intent.getFloatExtra(
                "pothole",
                10f
            )

        bumpThreshold =
            intent.getFloatExtra(
                "bump",
                7f
            )

        roughThreshold =
            intent.getFloatExtra(
                "rough",
                4f
            )

        minSpeed =
            intent.getFloatExtra(
                "speed",
                3f
            )
    }

    // LOCATION FUNCTION

    private fun getLocation() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                100
            )

            return
        }

        val locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000
            ).build()

        // LAST LOCATION

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    currentLocation = location
                }
            }

        // LIVE LOCATION

        locationCallback =
            object : LocationCallback() {

                override fun onLocationResult(
                    result: LocationResult
                ) {

                    currentLocation =
                        result.lastLocation
                }
            }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    // SENSOR START

    override fun onResume() {
        super.onResume()

        accelerometer?.let {

            sensorManager?.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    // SENSOR STOP

    override fun onPause() {
        super.onPause()

        sensorManager?.unregisterListener(this)

        fusedLocationClient.removeLocationUpdates(
            locationCallback
        )
    }

    // MAIN DETECTION LOGIC

    override fun onSensorChanged(
        event: SensorEvent?
    ) {

        if (
            event == null ||
            event.values.size < 3
        ) return

        if (currentLocation == null) {

            status.text =
                "Getting GPS Location..."

            return
        }

        // SENSOR VALUES

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // TOTAL FORCE

        val magnitude =
            sqrt(
                (x * x + y * y + z * z).toDouble()).toFloat()

        // REMOVE GRAVITY EFFECT

        val delta =
            abs(magnitude - SensorManager.GRAVITY_EARTH)

        val verticalShock = delta

        // LOCATION + SPEED

        val lat =
            currentLocation!!.latitude

        val lon =
            currentLocation!!.longitude

        val speed =
            currentLocation!!.speed

        // IGNORE LOW SPEED

        if (speed < minSpeed) {

            status.text =

                "Vehicle Moving Slowly\n\n" +

                        "Current Speed: %.2f m/s"
                            .format(speed)

            return
        }


        window.add(verticalShock)


        if (window.size > 20) {

            window.removeAt(0)
        }

        val currentTime =
            System.currentTimeMillis()


        if (
            window.size >= 10 &&
            currentTime - lastTime > 3000
        ) {

            val max =
                window.maxOrNull() ?: 0f

            val min =
                window.minOrNull() ?: 0f

            val variation =
                max - min

            val average =
                window.average().toFloat()

            val shockDifference =
                max - average

            // NOISE FILTERING

            if (shockDifference < 3f) {
                return
            }

            if (average < 5f) {
                return
            }

            // DETECTION

            val type = when {

                variation > potholeThreshold &&
                        max > potholeThreshold ->

                    "POTHOLE"

                variation > bumpThreshold &&
                        max > bumpThreshold ->

                    "SPEED_BUMP"

                variation > roughThreshold ->

                    "ROUGH_ROAD"

                else -> "NONE"
            }

            // LEVEL

            val level = when {

                variation > potholeThreshold ->
                    "HIGH"

                variation > bumpThreshold ->
                    "MEDIUM"

                variation > roughThreshold ->
                    "LOW"

                else -> "NONE"
            }

            // SHOW RESULT

            if (type != "NONE") {

                lastTime = currentTime

                status.text =

                    "Detection Found\n\n" +

                            "Type: $type\n\n" +

                            "Vehicle: $vehicleType\n" +

                            "Level: $level\n\n" +

                            "Variation: %.2f\n"
                                .format(variation) +

                            "Shock: %.2f\n"
                                .format(max) +

                            "Speed: %.2f m/s\n\n"
                                .format(speed) +

                            "Latitude:\n$lat\n\n" +

                            "Longitude:\n$lon"

                // SAVE CSV

                safeSave(
                    type,
                    level,
                    variation.toDouble(),
                    speed,
                    lat,
                    lon
                )
            }
        }
    }

    // SAVE CSV

    private fun safeSave(
        type: String,
        level: String,
        intensity: Double,
        speed: Float,
        lat: Double,
        lon: Double
    ) {

        try {

            val dir =
                getExternalFilesDir(null)
                    ?: return

            val file =
                File(
                    dir,
                    "road_anomalies.csv"
                )

            // HEADER

            if (!file.exists()) {

                file.writeText(

                    "Vehicle,Type,Level," +
                            "Intensity,Speed," +
                            "PotholeThreshold," +
                            "BumpThreshold," +
                            "RoughThreshold," +
                            "Latitude,Longitude,Time\n"
                )
            }

            // TIME

            val time =
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
                ).format(Date())

            // SAVE ROW

            file.appendText(

                "$vehicleType," +

                        "$type," +

                        "$level," +

                        "$intensity," +

                        "$speed," +

                        "$potholeThreshold," +

                        "$bumpThreshold," +

                        "$roughThreshold," +

                        "$lat," +

                        "$lon," +

                        "$time\n"
            )

        } catch (e: Exception) {

            status.text =
                "Error Saving File"
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {}

    override fun onDestroy() {
        super.onDestroy()

        try {

            sensorManager?.unregisterListener(this)

            fusedLocationClient.removeLocationUpdates(
                locationCallback
            )

        } catch (e: Exception) {
        }
    }
}


