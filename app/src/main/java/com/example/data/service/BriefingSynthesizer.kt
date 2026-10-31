package com.example.data.service

import com.example.data.model.DisasterAlert
import com.example.data.model.ForexRateItem
import com.example.data.model.GoldSilverRates
import com.example.data.model.MorningBriefing
import com.example.data.model.NewsArticle
import com.example.data.model.UserPreferences
import com.example.data.model.WeatherInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BriefingSynthesizer {

    fun generateBriefing(
        goldSilver: GoldSilverRates?,
        forexList: List<ForexRateItem>,
        weather: WeatherInfo?,
        articles: List<NewsArticle>,
        alerts: List<DisasterAlert>,
        prefs: UserPreferences
    ): MorningBriefing {
        val now = Date()
        val formattedDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH).format(now)
        val hour = SimpleDateFormat("H", Locale.ENGLISH).format(now).toIntOrNull() ?: 8
        val greeting = when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }

        // 1. Weather snippet
        val weatherSnippet = if (weather != null) {
            "${weather.cityName} is currently ${weather.currentTempC.toInt()}°C with ${weather.conditionDescription}. " +
                    "Today's high is expected near ${weather.dailyForecast.firstOrNull()?.maxTempC?.toInt() ?: (weather.currentTempC + 4).toInt()}°C. " +
                    "Air quality index is ${weather.aqi} (${weather.aqiStatus}). (Source: ${weather.sourceName})"
        } else {
            "Weather conditions for Nepal are currently unavailable."
        }

        // 2. Gold & Silver snippet
        val goldSnippet = if (goldSilver != null && goldSilver.fineGoldTola > 0) {
            val goldChangeText = if (goldSilver.changeGoldTola > 0) {
                "up by NPR ${String.format(Locale.ENGLISH, "%,.0f", goldSilver.changeGoldTola)} (+${String.format(Locale.ENGLISH, "%.2f", goldSilver.changeGoldTolaPercent)}%)"
            } else if (goldSilver.changeGoldTola < 0) {
                "down by NPR ${String.format(Locale.ENGLISH, "%,.0f", -goldSilver.changeGoldTola)} (${String.format(Locale.ENGLISH, "%.2f", goldSilver.changeGoldTolaPercent)}%)"
            } else {
                "unchanged from yesterday"
            }
            "In Nepal, Fine Gold is trading at NPR ${String.format(Locale.ENGLISH, "%,.0f", goldSilver.fineGoldTola)} per tola, $goldChangeText. " +
                    "Silver is at NPR ${String.format(Locale.ENGLISH, "%,.0f", goldSilver.silverTola)} per tola. (Source: FENEGOSIDA, ${goldSilver.publishedDate})"
        } else {
            "Official local gold and silver rates from FENEGOSIDA are currently awaiting publication."
        }

        // 3. Forex snippet
        val usdRate = forexList.find { it.currencyCode == "USD" }
        val inrRate = forexList.find { it.currencyCode == "INR" }
        val eurRate = forexList.find { it.currencyCode == "EUR" }
        val forexSnippet = if (usdRate != null) {
            val inrText = if (inrRate != null) "and INR 100 is fixed at NPR ${inrRate.buyRate}" else ""
            "Nepal Rastra Bank official exchange rate fixes 1 U.S. Dollar at NPR ${usdRate.buyRate} buy / NPR ${usdRate.sellRate} sell, $inrText. " +
                    (if (eurRate != null) "Euro is at NPR ${eurRate.buyRate} buy. " else "") +
                    "(Source: NRB, ${usdRate.date})"
        } else {
            "Nepal Rastra Bank foreign exchange rates are currently pending update."
        }

        // 4. Personalized News Highlights
        val prioritizedArticles = articles.filter { article ->
            prefs.selectedNewsCategories.any { cat -> article.category.equals(cat, ignoreCase = true) }
        }.ifEmpty { articles }

        val topNepalArticle = prioritizedArticles.firstOrNull { it.category == "Nepal News" } ?: articles.firstOrNull { it.category == "Nepal News" }
        val topWorldArticle = prioritizedArticles.firstOrNull { it.category != "Nepal News" } ?: articles.firstOrNull { it.category != "Nepal News" }
        val otherHighlights = prioritizedArticles.filter { it.id != topNepalArticle?.id && it.id != topWorldArticle?.id }.take(3)

        val newsHighlights = mutableListOf<String>()
        topNepalArticle?.let {
            newsHighlights.add("[Nepal] ${it.title} (${it.sourceName}, ${it.freshnessLabel})")
        }
        topWorldArticle?.let {
            newsHighlights.add("[World] ${it.title} (${it.sourceName}, ${it.freshnessLabel})")
        }
        otherHighlights.forEach {
            newsHighlights.add("[${it.category}] ${it.title} (${it.sourceName})")
        }

        // 5. Alerts snippet
        val alertsSnippet = if (alerts.isNotEmpty()) {
            val topAlert = alerts.first()
            "NOTICE: ${topAlert.title}. ${topAlert.description} (Authority: ${topAlert.issuingAuthority})"
        } else {
            "All national seismic and meteorological stations report normal baseline conditions. No critical disaster alerts active."
        }

        // 6. Daily Scripture Verse (if enabled by user, default is off)
        val bibleVerseService = BibleVerseService()
        val dailyVerse = if (prefs.dailyBibleVerseEnabled) {
            bibleVerseService.getDailyVerse(now)
        } else {
            null
        }

        // 7. Executive summary
        val executiveSummary = buildString {
            append("$greeting! Welcome to your Mornix morning briefing for $formattedDate. ")
            if (weather != null) {
                append("In ${weather.cityName}, expect ${weather.conditionDescription.lowercase()} today with a high of ${weather.dailyForecast.firstOrNull()?.maxTempC?.toInt() ?: weather.currentTempC.toInt()}°C. ")
            }
            if (goldSilver != null && goldSilver.fineGoldTola > 0) {
                append("Local gold opened at NPR ${String.format(Locale.ENGLISH, "%,.0f", goldSilver.fineGoldTola)} per tola. ")
            }
            if (topNepalArticle != null) {
                append("Top story: \"${topNepalArticle.title}\" reported by ${topNepalArticle.sourceName}. ")
            }
            if (dailyVerse != null) {
                append("Daily Verse (${dailyVerse.reference}): \"${dailyVerse.text}\" ")
            }
        }

        // 8. Spoken Script for TTS (including daily scripture if enabled)
        val spokenScript = buildString {
            append("$greeting. This is your Mornix intelligence briefing for $formattedDate. ")
            append("First, weather in ${weather?.cityName ?: "Nepal"}. ${weatherSnippet.replace(Regex("\\(Source:[^\\)]*\\)"), "")} ")
            append("Financial markets and precious metals. ${goldSnippet.replace(Regex("\\(Source:[^\\)]*\\)"), "")} ")
            append("Foreign exchange rates. ${forexSnippet.replace(Regex("\\(Source:[^\\)]*\\)"), "")} ")
            append("Today's leading news developments. ")
            if (topNepalArticle != null) {
                append("In Nepal: ${topNepalArticle.title}. ${topNepalArticle.summary.take(160)}. ")
            }
            if (topWorldArticle != null) {
                append("In global news: ${topWorldArticle.title}. ${topWorldArticle.summary.take(160)}. ")
            }
            if (alerts.isNotEmpty()) {
                append("Emergency and alerts update: ${alertsSnippet}. ")
            }
            if (dailyVerse != null) {
                append("Today's scripture reflection from ${dailyVerse.reference}: \"${dailyVerse.text}\". May peace accompany your day. ")
            }
            append("That concludes your Mornix briefing. Have a productive and informed day ahead.")
        }

        return MorningBriefing(
            generatedAtMillis = System.currentTimeMillis(),
            formattedDate = formattedDate,
            greeting = greeting,
            executiveSummary = executiveSummary,
            newsHighlights = newsHighlights,
            goldForexSnippet = "$goldSnippet\n\n$forexSnippet",
            weatherSnippet = weatherSnippet,
            alertsSnippet = alertsSnippet,
            spokenScript = spokenScript,
            isAiGenerated = true,
            bibleVerse = dailyVerse
        )
    }
}
