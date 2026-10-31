package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FreshnessStatus
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherInfo
import com.example.ui.components.FreshnessBadge
import com.example.ui.theme.AmberGoldLight
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.EmeraldLive
import com.example.ui.viewmodel.MornixViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeatherScreen(
    viewModel: MornixViewModel,
    modifier: Modifier = Modifier
) {
    val weather by viewModel.weather.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()

    val nepaleseCities = listOf(
        "Current Location", "Kathmandu", "Pokhara", "Biratnagar", "Itahari",
        "Dharan", "Chitwan", "Butwal", "Nepalgunj"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("weather_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // City Chips
        item {
            Column {
                Text(
                    text = "Select Weather Location (Default: Device GPS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(nepaleseCities) { city ->
                        val isSelected = prefs.preferredCity.equals(city, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setPreferredCity(city)
                                if (city == "Current Location") {
                                    viewModel.refreshDeviceLocation()
                                }
                            },
                            label = { Text(if (city == "Current Location") "Current Location (GPS)" else city) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (city == "Current Location") androidx.compose.material.icons.Icons.Default.MyLocation else Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            modifier = Modifier.testTag("weather_city_chip_${city.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }

        // Current Overview Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${weather?.cityName ?: "Kathmandu"}, Nepal",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = weather?.conditionDescription ?: "Current Weather",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        FreshnessBadge(
                            status = weather?.freshnessStatus ?: FreshnessStatus.UNAVAILABLE,
                            sourceName = "Open-Meteo"
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (weather != null) {
                        val tempVal = if (prefs.temperatureUnit == TemperatureUnit.FAHRENHEIT) {
                            (weather!!.currentTempC * 9 / 5 + 32).toInt()
                        } else {
                            weather!!.currentTempC.toInt()
                        }
                        val feelsVal = if (prefs.temperatureUnit == TemperatureUnit.FAHRENHEIT) {
                            (weather!!.feelsLikeC * 9 / 5 + 32).toInt()
                        } else {
                            weather!!.feelsLikeC.toInt()
                        }
                        val unitSym = if (prefs.temperatureUnit == TemperatureUnit.FAHRENHEIT) "°F" else "°C"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$tempVal$unitSym",
                                style = MaterialTheme.typography.headlineLarge,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Unit switch toggle
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(if (prefs.temperatureUnit == TemperatureUnit.CELSIUS) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                                        .clickable { viewModel.setTemperatureUnit(TemperatureUnit.CELSIUS) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "°C",
                                        fontWeight = FontWeight.Bold,
                                        color = if (prefs.temperatureUnit == TemperatureUnit.CELSIUS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(if (prefs.temperatureUnit == TemperatureUnit.FAHRENHEIT) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                                        .clickable { viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "°F",
                                        fontWeight = FontWeight.Bold,
                                        color = if (prefs.temperatureUnit == TemperatureUnit.FAHRENHEIT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Feels like $feelsVal$unitSym • Coordinates: ${weather!!.latitude}, ${weather!!.longitude}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Meteorological Metrics Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WeatherMetricPill(
                                label = "Humidity",
                                value = "${weather!!.humidityPercent}%",
                                icon = Icons.Default.WaterDrop,
                                modifier = Modifier.weight(1f)
                            )
                            WeatherMetricPill(
                                label = "Wind",
                                value = "${weather!!.windSpeedKmh.toInt()} km/h ${weather!!.windDirection}",
                                icon = Icons.Default.Air,
                                modifier = Modifier.weight(1f)
                            )
                            WeatherMetricPill(
                                label = "UV Index",
                                value = "${weather!!.uvIndex.toInt()} (${if (weather!!.uvIndex < 3) "Low" else if (weather!!.uvIndex < 6) "Mod" else "High"})",
                                icon = Icons.Default.WbSunny,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WeatherMetricPill(
                                label = "Air Quality (AQI)",
                                value = "${weather!!.aqi} • ${weather!!.aqiStatus}",
                                icon = Icons.Default.Air,
                                modifier = Modifier.weight(1.5f)
                            )
                            WeatherMetricPill(
                                label = "Sunrise / Sunset",
                                value = "${weather!!.sunriseTime} / ${weather!!.sunsetTime}",
                                icon = Icons.Default.WbTwilight,
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                    }
                }
            }
        }

        // Hourly Outlook Timeline
        if (weather != null && weather!!.hourlyForecast.isNotEmpty()) {
            item {
                Text(
                    text = "24-Hour Timeline",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(weather!!.hourlyForecast) { hour ->
                        val tempVal = if (prefs.temperatureUnit == TemperatureUnit.FAHRENHEIT) {
                            (hour.tempC * 9 / 5 + 32).toInt()
                        } else {
                            hour.tempC.toInt()
                        }
                        val unitSym = if (prefs.temperatureUnit == TemperatureUnit.FAHRENHEIT) "°F" else "°C"
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = hour.timeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "$tempVal$unitSym", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${hour.rainProb}% rain",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7-Day Forecast Cards
        if (weather != null && weather!!.dailyForecast.isNotEmpty()) {
            item {
                Text(
                    text = "7-Day Forecast",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(weather!!.dailyForecast) { day ->
                val maxTemp = if (prefs.temperatureUnit == TemperatureUnit.FAHRENHEIT) {
                    (day.maxTempC * 9 / 5 + 32).toInt()
                } else {
                    day.maxTempC.toInt()
                }
                val minTemp = if (prefs.temperatureUnit == TemperatureUnit.FAHRENHEIT) {
                    (day.minTempC * 9 / 5 + 32).toInt()
                } else {
                    day.minTempC.toInt()
                }
                val unitSym = if (prefs.temperatureUnit == TemperatureUnit.FAHRENHEIT) "°F" else "°C"

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = day.dayLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(90.dp)
                        )

                        Text(
                            text = "${day.rainProb}% rain chance",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$maxTemp$unitSym",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$minTemp$unitSym",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Model: High-Resolution Weather Model via Open-Meteo API. Atmospheric and precipitation observations are refreshed hourly.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun WeatherMetricPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
