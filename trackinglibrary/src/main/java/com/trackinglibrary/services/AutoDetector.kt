package com.trackinglibrary.services

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.trackinglibrary.utils.LocationUtils
import com.trackinglibrary.utils.LogUtils
import java.util.concurrent.TimeUnit

class AutoDetector(private val context: Context, looper: Looper, private val listener: (Int) -> Unit) : Handler(looper),
    SensorEventListener {

    @Volatile
    private var isRegistered = false

    private var senSensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var senAccelerometer: Sensor = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private lateinit var fusedClient: FusedLocationProviderClient
    private var location: Location? = null
    private val locationsStack = AutoDataStack(30L)

    @SuppressLint("MissingPermission")
    fun requestUpdate() {
        if (!isRegistered) {
            this.fusedClient = FusedLocationProviderClient(context)
            val request = LocationUtils.createFusedRequest(LocationRequest.PRIORITY_HIGH_ACCURACY, 10, 0, 0f)
            this.fusedClient.requestLocationUpdates(request, locationCallback, looper)
            senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL, this)
            isRegistered = true
        }
    }

    fun cancelRequest() {
        if (isRegistered) {
            removeCallbacksAndMessages(null)
            fusedClient.removeLocationUpdates(locationCallback)
            isRegistered = false
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            if (locationResult != null) {
                if (isRegistered) {
                    onNewLocation(locationResult.lastLocation)
                }
            }
        }
    }

    private fun onNewLocation(location: Location) {
        this.location = location
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (isRegistered) {
            val loc = location
            if (event != null && loc != null) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                if (location != null) {
                    val result = onNewData(loc, x, y, z)
                    listener(result)
                    location = null
                }
            }
        }
    }

    private fun onNewData(location: Location, x: Float, y: Float, z: Float): Int {
        val dateStr = "${location.time}"
        val spdStr = "${location.speed}"
        val xStr = "$x"
        val yStr = "$y"
        val zStr = "$z"
        val maxAcc = arrayOf(x, y, z).max()!!
        val maxAccStr = "$maxAcc"
        locationsStack.put(location, maxAcc)

        return if (location.speed > 24 || !locationsStack.hasEnoughData) {
            val case = (maxAcc / 4f) >= 1
            val result = if (case) 1 else 0
            LogUtils.writeAccidentLog("$dateStr, $spdStr, $xStr, $yStr, $zStr, $maxAccStr, $result")
            return result
        } else {
            val savedData = locationsStack.getData()
            val ssd = calculateSD(savedData.map { it.first.speed }.toFloatArray())
            val case = maxAcc / 4f + ssd / 2.05 >= 2
            val result = if (case) 1 else 0
            LogUtils.writeAccidentLog("$dateStr, $spdStr, $xStr, $yStr, $zStr, $maxAccStr, $result")
            return result
        }
    }

    private fun calculateSD(numArray: FloatArray): Double {

        val sum = numArray.fold(0.0) { s, r ->
            s + r
        }

        val mean = sum / numArray.size

        val standardDeviation = numArray.fold(0.0) { sdSum, value ->
            sdSum + Math.pow(value - mean, 2.0)
        }

        return Math.sqrt(standardDeviation / numArray.size)
    }

    internal class AutoDataStack(private val maxTimeSeconds: Long) {
        private val values = Array<Pair<Location, Float>?>(maxTimeSeconds.toInt()) { null }
        var hasEnoughData = false
        fun put(location: Location, acc: Float) {
            val data = Pair(location, acc)
            // clear old items
            values.forEachIndexed { index, it ->
                if (it != null) {
                    val timeCase = it.first.time + TimeUnit.MILLISECONDS.toSeconds(maxTimeSeconds) >= data.first.time
                    if (timeCase) {
                        values[index] = null
                        hasEnoughData = true
                    }
                }
            }

            for (i in values.size - 2 downTo 0) {
                values[i + 1] = values[i]
            }
            values[0] = data

            if (!hasEnoughData) {
                if (values.last() != null) {
                    hasEnoughData = true
                }
            }
        }

        fun getData(): Array<Pair<Location, Float>> {
            val items = mutableListOf<Pair<Location, Float>>()
            values.forEach {
                if (it == null) {
                    return@forEach
                }
                items.add(it)
            }
            return items.toTypedArray()
        }
    }
}