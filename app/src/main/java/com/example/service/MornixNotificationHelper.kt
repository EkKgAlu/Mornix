package com.example.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.DisasterAlert
import com.example.data.model.MorningBriefing
import java.util.Calendar

object MornixNotificationHelper {

    const val CHANNEL_BRIEFING_ID = "mornix_morning_briefing_channel"
    const val CHANNEL_DISASTER_ID = "mornix_disaster_alerts_channel"
    const val CHANNEL_AUDIO_ID = "mornix_audio_playback_channel"

    const val NOTIFICATION_BRIEFING_ID = 1001
    const val NOTIFICATION_DISASTER_BASE_ID = 2000
    const val NOTIFICATION_AUDIO_ID = 3001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // Channel 1: Morning Briefing
            val briefingChannel = NotificationChannel(
                CHANNEL_BRIEFING_ID,
                "Daily Morning Briefings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily scheduled morning news, financial markets, and weather briefing"
                enableVibration(true)
            }

            // Channel 2: Disaster & Hazard Alerts
            val disasterChannel = NotificationChannel(
                CHANNEL_DISASTER_ID,
                "Emergency & Disaster Hazards",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical earthquake, seismic, and severe weather disaster advisories"
                enableVibration(true)
            }

            // Channel 3: Audio Playback
            val audioChannel = NotificationChannel(
                CHANNEL_AUDIO_ID,
                "Briefing Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls and status for reading morning briefings aloud"
            }

            nm.createNotificationChannel(briefingChannel)
            nm.createNotificationChannel(disasterChannel)
            nm.createNotificationChannel(audioChannel)
        }
    }

    fun showMorningBriefingNotification(context: Context, briefing: MorningBriefing) {
        createNotificationChannels(context)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // 1. Content Intent: Open MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Action Intent: Listen to Briefing Audio
        val listenIntent = Intent(context, BriefingAudioService::class.java).apply {
            action = BriefingAudioService.ACTION_PLAY
            putExtra(BriefingAudioService.EXTRA_SCRIPT, briefing.spokenScript)
            putExtra(BriefingAudioService.EXTRA_TITLE, "Mornix Daily Briefing • ${briefing.formattedDate}")
        }
        val listenPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                context,
                1,
                listenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                context,
                1,
                listenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_BRIEFING_ID)
            .setSmallIcon(R.drawable.mornix_logo)
            .setContentTitle("☀️ ${briefing.greeting} • Mornix Briefing")
            .setContentText(briefing.executiveSummary)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "${briefing.executiveSummary}\n\n" +
                                "📊 Gold & Forex: ${briefing.goldForexSnippet}\n" +
                                "🌦️ Weather: ${briefing.weatherSnippet}"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_media_play,
                "▶ Listen to Briefing",
                listenPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                "Open Mornix",
                openPendingIntent
            )
            .build()

        nm.notify(NOTIFICATION_BRIEFING_ID, notification)
    }

    fun showDisasterNotification(context: Context, alert: DisasterAlert) {
        createNotificationChannels(context)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            alert.id.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DISASTER_ID)
            .setSmallIcon(R.drawable.mornix_logo)
            .setContentTitle("🚨 ${alert.severity}: ${alert.title}")
            .setContentText(alert.description)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${alert.description}\n\nLocation: ${alert.affectedLocation}\nAuthority: ${alert.issuingAuthority}\nTime: ${alert.pubDateFormatted}")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()

        nm.notify(NOTIFICATION_DISASTER_BASE_ID + (alert.id.hashCode() % 1000), notification)
    }

    fun scheduleDailyBriefingAlarm(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, MorningAlarmReceiver::class.java).apply {
            action = MorningAlarmReceiver.ACTION_MORNING_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            201,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If selected time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // If exact alarm permission is restricted, fallback to inexact repeating
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }

    fun cancelDailyBriefingAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, MorningAlarmReceiver::class.java).apply {
            action = MorningAlarmReceiver.ACTION_MORNING_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            201,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
