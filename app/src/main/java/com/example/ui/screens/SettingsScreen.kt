package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.GoldWeightUnit
import com.example.data.model.TemperatureUnit
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.MornixViewModel

@Composable
fun SettingsScreen(
    viewModel: MornixViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs by viewModel.userPreferences.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleMorningNotification(true)
        }
    }

    val allCategories = listOf(
        "Nepal News", "World News", "Politics", "Business and Economy",
        "Technology and AI", "Science", "Cryptocurrency", "Gold and Precious Metals",
        "Weather and Climate", "Sports", "Entertainment", "Health and Science"
    )

    val allWidgets = listOf(
        Pair("briefing", "Morning Briefing Card"),
        Pair("gold_silver", "Nepal Gold & Silver Rates"),
        Pair("forex", "Foreign Exchange (NRB)"),
        Pair("weather", "Weather Snapshot"),
        Pair("alerts", "Disaster & Emergency Notices"),
        Pair("markets", "International Markets & Crypto"),
        Pair("breaking_news", "Today's Verified News")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. ABOUT SECTION (Prominently displays "Made with AI by Resham")
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_mornix_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.mornix_logo),
                            contentDescription = "Mornix App Logo",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Mornix",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Daily Intelligence & Live Briefing • v1.0.1",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // "Made with AI by Resham" Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Made with AI by Resham",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Created as an intelligent, trustworthy morning companion with zero simulated data.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Mornix brings together verified publisher RSS news, official FENEGOSIDA gold and silver rates, Nepal Rastra Bank forex fixations, Open-Meteo atmospheric forecasts, international commodities, and disaster hazard telemetry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 2. Preferred Region & Units
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        title = "Regional & Unit Preferences",
                        subtitle = "Default: Current Location (GPS) • Base currency: NPR",
                        icon = Icons.Default.LocationOn
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Default Weather Location", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (prefs.preferredCity.equals("Current Location", ignoreCase = true)) "Current Location (Active GPS Default)" else prefs.preferredCity,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Temperature Unit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = (prefs.temperatureUnit == TemperatureUnit.CELSIUS),
                            onClick = { viewModel.setTemperatureUnit(TemperatureUnit.CELSIUS) },
                            label = { Text("Celsius (°C)") }
                        )
                        FilterChip(
                            selected = (prefs.temperatureUnit == TemperatureUnit.FAHRENHEIT),
                            onClick = { viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT) },
                            label = { Text("Fahrenheit (°F)") }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Gold & Bullion Unit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Pair("Tola", GoldWeightUnit.TOLA),
                            Pair("10 Grams", GoldWeightUnit.TEN_GRAM),
                            Pair("Gram", GoldWeightUnit.GRAM),
                            Pair("Troy Oz", GoldWeightUnit.TROY_OUNCE)
                        ).forEach { (label, unit) ->
                            FilterChip(
                                selected = (prefs.goldWeightUnit == unit),
                                onClick = { viewModel.setGoldWeightUnit(unit) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }

        // 3. Morning Briefing & Disaster Notifications
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth().testTag("notifications_settings_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        title = "Morning Briefing & Disaster Alerts",
                        subtitle = "Scheduled briefings with direct audio listening in notification",
                        icon = Icons.Default.NotificationsActive
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Morning Briefing Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Morning Briefing",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Receive morning summary with a direct 'Listen' button in the notification",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = prefs.morningBriefingNotificationEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                                viewModel.toggleMorningNotification(enabled)
                            },
                            modifier = Modifier.testTag("morning_notification_switch")
                        )
                    }

                    if (prefs.morningBriefingNotificationEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Scheduled Delivery Time: ${String.format("%02d:%02d", prefs.morningBriefingHour, prefs.morningBriefingMinute)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick presets
                        val timePresets = listOf(
                            Pair(6, 0) to "06:00 AM",
                            Pair(6, 30) to "06:30 AM",
                            Pair(7, 0) to "07:00 AM",
                            Pair(7, 30) to "07:30 AM",
                            Pair(8, 0) to "08:00 AM",
                            Pair(8, 30) to "08:30 AM"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            timePresets.take(3).forEach { (time, label) ->
                                val selected = prefs.morningBriefingHour == time.first && prefs.morningBriefingMinute == time.second
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.setMorningNotificationTime(time.first, time.second) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            timePresets.drop(3).forEach { (time, label) ->
                                val selected = prefs.morningBriefingHour == time.first && prefs.morningBriefingMinute == time.second
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.setMorningNotificationTime(time.first, time.second) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "When the notification arrives, tap '▶ Listen to Briefing' to hear the live audio brief without unlocking the phone.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Natural Disaster Push Alerts Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Natural Disaster & Seismic Alerts",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "High-priority alerts for earthquakes, flash floods, and active meteorological warnings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = prefs.disasterAlertsNotificationEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                                viewModel.toggleDisasterNotification(enabled)
                            },
                            modifier = Modifier.testTag("disaster_notification_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test Buttons Row
                    Text(
                        text = "Instant Notification Preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                                viewModel.sendTestMorningBriefingNotification()
                            },
                            modifier = Modifier.weight(1f).testTag("test_morning_briefing_btn")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Briefing", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                                viewModel.sendTestDisasterAlertNotification()
                            },
                            modifier = Modifier.weight(1f).testTag("test_disaster_alert_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Disaster", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 4. Daily Bible Verse & Spiritual Reflection
        item {
            val todayVerse = viewModel.getTodayBibleVerse()
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth().testTag("bible_verse_settings_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        title = "Daily Bible Verse",
                        subtitle = "Optional scripture in your morning brief (Default: Off)",
                        icon = Icons.AutoMirrored.Filled.MenuBook
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Bible Verse",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Include an authentic scripture passage in your daily morning briefing and spoken audio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = prefs.dailyBibleVerseEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.toggleDailyBibleVerse(enabled)
                            },
                            modifier = Modifier.testTag("bible_verse_switch")
                        )
                    }

                    if (prefs.dailyBibleVerseEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
                                .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Today's Scripture: ${todayVerse.reference}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = todayVerse.translation,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "\"${todayVerse.text}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Theme: ${todayVerse.theme} • Appears in your Morning Briefing card and audio narration",
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

        // 5. Text-To-Speech (TTS) Settings
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        title = "Audio Briefing (Text-To-Speech)",
                        subtitle = "Adjust audio narration playback speed",
                        icon = Icons.Default.RecordVoiceOver
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Speech Rate", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                                FilterChip(
                                    selected = (speechRate == speed),
                                    onClick = { viewModel.setTtsSpeed(speed) },
                                    label = { Text("${speed}x") }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Dashboard Widget Customization
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        title = "Home Dashboard Widgets",
                        subtitle = "Enable or disable cards to customize your home feed",
                        icon = Icons.Default.Tune
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    allWidgets.forEach { (widgetId, label) ->
                        val isEnabled = prefs.enabledWidgets.contains(widgetId)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { viewModel.toggleWidget(widgetId) }
                            )
                        }
                    }
                }
            }
        }

        // 5. News Interests Personalization
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        title = "Personalized News Interests",
                        subtitle = "Selected topics appear in your priority feed and daily briefing",
                        icon = Icons.Default.VerifiedUser
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    allCategories.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { cat ->
                                val isSelected = prefs.selectedNewsCategories.contains(cat)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.toggleNewsInterest(cat) },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // 6. Transparency, Data Sources & Verifiable Citations
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        title = "Data Sources & Transparency",
                        subtitle = "100% verified live providers • Zero synthetic or simulated data",
                        icon = Icons.Default.Info
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DataSourceRow(
                        name = "Nepal Bullion (Gold & Silver)",
                        provider = "Federation of Nepal Gold & Silver Dealers' Association (FENEGOSIDA)",
                        url = "https://fenegosida.org",
                        context = context
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    DataSourceRow(
                        name = "Foreign Exchange (Forex)",
                        provider = "Nepal Rastra Bank (NRB Central Bank API)",
                        url = "https://www.nrb.org.np",
                        context = context
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    DataSourceRow(
                        name = "Weather & Forecasts",
                        provider = "Open-Meteo High-Resolution Atmospheric Model",
                        url = "https://open-meteo.com",
                        context = context
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    DataSourceRow(
                        name = "Seismic & Disaster Hazards",
                        provider = "USGS Earthquake Hazards Program / National Seismological Monitoring",
                        url = "https://earthquake.usgs.gov",
                        context = context
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    DataSourceRow(
                        name = "News Feeds",
                        provider = "OnlineKhabar, Nepali Times, BBC World, TechCrunch, CoinDesk RSS",
                        url = "https://english.onlinekhabar.com",
                        context = context
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Mornix Principle: Correctness over completeness. When an external service is offline or unverified, Mornix displays OLD DATA or DATA UNAVAILABLE rather than fabricating information.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun DataSourceRow(
    name: String,
    provider: String,
    url: String,
    context: android.content.Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = provider, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Open Source Link",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
    }
}
