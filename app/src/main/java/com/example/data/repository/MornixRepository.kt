package com.example.data.repository

import android.content.Context
import com.example.data.model.DisasterAlert
import com.example.data.model.ForexRateItem
import com.example.data.model.FreshnessStatus
import com.example.data.model.GoldSilverRates
import com.example.data.model.GoldWeightUnit
import com.example.data.model.MarketQuote
import com.example.data.model.MorningBriefing
import com.example.data.model.NewsArticle
import com.example.data.model.TemperatureUnit
import com.example.data.model.UserPreferences
import com.example.data.model.WeatherInfo
import com.example.data.remote.AlertsService
import com.example.data.remote.LocationHelper
import com.example.data.remote.MarketsService
import com.example.data.remote.NepalBullionService
import com.example.data.remote.NepalForexService
import com.example.data.calendar.SavedBirthDate
import com.example.data.remote.NewsRssService
import com.example.data.remote.WeatherService
import com.example.data.service.BibleVerseService
import com.example.data.service.BriefingSynthesizer
import com.example.data.service.ChatMessage
import com.example.data.service.MornixChatService
import com.example.service.MornixNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MornixRepository(
    private val context: Context,
    private val bullionService: NepalBullionService = NepalBullionService(),
    private val forexService: NepalForexService = NepalForexService(),
    private val weatherService: WeatherService = WeatherService(),
    private val newsService: NewsRssService = NewsRssService(),
    private val marketsService: MarketsService = MarketsService(),
    private val alertsService: AlertsService = AlertsService(),
    private val synthesizer: BriefingSynthesizer = BriefingSynthesizer(),
    private val chatService: MornixChatService = MornixChatService(),
    private val bibleVerseService: BibleVerseService = BibleVerseService()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences("mornix_secure_prefs", Context.MODE_PRIVATE)

    private val _bullionRates = MutableStateFlow<GoldSilverRates?>(null)
    val bullionRates: StateFlow<GoldSilverRates?> = _bullionRates.asStateFlow()

    private val _forexRates = MutableStateFlow<List<ForexRateItem>>(emptyList())
    val forexRates: StateFlow<List<ForexRateItem>> = _forexRates.asStateFlow()

    private val _weather = MutableStateFlow<WeatherInfo?>(null)
    val weather: StateFlow<WeatherInfo?> = _weather.asStateFlow()

    private val _news = MutableStateFlow<List<NewsArticle>>(emptyList())
    val news: StateFlow<List<NewsArticle>> = _news.asStateFlow()

    private val _markets = MutableStateFlow<List<MarketQuote>>(emptyList())
    val markets: StateFlow<List<MarketQuote>> = _markets.asStateFlow()

    private val _alerts = MutableStateFlow<List<DisasterAlert>>(emptyList())
    val alerts: StateFlow<List<DisasterAlert>> = _alerts.asStateFlow()

    private val _briefing = MutableStateFlow<MorningBriefing?>(null)
    val briefing: StateFlow<MorningBriefing?> = _briefing.asStateFlow()

    private val _userPreferences = MutableStateFlow(loadPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(chatService.history)
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _savedBirthDate = MutableStateFlow<SavedBirthDate?>(null)
    val savedBirthDate: StateFlow<SavedBirthDate?> = _savedBirthDate.asStateFlow()

    private val bookmarkedIds = mutableSetOf<String>()

    init {
        loadBookmarkedIds()
        loadCachedData()
        loadSavedBirthDate()
        MornixNotificationHelper.createNotificationChannels(context)
        if (_userPreferences.value.morningBriefingNotificationEnabled) {
            MornixNotificationHelper.scheduleDailyBriefingAlarm(
                context,
                _userPreferences.value.morningBriefingHour,
                _userPreferences.value.morningBriefingMinute
            )
        }
        refreshAllData()
    }

    fun refreshAllData() {
        scope.launch {
            _isRefreshing.value = true
            try {
                // 1. Fetch Bullion Rates
                val bullionResult = bullionService.fetchOfficialBullionRates()
                if (bullionResult.isSuccess) {
                    val rates = bullionResult.getOrNull()
                    _bullionRates.value = rates
                    rates?.let { saveCachedBullion(it) }
                } else if (_bullionRates.value != null) {
                    _bullionRates.value = _bullionRates.value?.copy(freshnessStatus = FreshnessStatus.STALE)
                }

                // 2. Fetch Forex Rates with User Favorites Sorted to the Front
                val forexResult = forexService.fetchOfficialForexRates()
                if (forexResult.isSuccess) {
                    val rawList = forexResult.getOrNull() ?: emptyList()
                    val favCurrencies = _userPreferences.value.favoriteCurrencies
                    val mapped = rawList.map { it.copy(isFavorite = favCurrencies.contains(it.currencyCode)) }
                        .sortedByDescending { it.isFavorite }
                    _forexRates.value = mapped
                    saveCachedForex(mapped)
                } else if (_forexRates.value.isNotEmpty()) {
                    _forexRates.value = _forexRates.value.map { it.copy(freshnessStatus = FreshnessStatus.STALE) }
                }

                // 3. Fetch Weather (Default: Device Current Location, or remembered coordinates)
                val city = _userPreferences.value.preferredCity
                val weatherResult = if (city.equals("Current Location", ignoreCase = true)) {
                    val loc = LocationHelper.getDeviceLocation(context)
                    val displayCity = if (loc.cityName.isNotBlank() && loc.cityName != "Current Location") loc.cityName else "Current Location"
                    weatherService.fetchWeather(displayCity, loc.latitude, loc.longitude)
                } else {
                    val coords = weatherService.popularCities.find { it.name.equals(city, ignoreCase = true) }
                        ?: weatherService.popularCities.first()
                    weatherService.fetchWeather(coords.name, coords.lat, coords.lon)
                }

                if (weatherResult.isSuccess) {
                    val w = weatherResult.getOrNull()
                    _weather.value = w
                    w?.let { saveCachedWeather(it) }
                } else if (_weather.value != null) {
                    _weather.value = _weather.value?.copy(freshnessStatus = FreshnessStatus.STALE)
                }

                // 4. Fetch News from All Configured Local and International RSS Sources
                val newsArticles = newsService.fetchAllFeeds()
                if (newsArticles.isNotEmpty()) {
                    val withBookmarks = newsArticles.map { it.copy(isBookmarked = bookmarkedIds.contains(it.id)) }
                    _news.value = withBookmarks
                }

                // 5. Fetch International Markets with User Favorites Sorted to the Front
                val quotes = marketsService.fetchAllMarkets()
                if (quotes.isNotEmpty()) {
                    val favMarkets = _userPreferences.value.favoriteMarkets
                    val mapped = quotes.map { it.copy(isFavorite = favMarkets.contains(it.symbol)) }
                        .sortedByDescending { it.isFavorite }
                    _markets.value = mapped
                }

                // 6. Fetch Disaster & Seismic Alerts
                val alertList = alertsService.fetchVerifiedAlerts()
                _alerts.value = alertList

                // If disaster alerts notification is enabled and a critical alert is observed, notify
                if (_userPreferences.value.disasterAlertsNotificationEnabled && alertList.isNotEmpty()) {
                    val topAlert = alertList.firstOrNull { it.severity == "CRITICAL" || it.severity == "WARNING" }
                    topAlert?.let {
                        val lastNotifiedId = prefs.getString("last_notified_alert_id", "")
                        if (lastNotifiedId != it.id) {
                            MornixNotificationHelper.showDisasterNotification(context, it)
                            prefs.edit().putString("last_notified_alert_id", it.id).apply()
                        }
                    }
                }

                // 7. Synthesize Morning Briefing and Cache for Morning Notification
                val newBriefing = synthesizer.generateBriefing(
                    goldSilver = _bullionRates.value,
                    forexList = _forexRates.value,
                    weather = _weather.value,
                    articles = _news.value,
                    alerts = _alerts.value,
                    prefs = _userPreferences.value
                )
                _briefing.value = newBriefing
                saveCachedBriefing(newBriefing)

            } catch (_: Exception) {
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshDeviceLocation() {
        scope.launch {
            val loc = LocationHelper.getDeviceLocation(context)
            val displayCity = if (loc.cityName.isNotBlank() && loc.cityName != "Current Location") loc.cityName else "Current Location"
            val weatherResult = weatherService.fetchWeather(displayCity, loc.latitude, loc.longitude)
            if (weatherResult.isSuccess) {
                _weather.value = weatherResult.getOrNull()
            }
        }
    }

    fun toggleFavoriteCurrency(currencyCode: String) {
        val current = _userPreferences.value
        val list = current.favoriteCurrencies.toMutableList()
        if (list.contains(currencyCode)) {
            list.remove(currencyCode)
        } else {
            list.add(0, currencyCode) // Pin to start
        }
        val updated = current.copy(favoriteCurrencies = list)
        updatePreferences(updated)

        // Re-sort current forex list
        _forexRates.value = _forexRates.value.map {
            it.copy(isFavorite = list.contains(it.currencyCode))
        }.sortedByDescending { it.isFavorite }
    }

    fun toggleFavoriteMarket(symbol: String) {
        val current = _userPreferences.value
        val list = current.favoriteMarkets.toMutableList()
        if (list.contains(symbol)) {
            list.remove(symbol)
        } else {
            list.add(0, symbol) // Pin to start
        }
        val updated = current.copy(favoriteMarkets = list)
        updatePreferences(updated)

        // Re-sort current markets list
        _markets.value = _markets.value.map {
            it.copy(isFavorite = list.contains(it.symbol))
        }.sortedByDescending { it.isFavorite }
    }

    fun toggleDailyBibleVerse(enabled: Boolean) {
        val updated = _userPreferences.value.copy(dailyBibleVerseEnabled = enabled)
        updatePreferences(updated)
    }

    fun getTodayBibleVerse() = bibleVerseService.getDailyVerse()

    fun loadSavedBirthDate() {
        val type = prefs.getString("pref_dob_calendar_type", null)
        val y = prefs.getInt("pref_dob_year", 0)
        val m = prefs.getInt("pref_dob_month", 0)
        val d = prefs.getInt("pref_dob_day", 0)
        if (type != null && y > 0 && m > 0 && d > 0) {
            _savedBirthDate.value = SavedBirthDate(type, y, m, d)
        } else {
            _savedBirthDate.value = null
        }
    }

    fun saveBirthDate(calendarType: String, year: Int, month: Int, day: Int) {
        prefs.edit()
            .putString("pref_dob_calendar_type", calendarType)
            .putInt("pref_dob_year", year)
            .putInt("pref_dob_month", month)
            .putInt("pref_dob_day", day)
            .apply()
        _savedBirthDate.value = SavedBirthDate(calendarType, year, month, day)
    }

    fun clearBirthDate() {
        prefs.edit()
            .remove("pref_dob_calendar_type")
            .remove("pref_dob_year")
            .remove("pref_dob_month")
            .remove("pref_dob_day")
            .apply()
        _savedBirthDate.value = null
    }

    fun sendTestMorningBriefingNotification() {
        val currentBriefing = _briefing.value ?: run {
            MorningBriefing(
                generatedAtMillis = System.currentTimeMillis(),
                formattedDate = "Today",
                greeting = "Good Morning",
                executiveSummary = "Test notification: Mornix daily briefing is active with live bullion, forex, and news.",
                newsHighlights = emptyList(),
                goldForexSnippet = "Gold: 24K Live • USD/NPR verified.",
                weatherSnippet = "Current temperature available.",
                alertsSnippet = "No active hazards.",
                spokenScript = "Good morning! This is Mornix with your morning briefing. Tapping listen directly in this notification plays your spoken daily audio brief.",
                isAiGenerated = true
            )
        }
        MornixNotificationHelper.showMorningBriefingNotification(context, currentBriefing)
    }

    fun sendTestDisasterAlertNotification() {
        val alert = _alerts.value.firstOrNull() ?: DisasterAlert(
            id = "test_disaster_001",
            title = "Earthquake M4.8 - Central Region, Nepal",
            description = "Seismic tremor recorded. Monitored by National Seismological Centre / USGS. Exercise safety precautions.",
            affectedLocation = "Central Nepal / Himalayan Belt",
            severity = "WARNING",
            issuingAuthority = "National Seismological Centre / USGS",
            pubDateFormatted = "Just now",
            sourceUrl = "https://earthquake.usgs.gov",
            timestampMillis = System.currentTimeMillis()
        )
        MornixNotificationHelper.showDisasterNotification(context, alert)
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        scope.launch {
            _isChatLoading.value = true
            _chatMessages.value = chatService.history + ChatMessage(
                sender = com.example.data.service.MessageSender.USER,
                text = text
            )
            chatService.sendMessage(
                userMessage = text,
                currentWeather = _weather.value,
                currentBullion = _bullionRates.value,
                currentForex = _forexRates.value,
                currentNews = _news.value
            )
            _chatMessages.value = chatService.history
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        chatService.clearHistory()
        _chatMessages.value = chatService.history
    }

    fun toggleBookmark(articleId: String) {
        if (bookmarkedIds.contains(articleId)) {
            bookmarkedIds.remove(articleId)
        } else {
            bookmarkedIds.add(articleId)
        }
        saveBookmarkedIds()
        _news.value = _news.value.map {
            if (it.id == articleId) it.copy(isBookmarked = bookmarkedIds.contains(articleId)) else it
        }
    }

    fun updatePreferences(newPrefs: UserPreferences) {
        val oldPrefs = _userPreferences.value
        _userPreferences.value = newPrefs
        savePreferences(newPrefs)

        // Reschedule alarm if notification time or enabled status changed
        if (oldPrefs.morningBriefingNotificationEnabled != newPrefs.morningBriefingNotificationEnabled ||
            oldPrefs.morningBriefingHour != newPrefs.morningBriefingHour ||
            oldPrefs.morningBriefingMinute != newPrefs.morningBriefingMinute
        ) {
            if (newPrefs.morningBriefingNotificationEnabled) {
                MornixNotificationHelper.scheduleDailyBriefingAlarm(
                    context,
                    newPrefs.morningBriefingHour,
                    newPrefs.morningBriefingMinute
                )
            } else {
                MornixNotificationHelper.cancelDailyBriefingAlarm(context)
            }
        }

        // If city changed, refresh weather
        scope.launch {
            val weatherResult = if (newPrefs.preferredCity.equals("Current Location", ignoreCase = true)) {
                val loc = LocationHelper.getDeviceLocation(context)
                val displayCity = if (loc.cityName.isNotBlank() && loc.cityName != "Current Location") loc.cityName else "Current Location"
                weatherService.fetchWeather(displayCity, loc.latitude, loc.longitude)
            } else {
                val coords = weatherService.popularCities.find { it.name.equals(newPrefs.preferredCity, ignoreCase = true) }
                    ?: weatherService.popularCities.first()
                weatherService.fetchWeather(coords.name, coords.lat, coords.lon)
            }

            if (weatherResult.isSuccess) {
                _weather.value = weatherResult.getOrNull()
            }
            // Re-generate briefing with updated preferences
            val newBriefing = synthesizer.generateBriefing(
                goldSilver = _bullionRates.value,
                forexList = _forexRates.value,
                weather = _weather.value,
                articles = _news.value,
                alerts = _alerts.value,
                prefs = newPrefs
            )
            _briefing.value = newBriefing
            saveCachedBriefing(newBriefing)
        }
    }

    private fun loadPreferences(): UserPreferences {
        val city = prefs.getString("pref_city", "Current Location") ?: "Current Location"
        val country = prefs.getString("pref_country", "Nepal") ?: "Nepal"
        val tempUnit = if (prefs.getString("pref_temp", "C") == "F") TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
        val goldUnitStr = prefs.getString("pref_gold_unit", "TOLA")
        val goldUnit = try { GoldWeightUnit.valueOf(goldUnitStr ?: "TOLA") } catch (_: Exception) { GoldWeightUnit.TOLA }
        val speed = prefs.getFloat("pref_tts_speed", 1.0f)

        val favCurrenciesSet = prefs.getStringSet("pref_fav_currencies", null)
        val favCurrencies = favCurrenciesSet?.toList() ?: listOf("USD", "INR", "EUR", "AED", "GBP", "AUD")

        val favMarketsSet = prefs.getStringSet("pref_fav_markets", null)
        val favMarkets = favMarketsSet?.toList() ?: listOf("BTC", "ETH", "GC=F", "SI=F", "CL=F", "^GSPC")

        val morningNotifEnabled = prefs.getBoolean("pref_morning_notification_enabled", true)
        val morningHour = prefs.getInt("pref_morning_hour", 7)
        val morningMinute = prefs.getInt("pref_morning_minute", 0)
        val disasterNotifEnabled = prefs.getBoolean("pref_disaster_notification_enabled", true)
        val dailyVerseEnabled = prefs.getBoolean("pref_daily_bible_verse_enabled", false)

        return UserPreferences(
            preferredCity = city,
            preferredCountry = country,
            temperatureUnit = tempUnit,
            goldWeightUnit = goldUnit,
            favoriteCurrencies = favCurrencies,
            favoriteMarkets = favMarkets,
            ttsSpeed = speed,
            morningBriefingNotificationEnabled = morningNotifEnabled,
            morningBriefingHour = morningHour,
            morningBriefingMinute = morningMinute,
            disasterAlertsNotificationEnabled = disasterNotifEnabled,
            dailyBibleVerseEnabled = dailyVerseEnabled
        )
    }

    private fun savePreferences(p: UserPreferences) {
        prefs.edit()
            .putString("pref_city", p.preferredCity)
            .putString("pref_country", p.preferredCountry)
            .putString("pref_temp", if (p.temperatureUnit == TemperatureUnit.FAHRENHEIT) "F" else "C")
            .putString("pref_gold_unit", p.goldWeightUnit.name)
            .putFloat("pref_tts_speed", p.ttsSpeed)
            .putStringSet("pref_fav_currencies", p.favoriteCurrencies.toSet())
            .putStringSet("pref_fav_markets", p.favoriteMarkets.toSet())
            .putBoolean("pref_morning_notification_enabled", p.morningBriefingNotificationEnabled)
            .putInt("pref_morning_hour", p.morningBriefingHour)
            .putInt("pref_morning_minute", p.morningBriefingMinute)
            .putBoolean("pref_disaster_notification_enabled", p.disasterAlertsNotificationEnabled)
            .putBoolean("pref_daily_bible_verse_enabled", p.dailyBibleVerseEnabled)
            .apply()
    }

    private fun loadBookmarkedIds() {
        val set = prefs.getStringSet("bookmarked_ids", emptySet()) ?: emptySet()
        bookmarkedIds.addAll(set)
    }

    private fun saveBookmarkedIds() {
        prefs.edit().putStringSet("bookmarked_ids", bookmarkedIds).apply()
    }

    private fun saveCachedBriefing(b: MorningBriefing) {
        try {
            val json = JSONObject().apply {
                put("generatedAtMillis", b.generatedAtMillis)
                put("formattedDate", b.formattedDate)
                put("greeting", b.greeting)
                put("executiveSummary", b.executiveSummary)
                put("goldForexSnippet", b.goldForexSnippet)
                put("weatherSnippet", b.weatherSnippet)
                put("alertsSnippet", b.alertsSnippet)
                put("spokenScript", b.spokenScript)
                b.bibleVerse?.let { v ->
                    val vObj = JSONObject().apply {
                        put("reference", v.reference)
                        put("text", v.text)
                        put("translation", v.translation)
                        put("theme", v.theme)
                        put("dateFormatted", v.dateFormatted)
                    }
                    put("bibleVerse", vObj)
                }
            }
            prefs.edit().putString("cached_briefing_json", json.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun saveCachedBullion(rates: GoldSilverRates) {
        try {
            val json = JSONObject().apply {
                put("fineGold10g", rates.fineGold10g)
                put("fineGoldTola", rates.fineGoldTola)
                put("tejabiGold10g", rates.tejabiGold10g)
                put("tejabiGoldTola", rates.tejabiGoldTola)
                put("silver10g", rates.silver10g)
                put("silverTola", rates.silverTola)
                put("publishedDate", rates.publishedDate)
                put("publishedTime", rates.publishedTime)
                put("yesterdayFineGoldTola", rates.yesterdayFineGoldTola)
                put("yesterdaySilverTola", rates.yesterdaySilverTola)
                put("changeGoldTola", rates.changeGoldTola)
                put("changeGoldTolaPercent", rates.changeGoldTolaPercent)
                put("changeSilverTola", rates.changeSilverTola)
                put("changeSilverTolaPercent", rates.changeSilverTolaPercent)
                put("retrievedAtMillis", rates.retrievedAtMillis)
            }
            prefs.edit().putString("cached_bullion_json", json.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun saveCachedForex(list: List<ForexRateItem>) {
        try {
            val arr = JSONArray()
            for (item in list) {
                arr.put(JSONObject().apply {
                    put("code", item.currencyCode)
                    put("name", item.currencyName)
                    put("flag", item.flagEmoji)
                    put("unit", item.unit)
                    put("buy", item.buyRate)
                    put("sell", item.sellRate)
                    put("date", item.date)
                    put("pub", item.publishedOn)
                    put("isFav", item.isFavorite)
                })
            }
            prefs.edit().putString("cached_forex_json", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun saveCachedWeather(w: WeatherInfo) {
        try {
            val json = JSONObject().apply {
                put("cityName", w.cityName)
                put("currentTempC", w.currentTempC)
                put("feelsLikeC", w.feelsLikeC)
                put("humidityPercent", w.humidityPercent)
                put("windSpeedKmh", w.windSpeedKmh)
                put("windDirection", w.windDirection)
                put("precipitationMm", w.precipitationMm)
                put("rainProbability", w.rainProbability)
                put("uvIndex", w.uvIndex)
                put("aqi", w.aqi)
                put("aqiStatus", w.aqiStatus)
                put("weatherCode", w.weatherCode)
                put("conditionDescription", w.conditionDescription)
                put("sunriseTime", w.sunriseTime)
                put("sunsetTime", w.sunsetTime)
            }
            prefs.edit().putString("cached_weather_json", json.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadCachedData() {
        try {
            val bullionStr = prefs.getString("cached_bullion_json", null)
            if (bullionStr != null) {
                val j = JSONObject(bullionStr)
                _bullionRates.value = GoldSilverRates(
                    fineGold10g = j.optDouble("fineGold10g"),
                    fineGoldTola = j.optDouble("fineGoldTola"),
                    tejabiGold10g = j.optDouble("tejabiGold10g"),
                    tejabiGoldTola = j.optDouble("tejabiGoldTola"),
                    silver10g = j.optDouble("silver10g"),
                    silverTola = j.optDouble("silverTola"),
                    publishedDate = j.optString("publishedDate"),
                    publishedTime = j.optString("publishedTime"),
                    yesterdayFineGoldTola = j.optDouble("yesterdayFineGoldTola"),
                    yesterdaySilverTola = j.optDouble("yesterdaySilverTola"),
                    changeGoldTola = j.optDouble("changeGoldTola"),
                    changeGoldTolaPercent = j.optDouble("changeGoldTolaPercent"),
                    changeSilverTola = j.optDouble("changeSilverTola"),
                    changeSilverTolaPercent = j.optDouble("changeSilverTolaPercent"),
                    freshnessStatus = FreshnessStatus.STALE,
                    retrievedAtMillis = j.optLong("retrievedAtMillis")
                )
            }
        } catch (_: Exception) {}
    }
}
