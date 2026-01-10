package com.humayapp.scout.feature.form.impl.data.repository

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

typealias Coordinates = Pair<Double, Double> // longitude, latitude

fun emptyCoordinates(): Coordinates = Coordinates(0.0, 0.0)
fun defaultGeom(): String = "POINT(0 0)"
fun Coordinates.toDisplay(): String = "${this.first}, ${this.second}"

// should only be called on strings that is transformed to `toDisplay`
fun String.toGeometry(): String {
    val parts = this.split(",").map { it.trim() }
    val x = parts[0].toDouble()
    val y = parts[1].toDouble()
    return "POINT($x $y)"
}

fun Coordinates.asGeometry(): String = "POINT(${this.first} ${this.second})"
fun Coordinates.isNotZero(): Boolean = this.first != 0.0 && this.second != 0.0

interface CoordinatesService {
    suspend fun getCoordinates(
        retries: Int = 3,
        timeoutMillis: Long = 5000L,
        initialDelayMillis: Long = 500L,
        backoffFactor: Double = 2.0
    ): Coordinates
}

interface CoordinatesRepository {
    suspend fun getCoordinates(): Coordinates
}

class CoordinatesServiceGms @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
) : CoordinatesService {


    @RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun getCoordinates(
        retries: Int,
        timeoutMillis: Long,
        initialDelayMillis: Long,
        backoffFactor: Double
    ): Coordinates {
        var delayMillis = initialDelayMillis
        repeat(retries + 1) { attempt ->
            val location = withTimeoutOrNull(timeoutMillis) {
                suspendCancellableCoroutine<Location?> { cont ->
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { loc -> if (!cont.isCompleted) cont.resume(loc) }
                        .addOnFailureListener { ex -> if (!cont.isCompleted) cont.resume(null) }
                }
            }

            if (location != null) return Coordinates(location.longitude, location.latitude)

            if (attempt < retries) {
                delay(delayMillis)
                delayMillis = (delayMillis * backoffFactor).toLong()
            }
        }
        throw RuntimeException("Failed to get current location after ${retries + 1} attempts")
    }
}

class CoordinatesServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CoordinatesService {

    @RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun getCoordinates(
        retries: Int,
        timeoutMillis: Long,
        initialDelayMillis: Long,
        backoffFactor: Double
    ): Coordinates = suspendCancellableCoroutine { cont ->

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            cont.resume(Coordinates(0.0, 0.0)) { _, _, _ -> }
            return@suspendCancellableCoroutine
        }

        val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastKnown != null) {
            cont.resume(Coordinates(lastKnown.longitude, lastKnown.latitude)) { _, _, _ -> }
            return@suspendCancellableCoroutine
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                cont.resume(Coordinates(location.longitude, location.latitude)) { _, _, _ -> }
                locationManager.removeUpdates(this)
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 1000L, 1f, listener,
            Looper.getMainLooper()
        )

        cont.invokeOnCancellation {
            locationManager.removeUpdates(listener)
        }
    }
}

class CoordinatesRepositoryImpl @Inject constructor(
    private val coordinatesService: CoordinatesService
) : CoordinatesRepository {

    override suspend fun getCoordinates(): Coordinates {
        val result = coordinatesService.getCoordinates()
        return result
    }
}
