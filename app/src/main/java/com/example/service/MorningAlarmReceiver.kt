package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.model.MorningBriefing
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MorningAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MORNING_ALARM = "com.example.mornix.ACTION_MORNING_ALARM"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val sp = context.getSharedPreferences("mornix_secure_prefs", Context.MODE_PRIVATE)
        val isEnabled = sp.getBoolean("pref_morning_notification_enabled", true)
        val hour = sp.getInt("pref_morning_hour", 7)
        val minute = sp.getInt("pref_morning_minute", 0)

        if (!isEnabled) return

        // 1. Re-schedule for tomorrow to ensure uninterrupted daily cycle
        MornixNotificationHelper.scheduleDailyBriefingAlarm(context, hour, minute)

        // 2. Build or read cached morning briefing
        val briefing = loadCachedBriefing(sp) ?: createFallbackBriefing()

        // 3. Post notification with Listen action button
        MornixNotificationHelper.showMorningBriefingNotification(context, briefing)
    }

    private fun loadCachedBriefing(sp: android.content.SharedPreferences): MorningBriefing? {
        val jsonStr = sp.getString("cached_briefing_json", null) ?: return null
        return try {
            val json = JSONObject(jsonStr)
            val bvObj = json.optJSONObject("bibleVerse")
            val bibleVerse = if (bvObj != null) {
                com.example.data.model.BibleVerse(
                    reference = bvObj.optString("reference", ""),
                    text = bvObj.optString("text", ""),
                    translation = bvObj.optString("translation", "NIV"),
                    theme = bvObj.optString("theme", ""),
                    dateFormatted = bvObj.optString("dateFormatted", "")
                )
            } else null

            MorningBriefing(
                generatedAtMillis = json.optLong("generatedAtMillis", System.currentTimeMillis()),
                formattedDate = json.optString("formattedDate", SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH).format(Date())),
                greeting = json.optString("greeting", "Good Morning"),
                executiveSummary = json.optString("executiveSummary", "Your verified morning briefing is ready."),
                newsHighlights = emptyList(),
                goldForexSnippet = json.optString("goldForexSnippet", "Live verified Nepal market rates available."),
                weatherSnippet = json.optString("weatherSnippet", "Current local conditions updated."),
                alertsSnippet = json.optString("alertsSnippet", "No active natural hazards reported."),
                spokenScript = json.optString("spokenScript", "Good morning. Here is your Mornix daily update. Check the app for live rates and news."),
                isAiGenerated = true,
                bibleVerse = bibleVerse
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun createFallbackBriefing(): MorningBriefing {
        val today = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH).format(Date())
        return MorningBriefing(
            generatedAtMillis = System.currentTimeMillis(),
            formattedDate = today,
            greeting = "Good Morning",
            executiveSummary = "Your personalized Mornix morning briefing is ready with today's live bullion, forex, weather, and top headlines.",
            newsHighlights = emptyList(),
            goldForexSnippet = "FENEGOSIDA gold rates and Nepal Rastra Bank forex rates updated.",
            weatherSnippet = "Local forecasts available from Open-Meteo.",
            alertsSnippet = "Continuous monitoring for seismic and disaster alerts.",
            spokenScript = "Good morning. This is Mornix with your daily morning briefing for $today. Check your local gold, forex, and news feeds in the app. Have a productive day ahead.",
            isAiGenerated = true
        )
    }
}
