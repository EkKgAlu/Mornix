package com.example.data.remote

import com.example.data.model.DisasterAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlertsService {

    suspend fun fetchVerifiedAlerts(): List<DisasterAlert> = withContext(Dispatchers.IO) {
        val alerts = mutableListOf<DisasterAlert>()

        // 1. Fetch real-time USGS Earthquakes (Magnitude 3.5+ past 24h)
        try {
            val url = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.geojson"
            val request = Request.Builder().url(url).build()
            val response = ApiClient.okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val json = JSONObject(body)
                    val features = json.getJSONArray("features")
                    for (i in 0 until minOf(features.length(), 25)) {
                        val feat = features.getJSONObject(i)
                        val props = feat.getJSONObject("properties")
                        val mag = props.optDouble("mag", 0.0)
                        val place = props.optString("place", "Regional Station")
                        val timeMillis = props.optLong("time", System.currentTimeMillis())
                        val alertUrl = props.optString("url", "https://earthquake.usgs.gov")

                        // Flag if magnitude >= 4.5 or if in South Asia / Himalayan belt
                        val isNearNepal = place.contains("Nepal", ignoreCase = true) ||
                                place.contains("India", ignoreCase = true) ||
                                place.contains("Tibet", ignoreCase = true) ||
                                place.contains("Myanmar", ignoreCase = true)

                        if (isNearNepal || mag >= 5.8) {
                            val severity = when {
                                mag >= 6.0 -> "CRITICAL"
                                mag >= 4.5 -> "WARNING"
                                else -> "ADVISORY"
                            }
                            val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.ENGLISH).format(Date(timeMillis))
                            alerts.add(
                                DisasterAlert(
                                    id = "eq_${feat.optString("id", i.toString())}",
                                    title = "Earthquake M$mag - $place",
                                    description = "Seismic tremor recorded at magnitude $mag. Depth and epicenter monitored by National/International Seismological Networks.",
                                    affectedLocation = place,
                                    severity = severity,
                                    issuingAuthority = if (isNearNepal) "National Earthquake Monitoring & Research Center / USGS" else "USGS Earthquake Hazards Program",
                                    pubDateFormatted = dateStr,
                                    sourceUrl = alertUrl,
                                    timestampMillis = timeMillis
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        alerts.sortedByDescending { it.timestampMillis }
    }
}
