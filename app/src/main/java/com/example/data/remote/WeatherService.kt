package com.example.data.remote

import com.example.data.model.DayWeather
import com.example.data.model.FreshnessStatus
import com.example.data.model.HourWeather
import com.example.data.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherService {

    data class CityCoordinates(val name: String, val lat: Double, val lon: Double)

    val popularCities = listOf(
        CityCoordinates("Kathmandu", 27.7172, 85.3240),
        CityCoordinates("Pokhara", 28.2096, 83.9856),
        CityCoordinates("Biratnagar", 26.4525, 87.2718),
        CityCoordinates("Itahari", 26.6667, 87.2833),
        CityCoordinates("Dharan", 26.8124, 87.2834),
        CityCoordinates("Chitwan", 27.6833, 84.4333),
        CityCoordinates("Butwal", 27.7006, 83.4484),
        CityCoordinates("Nepalgunj", 28.0500, 81.6167)
    )

    suspend fun fetchWeather(cityName: String, lat: Double, lon: Double): Result<WeatherInfo> = withContext(Dispatchers.IO) {
        try {
            val weatherUrl = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m" +
                    "&hourly=temperature_2m,weather_code,precipitation_probability" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset,uv_index_max" +
                    "&timezone=auto"

            val request = Request.Builder().url(weatherUrl).build()
            val response = ApiClient.okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code} from Open-Meteo"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body from Open-Meteo"))
            val json = JSONObject(body)

            val current = json.getJSONObject("current")
            val currentTemp = current.optDouble("temperature_2m", 20.0)
            val feelsLike = current.optDouble("apparent_temperature", currentTemp)
            val humidity = current.optInt("relative_humidity_2m", 60)
            val windSpeed = current.optDouble("wind_speed_10m", 5.0)
            val windDirDeg = current.optInt("wind_direction_10m", 0)
            val precip = current.optDouble("precipitation", 0.0)
            val code = current.optInt("weather_code", 0)

            // Hourly parsing
            val hourlyList = mutableListOf<HourWeather>()
            val hourlyObj = json.optJSONObject("hourly")
            if (hourlyObj != null) {
                val times = hourlyObj.optJSONArray("time")
                val temps = hourlyObj.optJSONArray("temperature_2m")
                val codes = hourlyObj.optJSONArray("weather_code")
                val probs = hourlyObj.optJSONArray("precipitation_probability")
                val count = minOf(times?.length() ?: 0, 24)
                for (i in 0 until count) {
                    val rawTime = times?.optString(i, "") ?: ""
                    val hourLabel = formatHour(rawTime)
                    val t = temps?.optDouble(i, currentTemp) ?: currentTemp
                    val c = codes?.optInt(i, 0) ?: 0
                    val p = probs?.optInt(i, 0) ?: 0
                    hourlyList.add(HourWeather(hourLabel, t, c, p))
                }
            }

            // Daily parsing
            val dailyList = mutableListOf<DayWeather>()
            var uvMax = 5.0
            var sunrise = "06:12 AM"
            var sunset = "05:35 PM"
            var rainProbMax = 10

            val dailyObj = json.optJSONObject("daily")
            if (dailyObj != null) {
                val dates = dailyObj.optJSONArray("time")
                val maxTemps = dailyObj.optJSONArray("temperature_2m_max")
                val minTemps = dailyObj.optJSONArray("temperature_2m_min")
                val dailyCodes = dailyObj.optJSONArray("weather_code")
                val dailyProbs = dailyObj.optJSONArray("precipitation_probability_max")
                val sunrises = dailyObj.optJSONArray("sunrise")
                val sunsets = dailyObj.optJSONArray("sunset")
                val uvs = dailyObj.optJSONArray("uv_index_max")

                val count = minOf(dates?.length() ?: 0, 7)
                for (i in 0 until count) {
                    val rawDate = dates?.optString(i, "") ?: ""
                    val dayLabel = formatDayOfWeek(rawDate, i)
                    val maxT = maxTemps?.optDouble(i, currentTemp + 4.0) ?: (currentTemp + 4.0)
                    val minT = minTemps?.optDouble(i, currentTemp - 5.0) ?: (currentTemp - 5.0)
                    val c = dailyCodes?.optInt(i, 0) ?: 0
                    val p = dailyProbs?.optInt(i, 0) ?: 0
                    dailyList.add(DayWeather(dayLabel, maxT, minT, c, p))
                }

                if ((uvs?.length() ?: 0) > 0) uvMax = uvs?.optDouble(0, 5.0) ?: 5.0
                if ((sunrises?.length() ?: 0) > 0) sunrise = formatSunTime(sunrises?.optString(0, "") ?: "")
                if ((sunsets?.length() ?: 0) > 0) sunset = formatSunTime(sunsets?.optString(0, "") ?: "")
                if ((dailyProbs?.length() ?: 0) > 0) rainProbMax = dailyProbs?.optInt(0, 10) ?: 10
            }

            // Optional AQI check
            val aqiValue = estimateAirQuality(lat, lon)
            val aqiDesc = when {
                aqiValue <= 50 -> "Good"
                aqiValue <= 100 -> "Moderate"
                aqiValue <= 150 -> "Unhealthy for Sensitive Groups"
                aqiValue <= 200 -> "Unhealthy"
                else -> "Very Unhealthy"
            }

            val condition = getWeatherConditionDescription(code)
            val windDirection = degreesToCompass(windDirDeg)

            val info = WeatherInfo(
                cityName = cityName,
                country = "Nepal",
                latitude = lat,
                longitude = lon,
                currentTempC = currentTemp,
                feelsLikeC = feelsLike,
                humidityPercent = humidity,
                windSpeedKmh = windSpeed,
                windDirection = windDirection,
                precipitationMm = precip,
                rainProbability = rainProbMax,
                uvIndex = uvMax,
                aqi = aqiValue,
                aqiStatus = aqiDesc,
                weatherCode = code,
                conditionDescription = condition,
                sunriseTime = sunrise,
                sunsetTime = sunset,
                hourlyForecast = hourlyList,
                dailyForecast = dailyList,
                sourceName = "Open-Meteo High-Resolution Model",
                sourceUrl = "https://open-meteo.com",
                freshnessStatus = FreshnessStatus.LIVE,
                retrievedAtMillis = System.currentTimeMillis()
            )

            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun estimateAirQuality(lat: Double, lon: Double): Int {
        return try {
            val aqiUrl = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$lat&longitude=$lon&current=european_aqi,pm2_5"
            val request = Request.Builder().url(aqqiUrlSafe(aqiUrl)).build()
            val resp = ApiClient.okHttpClient.newCall(request).execute()
            if (resp.isSuccessful) {
                val b = resp.body?.string()
                if (b != null) {
                    val j = JSONObject(b)
                    val c = j.optJSONObject("current")
                    val pm = c?.optDouble("pm2_5", 35.0) ?: 35.0
                    return (pm * 2.5).toInt().coerceIn(30, 220)
                }
            }
            65
        } catch (_: Exception) {
            65
        }
    }

    private fun aqqiUrlSafe(url: String): String = url

    fun getWeatherConditionDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy & Hazy"
            51, 53, 55 -> "Light Drizzle"
            61, 63, 65 -> "Rain Showers"
            71, 73, 75 -> "Snowfall"
            80, 81, 82 -> "Heavy Rain Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Pleasant Weather"
        }
    }

    private fun degreesToCompass(deg: Int): String {
        val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = ((deg / 22.5) + 0.5).toInt() % 16
        return directions[index]
    }

    private fun formatHour(raw: String): String {
        return try {
            val timePart = raw.substringAfter("T")
            val hour = timePart.substringBefore(":").toInt()
            when {
                hour == 0 -> "12 AM"
                hour < 12 -> "$hour AM"
                hour == 12 -> "12 PM"
                else -> "${hour - 12} PM"
            }
        } catch (_: Exception) {
            raw
        }
    }

    private fun formatDayOfWeek(raw: String, index: Int): String {
        if (index == 0) return "Today"
        if (index == 1) return "Tomorrow"
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val d = format.parse(raw) ?: Date()
            SimpleDateFormat("EEE", Locale.ENGLISH).format(d)
        } catch (_: Exception) {
            "Day $index"
        }
    }

    private fun formatSunTime(raw: String): String {
        return try {
            val timePart = raw.substringAfter("T")
            val parts = timePart.split(":")
            val hour = parts[0].toInt()
            val min = parts[1]
            when {
                hour == 0 -> "12:$min AM"
                hour < 12 -> "$hour:$min AM"
                hour == 12 -> "12:$min PM"
                else -> "${hour - 12}:$min PM"
            }
        } catch (_: Exception) {
            raw
        }
    }
}
