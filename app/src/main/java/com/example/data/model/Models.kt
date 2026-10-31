package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.MorningSkyBlue
import com.example.ui.theme.StaleOrange

enum class FreshnessStatus(
    val label: String,
    val color: Color,
    val description: String
) {
    LIVE("LIVE", EmeraldLive, "Real-time live feed from provider"),
    UPDATED("UPDATED", MorningSkyBlue, "Recently verified and fresh"),
    DELAYED("DELAYED", StaleOrange, "Official published rate / delayed session"),
    STALE("OLD DATA", StaleOrange, "Previous cached value; network/provider offline"),
    UNAVAILABLE("DATA UNAVAILABLE", CoralWarning, "No verified data could be retrieved")
}

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val sourceName: String,
    val sourceUrl: String,
    val imageUrl: String? = null,
    val pubDateMillis: Long,
    val pubDateFormatted: String,
    val freshnessLabel: String,
    val category: String,
    val isBreaking: Boolean = false,
    val isBookmarked: Boolean = false,
    val aiSummary: String? = null
)

data class GoldSilverRates(
    val fineGold10g: Double,
    val fineGoldTola: Double,
    val tejabiGold10g: Double,
    val tejabiGoldTola: Double,
    val silver10g: Double,
    val silverTola: Double,
    val publishedDate: String,
    val publishedTime: String,
    val yesterdayFineGoldTola: Double,
    val yesterdaySilverTola: Double,
    val changeGoldTola: Double,
    val changeGoldTolaPercent: Double,
    val changeSilverTola: Double,
    val changeSilverTolaPercent: Double,
    val sourceName: String = "Federation of Nepal Gold & Silver Dealers' Association (FENEGOSIDA)",
    val sourceUrl: String = "https://fenegosida.org",
    val freshnessStatus: FreshnessStatus = FreshnessStatus.LIVE,
    val retrievedAtMillis: Long = System.currentTimeMillis()
)

data class ForexRateItem(
    val currencyCode: String,
    val currencyName: String,
    val flagEmoji: String,
    val unit: Int,
    val buyRate: Double,
    val sellRate: Double,
    val date: String,
    val publishedOn: String,
    val freshnessStatus: FreshnessStatus = FreshnessStatus.LIVE,
    val isFavorite: Boolean = false
)

data class HourWeather(
    val timeLabel: String,
    val tempC: Double,
    val weatherCode: Int,
    val rainProb: Int
)

data class DayWeather(
    val dayLabel: String,
    val maxTempC: Double,
    val minTempC: Double,
    val weatherCode: Int,
    val rainProb: Int
)

data class WeatherInfo(
    val cityName: String,
    val country: String = "Nepal",
    val latitude: Double,
    val longitude: Double,
    val currentTempC: Double,
    val feelsLikeC: Double,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirection: String,
    val precipitationMm: Double,
    val rainProbability: Int,
    val uvIndex: Double,
    val aqi: Int,
    val aqiStatus: String,
    val weatherCode: Int,
    val conditionDescription: String,
    val sunriseTime: String,
    val sunsetTime: String,
    val hourlyForecast: List<HourWeather>,
    val dailyForecast: List<DayWeather>,
    val sourceName: String = "Open-Meteo High-Resolution Weather Model",
    val sourceUrl: String = "https://open-meteo.com",
    val freshnessStatus: FreshnessStatus = FreshnessStatus.LIVE,
    val retrievedAtMillis: Long = System.currentTimeMillis()
)

data class MarketQuote(
    val symbol: String,
    val name: String,
    val category: String, // "Commodity", "Crypto", "Index", "Forex"
    val price: Double,
    val change24h: Double,
    val changePercent: Double,
    val currencySymbol: String = "$",
    val historyPoints: List<Double> = emptyList(),
    val marketStatus: String = "Active",
    val source: String,
    val sourceUrl: String,
    val freshnessStatus: FreshnessStatus = FreshnessStatus.LIVE,
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

data class DisasterAlert(
    val id: String,
    val title: String,
    val description: String,
    val affectedLocation: String,
    val severity: String, // "HIGH", "MODERATE", "ADVISORY"
    val issuingAuthority: String,
    val pubDateFormatted: String,
    val sourceUrl: String,
    val timestampMillis: Long
)

data class BibleVerse(
    val reference: String,
    val text: String,
    val translation: String = "NIV",
    val theme: String = "Wisdom & Peace",
    val dateFormatted: String = ""
)

data class MorningBriefing(
    val generatedAtMillis: Long,
    val formattedDate: String,
    val greeting: String,
    val executiveSummary: String,
    val newsHighlights: List<String>,
    val goldForexSnippet: String,
    val weatherSnippet: String,
    val alertsSnippet: String,
    val spokenScript: String,
    val isAiGenerated: Boolean = true,
    val bibleVerse: BibleVerse? = null
)

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }
enum class GoldWeightUnit { TOLA, GRAM, TEN_GRAM, TROY_OUNCE }

data class UserPreferences(
    val preferredCity: String = "Current Location",
    val preferredCountry: String = "Nepal",
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val goldWeightUnit: GoldWeightUnit = GoldWeightUnit.TOLA,
    val favoriteCurrencies: List<String> = listOf("USD", "INR", "EUR", "AED", "GBP", "AUD"),
    val favoriteMarkets: List<String> = listOf("BTC", "ETH", "GC=F", "SI=F", "CL=F", "^GSPC"),
    val selectedNewsCategories: List<String> = listOf(
        "Nepal News", "World News", "Politics", "Business and Economy",
        "Technology and AI", "Gold and Precious Metals", "Weather and Climate"
    ),
    val enabledWidgets: List<String> = listOf(
        "briefing", "gold_silver", "forex", "weather", "breaking_news", "markets", "alerts"
    ),
    val ttsSpeed: Float = 1.0f,
    val autoRefreshMinutes: Int = 15,
    val morningBriefingNotificationEnabled: Boolean = true,
    val morningBriefingHour: Int = 7,
    val morningBriefingMinute: Int = 0,
    val disasterAlertsNotificationEnabled: Boolean = true,
    val dailyBibleVerseEnabled: Boolean = false
)
