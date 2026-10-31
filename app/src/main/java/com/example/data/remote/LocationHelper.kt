package com.example.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    private const val PREFS_NAME = "mornix_location_prefs"
    private const val KEY_LAST_LAT = "last_latitude"
    private const val KEY_LAST_LON = "last_longitude"
    private const val KEY_LAST_CITY = "last_city_name"
    private const val KEY_LAST_TIMESTAMP = "last_timestamp"

    data class DeviceLocation(
        val cityName: String,
        val latitude: Double,
        val longitude: Double,
        val isGps: Boolean,
        val timestampMillis: Long = System.currentTimeMillis()
    )

    fun getLastSavedLocation(context: Context): DeviceLocation {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lat = sp.getFloat(KEY_LAST_LAT, 27.7172f).toDouble()
        val lon = sp.getFloat(KEY_LAST_LON, 85.3240f).toDouble()
        val city = sp.getString(KEY_LAST_CITY, "Kathmandu") ?: "Kathmandu"
        val time = sp.getLong(KEY_LAST_TIMESTAMP, System.currentTimeMillis())
        val hasSaved = sp.contains(KEY_LAST_LAT)
        return DeviceLocation(
            cityName = city,
            latitude = lat,
            longitude = lon,
            isGps = hasSaved,
            timestampMillis = time
        )
    }

    fun saveLastLocation(context: Context, location: DeviceLocation) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putFloat(KEY_LAST_LAT, location.latitude.toFloat())
            .putFloat(KEY_LAST_LON, location.longitude.toFloat())
            .putString(KEY_LAST_CITY, location.cityName)
            .putLong(KEY_LAST_TIMESTAMP, location.timestampMillis)
            .apply()
    }

    private suspend fun <T> Task<T>.awaitResult(): T? = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener {
            if (cont.isActive) cont.resume(null)
        }
        addOnCanceledListener {
            if (cont.isActive) cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getDeviceLocation(context: Context): DeviceLocation = withContext(Dispatchers.IO) {
        val rememberedLocation = getLastSavedLocation(context)

        try {
            var location: Location? = null

            // 1. Primary: Use Google Play Services FusedLocationProviderClient
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                
                // Try last known location first for immediate response
                location = fusedLocationClient.lastLocation.awaitResult()

                // If not cached, fetch fresh current location with balanced power accuracy
                if (location == null) {
                    location = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        null
                    ).awaitResult()
                }
            } catch (_: Exception) {
                // Play services failed or not available on device
            }

            // 2. Fallback: Standard Android LocationManager
            if (location == null) {
                try {
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    if (locationManager != null) {
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        }
                        if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        }
                    }
                } catch (_: Exception) {}
            }

            if (location == null) {
                return@withContext rememberedLocation
            }

            val lat = location.latitude
            val lon = location.longitude

            // Reverse geocode to resolve human-readable city/district name
            var resolvedCity = "Current Location"
            try {
                val geocoder = Geocoder(context, Locale.ENGLISH)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    if (!locality.isNullOrBlank()) {
                        resolvedCity = locality
                    }
                }
            } catch (_: Exception) {
                resolvedCity = if (rememberedLocation.cityName.isNotBlank() && rememberedLocation.cityName != "Current Location") {
                    rememberedLocation.cityName
                } else {
                    "Current Location"
                }
            }

            val freshLocation = DeviceLocation(
                cityName = resolvedCity,
                latitude = lat,
                longitude = lon,
                isGps = true,
                timestampMillis = System.currentTimeMillis()
            )

            // Persist the last known location
            saveLastLocation(context, freshLocation)

            freshLocation
        } catch (_: Exception) {
            rememberedLocation
        }
    }
}
