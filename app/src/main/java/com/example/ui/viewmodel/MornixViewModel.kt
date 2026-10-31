package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.calendar.SavedBirthDate
import com.example.data.model.DisasterAlert
import com.example.data.model.ForexRateItem
import com.example.data.model.GoldSilverRates
import com.example.data.model.GoldWeightUnit
import com.example.data.model.MarketQuote
import com.example.data.model.MorningBriefing
import com.example.data.model.NewsArticle
import com.example.data.model.TemperatureUnit
import com.example.data.model.UserPreferences
import com.example.data.model.WeatherInfo
import com.example.data.repository.MornixRepository
import com.example.data.service.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MornixViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MornixRepository(application)
    val ttsManager = TtsManager(application)

    val bullionRates: StateFlow<GoldSilverRates?> = repository.bullionRates
    val forexRates: StateFlow<List<ForexRateItem>> = repository.forexRates
    val weather: StateFlow<WeatherInfo?> = repository.weather
    val allNews: StateFlow<List<NewsArticle>> = repository.news
    val markets: StateFlow<List<MarketQuote>> = repository.markets
    val alerts: StateFlow<List<DisasterAlert>> = repository.alerts
    val briefing: StateFlow<MorningBriefing?> = repository.briefing
    val userPreferences: StateFlow<UserPreferences> = repository.userPreferences
    val isRefreshing: StateFlow<Boolean> = repository.isRefreshing

    val isPlayingTts: StateFlow<Boolean> = ttsManager.isPlaying
    val isPausedTts: StateFlow<Boolean> = ttsManager.isPaused
    val speechRate: StateFlow<Float> = ttsManager.speechRate

    val chatMessages: StateFlow<List<com.example.data.service.ChatMessage>> = repository.chatMessages
    val isChatLoading: StateFlow<Boolean> = repository.isChatLoading

    // Navigation State
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // News Filters
    private val _selectedNewsCategory = MutableStateFlow("All")
    val selectedNewsCategory: StateFlow<String> = _selectedNewsCategory.asStateFlow()

    private val _newsSearchQuery = MutableStateFlow("")
    val newsSearchQuery: StateFlow<String> = _newsSearchQuery.asStateFlow()

    private val _showOnlyBookmarked = MutableStateFlow(false)
    val showOnlyBookmarked: StateFlow<Boolean> = _showOnlyBookmarked.asStateFlow()

    // Filtered News flow
    val filteredNews: StateFlow<List<NewsArticle>> = combine(
        allNews,
        selectedNewsCategory,
        newsSearchQuery,
        showOnlyBookmarked
    ) { news, cat, query, bookmarkedOnly ->
        var list = news
        if (bookmarkedOnly) {
            list = list.filter { it.isBookmarked }
        }
        if (cat != "All") {
            list = list.filter { it.category.equals(cat, ignoreCase = true) }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) || it.summary.lowercase().contains(q) || it.sourceName.lowercase().contains(q)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Markets Navigation Sub-tab
    private val _selectedMarketsSubTab = MutableStateFlow(0)
    val selectedMarketsSubTab: StateFlow<Int> = _selectedMarketsSubTab.asStateFlow()

    // Calendar Navigation Sub-tab (0 = Calendar View, 1 = Date Converter, 2 = Age Calculator)
    private val _selectedCalendarSubTab = MutableStateFlow(0)
    val selectedCalendarSubTab: StateFlow<Int> = _selectedCalendarSubTab.asStateFlow()

    val savedBirthDate: StateFlow<SavedBirthDate?> = repository.savedBirthDate

    // Calculator State
    private val _calcWeight = MutableStateFlow("1")
    val calcWeight: StateFlow<String> = _calcWeight.asStateFlow()

    private val _calcUnit = MutableStateFlow(GoldWeightUnit.TOLA)
    val calcUnit: StateFlow<GoldWeightUnit> = _calcUnit.asStateFlow()

    private val _calcPurity = MutableStateFlow("24K")
    val calcPurity: StateFlow<String> = _calcPurity.asStateFlow()

    private val _calcMetal = MutableStateFlow("Gold") // "Gold" or "Silver"
    val calcMetal: StateFlow<String> = _calcMetal.asStateFlow()

    // Converter State
    private val _convSourceCurrency = MutableStateFlow("USD")
    val convSourceCurrency: StateFlow<String> = _convSourceCurrency.asStateFlow()

    private val _convTargetCurrency = MutableStateFlow("NPR")
    val convTargetCurrency: StateFlow<String> = _convTargetCurrency.asStateFlow()

    private val _convAmount = MutableStateFlow("100")
    val convAmount: StateFlow<String> = _convAmount.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun selectMarketsSubTab(index: Int) {
        _selectedMarketsSubTab.value = index
    }

    fun selectCalendarSubTab(index: Int) {
        _selectedCalendarSubTab.value = index
    }

    fun saveBirthDate(calendarType: String, year: Int, month: Int, day: Int) {
        repository.saveBirthDate(calendarType, year, month, day)
    }

    fun clearBirthDate() {
        repository.clearBirthDate()
    }

    fun selectNewsCategory(category: String) {
        _selectedNewsCategory.value = category
    }

    fun searchNews(query: String) {
        _newsSearchQuery.value = query
    }

    fun toggleShowBookmarkedOnly() {
        _showOnlyBookmarked.value = !_showOnlyBookmarked.value
    }

    fun toggleBookmark(articleId: String) {
        repository.toggleBookmark(articleId)
    }

    fun refreshAll() {
        repository.refreshAllData()
    }

    // TTS Actions
    fun playBriefing() {
        briefing.value?.let { b ->
            ttsManager.play(b.spokenScript)
        }
    }

    fun pauseBriefing() {
        ttsManager.pause()
    }

    fun resumeBriefing() {
        ttsManager.resume()
    }

    fun stopBriefing() {
        ttsManager.stop()
    }

    fun setTtsSpeed(speed: Float) {
        ttsManager.setSpeechRate(speed)
        val current = userPreferences.value
        repository.updatePreferences(current.copy(ttsSpeed = speed))
    }

    // Calculator Actions
    fun setCalcWeight(weight: String) { _calcWeight.value = weight }
    fun setCalcUnit(unit: GoldWeightUnit) { _calcUnit.value = unit }
    fun setCalcPurity(purity: String) { _calcPurity.value = purity }
    fun setCalcMetal(metal: String) { _calcMetal.value = metal }

    // Currency Converter Actions
    fun setConvSource(code: String) { _convSourceCurrency.value = code }
    fun setConvTarget(code: String) { _convTargetCurrency.value = code }
    fun setConvAmount(amount: String) { _convAmount.value = amount }
    fun swapCurrencies() {
        val s = _convSourceCurrency.value
        val t = _convTargetCurrency.value
        _convSourceCurrency.value = t
        _convTargetCurrency.value = s
    }

    // Preferences Actions
    fun setPreferredCity(city: String) {
        val current = userPreferences.value
        repository.updatePreferences(current.copy(preferredCity = city))
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        val current = userPreferences.value
        repository.updatePreferences(current.copy(temperatureUnit = unit))
    }

    fun setGoldWeightUnit(unit: GoldWeightUnit) {
        val current = userPreferences.value
        repository.updatePreferences(current.copy(goldWeightUnit = unit))
    }

    fun toggleNewsInterest(category: String) {
        val current = userPreferences.value
        val list = current.selectedNewsCategories.toMutableList()
        if (list.contains(category)) {
            if (list.size > 1) list.remove(category)
        } else {
            list.add(category)
        }
        repository.updatePreferences(current.copy(selectedNewsCategories = list))
    }

    fun toggleWidget(widgetId: String) {
        val current = userPreferences.value
        val list = current.enabledWidgets.toMutableList()
        if (list.contains(widgetId)) {
            if (list.size > 1) list.remove(widgetId)
        } else {
            list.add(widgetId)
        }
        repository.updatePreferences(current.copy(enabledWidgets = list))
    }

    fun sendChatMessage(text: String) {
        repository.sendChatMessage(text)
    }

    fun clearChat() {
        repository.clearChat()
    }

    fun refreshDeviceLocation() {
        repository.refreshDeviceLocation()
    }

    fun toggleFavoriteCurrency(code: String) {
        repository.toggleFavoriteCurrency(code)
    }

    fun toggleFavoriteMarket(symbol: String) {
        repository.toggleFavoriteMarket(symbol)
    }

    fun setMorningNotificationTime(hour: Int, minute: Int) {
        val current = userPreferences.value
        repository.updatePreferences(current.copy(morningBriefingHour = hour, morningBriefingMinute = minute))
    }

    fun toggleMorningNotification(enabled: Boolean) {
        val current = userPreferences.value
        repository.updatePreferences(current.copy(morningBriefingNotificationEnabled = enabled))
    }

    fun toggleDisasterNotification(enabled: Boolean) {
        val current = userPreferences.value
        repository.updatePreferences(current.copy(disasterAlertsNotificationEnabled = enabled))
    }

    fun sendTestMorningBriefingNotification() {
        repository.sendTestMorningBriefingNotification()
    }

    fun sendTestDisasterAlertNotification() {
        repository.sendTestDisasterAlertNotification()
    }

    fun toggleDailyBibleVerse(enabled: Boolean) {
        repository.toggleDailyBibleVerse(enabled)
    }

    fun getTodayBibleVerse() = repository.getTodayBibleVerse()

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
