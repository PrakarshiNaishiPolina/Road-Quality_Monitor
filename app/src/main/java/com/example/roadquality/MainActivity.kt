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

    private var lastTime = 0L

    private val window = mutableListOf<Float>()

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
    }

    private fun getLocation() {

        if (ActivityCompat.checkSelfPermission(
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
            Priority.PRIORITY_HIGH_ACCURACY, 2000
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                currentLocation = result.lastLocation
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

        val verticalShock = abs(z - SensorManager.GRAVITY_EARTH)

        val currentTime = System.currentTimeMillis()

        val lat = currentLocation!!.latitude
        val lon = currentLocation!!.longitude

        val speed = currentLocation!!.speed
        val speedKmph = speed * 3.6f

        if (speedKmph < 2) {

            status.text =
                "Vehicle Almost Stationary\nSpeed: %.2f km/h"
                    .format(speedKmph)

            return
        }

        window.add(verticalShock)
        if (window.size > 10) {
            window.removeAt(0)
        }

        if (window.size >= 5 && currentTime - lastTime > 2000) {

            val max = window.maxOrNull() ?: 0f
            val min = window.minOrNull() ?: 0f
            val variation = max - min

            val potholeThreshold: Float
            val bumpThreshold: Float
            val roughThreshold: Float

            if (speedKmph < 15) {

                // Slow vehicles / buses / EVs
                potholeThreshold = 6f
                bumpThreshold = 4f
                roughThreshold = 2f

            } else {

                // Normal speed vehicles
                potholeThreshold = 12f
                bumpThreshold = 6f
                roughThreshold = 3f
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
            val level = when {
                variation > 12 -> "HIGH"
                variation > 6 -> "MEDIUM"
                variation > 3 -> "LOW"
                else -> "NONE"
            }

            if (type != "NONE") {

                lastTime = currentTime

                status.text =
                    "🚧 $type DETECTED\n" +
                            "Level: $level\n" +
                            "Variation: %.2f\n".format(variation) +
                            "Max Shock: %.2f\n".format(max) +
                            "Speed: %.2f km/h\n".format(speedKmph) +
                            "Lat: $lat\nLon: $lon"

                safeSave(type, level, variation.toDouble(), lat, lon)
            }
        }
    }

    private fun safeSave(
        type: String,
        level: String,
        intensity: Double,
        lat: Double,
        lon: Double
    ) {
        try {
            val dir = getExternalFilesDir(null) ?: return
            val file = File(dir, "road_anomalies.csv")

            if (!file.exists()) {
                file.writeText("Type,Level,Intensity,Latitude,Longitude,Time\n")
            }

            val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date())

            file.appendText("$type,$level,$intensity,$lat,$lon,$time\n")

        } catch (e: Exception) {
            status.text = "File error"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}