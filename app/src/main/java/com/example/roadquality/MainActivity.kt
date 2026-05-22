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

class MainActivity : ComponentActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private lateinit var status: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private lateinit var locationCallback: LocationCallback

    private var lastTime = 0L

    private val window = mutableListOf<Float>()
    private var potholeThreshold = 6f
    private var bumpThreshold = 4f
    private var roughThreshold = 2f

    private var minSpeed = 3f

    private var vehicleType = "Car"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        status = findViewById(R.id.statusText)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            status.text = "Sensor not available"
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()
        vehicleType =
            intent.getStringExtra("vehicle") ?: "Car"

        potholeThreshold =
            intent.getFloatExtra("pothole", 6f)

        bumpThreshold =
            intent.getFloatExtra("bump", 4f)

        roughThreshold =
            intent.getFloatExtra("rough", 2f)

        minSpeed =
            intent.getFloatExtra("speed", 3f)
    }

    private fun getLocation() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000
        ).build()

        // Get last known location first
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->

            if (location != null) {

                currentLocation = location

                status.text =
                    "GPS Connected\nLat: ${location.latitude}\nLon: ${location.longitude}"
            }
        }

        // Continuous location updates
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(result: LocationResult) {

                currentLocation = result.lastLocation

                currentLocation?.let {

                    status.text =
                        "GPS Connected\nLat: ${it.latitude}\nLon: ${it.longitude}"
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }
    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            // 🔥 Faster sensor updates for better detection
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()

        sensorManager?.unregisterListener(this)

        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event == null || event.values.size < 3) return

        if (currentLocation == null) {
            status.text = "📡 Getting GPS..."
            return
        }

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = abs(magnitude - SensorManager.GRAVITY_EARTH)

//        val verticalShock = abs(z - SensorManager.GRAVITY_EARTH)
        val verticalShock = delta

        val currentTime = System.currentTimeMillis()

        val lat = currentLocation!!.latitude
        val lon = currentLocation!!.longitude

        val speed = currentLocation!!.speed


        if (speed <minSpeed) {

            status.text =
                "Vehicle Almost Stationary\nSpeed: %.2f m/s"
                    .format(speed)

            return
        }

        window.add(verticalShock)

        if (window.size > 20) {
            window.removeAt(0)
        }

        if (window.size >= 10 && currentTime - lastTime > 3000){

            val max = window.maxOrNull() ?: 0f
            val min = window.minOrNull() ?: 0f
            val variation = max - min

            val average = window.average().toFloat()
            val shockDifference = max - average

            if (shockDifference < 3f) {
                return
            }

            if (average < 5f) {
                return
            }


            val type = when {

                variation > potholeThreshold && max > potholeThreshold ->
                    "POTHOLE"

                variation > bumpThreshold && max > bumpThreshold ->
                    "SPEED_BUMP"

                variation > roughThreshold ->
                    "ROUGH_ROAD"

                else -> "NONE"
            }
//            val level = when {
//                variation > 12 -> "HIGH"
//                variation > 6 -> "MEDIUM"
//                variation > 3 -> "LOW"
//                else -> "NONE"
//            }
            val level = when {
                variation > potholeThreshold -> "HIGH"
                variation > bumpThreshold -> "MEDIUM"
                variation > roughThreshold -> "LOW"
                else -> "NONE"
            }

            if (type != "NONE") {

                lastTime = currentTime

                status.text =

                    "🚧 $type DETECTED\n\n" +

                            "Vehicle: $vehicleType\n" +

                            "Level: $level\n\n" +

                            "Variation: %.2f\n".format(variation) +

                            "Max Shock: %.2f\n".format(max) +

                            "Speed: %.2f m/s\n\n".format(speed) +

                            "Pothole Threshold: $potholeThreshold\n" +

                            "Bump Threshold: $bumpThreshold\n" +

                            "Rough Threshold: $roughThreshold\n\n" +

                            "Lat: $lat\nLon: $lon"

                safeSave(
                    type,
                    level,
                    variation.toDouble(),
                    speed,
                    lat,
                    lon
                )            }
        }
    }

    private fun safeSave(
        type: String,
        level: String,
        intensity: Double,
        speed: Float,
        lat: Double,
        lon: Double
    ) {
        try {
            val dir = getExternalFilesDir(null) ?: return
            val file = File(dir, "road_anomalies.csv")

            if (!file.exists()) {
                file.writeText(
                    "Vehicle,Type,Level,Intensity,Speed,PotholeThreshold,BumpThreshold,RoughThreshold,Latitude,Longitude,Time\n"
                )            }

            val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date())

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
            status.text = "File error"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}