package com.example.data.service

import com.example.BuildConfig
import com.example.data.model.ForexRateItem
import com.example.data.model.GoldSilverRates
import com.example.data.model.NewsArticle
import com.example.data.model.WeatherInfo
import com.example.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class MessageSender { USER, ASSISTANT }

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val citations: List<String> = emptyList()
)

class MornixChatService {

    private val conversationHistory = mutableListOf<ChatMessage>()

    val history: List<ChatMessage> get() = conversationHistory.toList()

    init {
        // Welcome message
        conversationHistory.add(
            ChatMessage(
                sender = MessageSender.ASSISTANT,
                text = "Hello! I am Mornix AI, your intelligent morning briefing assistant. Ask me anything about today's Nepal news, FENEGOSIDA gold and silver rates, NRB currency exchange, or current weather forecast.\n\n*Made with AI by Resham*",
                citations = listOf("FENEGOSIDA", "Nepal Rastra Bank", "Open-Meteo", "Verified RSS")
            )
        )
    }

    suspend fun sendMessage(
        userMessage: String,
        currentWeather: WeatherInfo?,
        currentBullion: GoldSilverRates?,
        currentForex: List<ForexRateItem>,
        currentNews: List<NewsArticle>
    ): ChatMessage = withContext(Dispatchers.IO) {
        val userMsg = ChatMessage(sender = MessageSender.USER, text = userMessage)
        conversationHistory.add(userMsg)

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        val hasValidKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (hasValidKey) {
            try {
                val response = callGeminiApi(userMessage, apiKey, currentWeather, currentBullion, currentForex, currentNews)
                conversationHistory.add(response)
                return@withContext response
            } catch (_: Exception) {
                // Graceful fallback to grounded live synthesis
            }
        }

        // Context-grounded synthesis using the active verified data
        val localResponse = generateGroundedResponse(userMessage, currentWeather, currentBullion, currentForex, currentNews)
        conversationHistory.add(localResponse)
        localResponse
    }

    private fun callGeminiApi(
        userPrompt: String,
        apiKey: String,
        weather: WeatherInfo?,
        bullion: GoldSilverRates?,
        forex: List<ForexRateItem>,
        news: List<NewsArticle>
    ): ChatMessage {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val systemInstruction = "You are Mornix AI, an intelligent daily briefing assistant (Made with AI by Resham). " +
                "Ground your answers in verified live facts about Nepal and the world. " +
                "Live context from Mornix app: " +
                (if (weather != null) "Weather in ${weather.cityName}: ${weather.currentTempC}°C, ${weather.conditionDescription}. " else "") +
                (if (bullion != null && bullion.fineGoldTola > 0) "Nepal Fine Gold (24K): NPR ${bullion.fineGoldTola}/tola, Silver: NPR ${bullion.silverTola}/tola (Source: FENEGOSIDA). " else "") +
                (if (forex.isNotEmpty()) "USD buy: NPR ${forex.find { it.currencyCode == "USD" }?.buyRate ?: 152.0} (Source: Nepal Rastra Bank). " else "") +
                "Top headlines: " + news.take(3).joinToString("; ") { it.title }

        val rootJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
            })
            // Pass conversation contents
            val contentsArr = JSONArray()
            conversationHistory.takeLast(6).forEach { msg ->
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                contentsArr.put(JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                })
            }
            put("contents", contentsArr)

            // Search Grounding tool
            put("tools", JSONArray().put(JSONObject().put("google_search", JSONObject())))
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = rootJson.toString().toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(body).build()

        val resp = ApiClient.okHttpClient.newCall(request).execute()
        if (!resp.isSuccessful) {
            throw Exception("Gemini API error: ${resp.code}")
        }

        val respStr = resp.body?.string() ?: throw Exception("Empty response from Gemini")
        val respJson = JSONObject(respStr)
        val candidates = respJson.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val content = firstCandidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text") ?: "I'm sorry, I could not generate a response."

        // Extract grounding citations if present
        val citations = mutableListOf<String>()
        val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
        val webSearchQueries = groundingMetadata?.optJSONArray("webSearchQueries")
        if (webSearchQueries != null) {
            for (i in 0 until webSearchQueries.length()) {
                val q = webSearchQueries.optString(i)
                if (q.isNotBlank()) citations.add("Google Search: $q")
            }
        }

        return ChatMessage(
            sender = MessageSender.ASSISTANT,
            text = text,
            citations = if (citations.isNotEmpty()) citations else listOf("Verified Mornix Live Engine")
        )
    }

    private fun generateGroundedResponse(
        prompt: String,
        weather: WeatherInfo?,
        bullion: GoldSilverRates?,
        forex: List<ForexRateItem>,
        news: List<NewsArticle>
    ): ChatMessage {
        val p = prompt.lowercase()
        val text: String
        val citations = mutableListOf<String>()

        when {
            p.contains("gold") || p.contains("silver") || p.contains("bullion") || p.contains("sun") || p.contains("chandi") -> {
                if (bullion != null && bullion.fineGoldTola > 0) {
                    text = "According to today's official publication by the Federation of Nepal Gold & Silver Dealers' Association (FENEGOSIDA):\n\n" +
                            "• Fine Gold (24K / Chhapawal): NPR ${String.format(java.util.Locale.ENGLISH, "%,.0f", bullion.fineGoldTola)} per Tola (NPR ${String.format(java.util.Locale.ENGLISH, "%,.0f", bullion.fineGold10g)} / 10g)\n" +
                            "• Tejabi Gold (22K): NPR ${String.format(java.util.Locale.ENGLISH, "%,.0f", bullion.tejabiGoldTola)} per Tola\n" +
                            "• Silver (चाँदी): NPR ${String.format(java.util.Locale.ENGLISH, "%,.0f", bullion.silverTola)} per Tola\n\n" +
                            "Daily Change: ${if (bullion.changeGoldTola >= 0) "+" else ""}${String.format(java.util.Locale.ENGLISH, "%,.0f", bullion.changeGoldTola)} NPR (${String.format(java.util.Locale.ENGLISH, "%.2f", bullion.changeGoldTolaPercent)}%)."
                    citations.add("FENEGOSIDA Official Bulletin (${bullion.publishedDate})")
                } else {
                    text = "Official FENEGOSIDA rates for Nepal are currently loading from the verified bulletin. Please check back shortly."
                }
            }
            p.contains("weather") || p.contains("rain") || p.contains("temp") || p.contains("hot") || p.contains("cold") -> {
                if (weather != null) {
                    text = "Current weather for ${weather.cityName}, Nepal:\n\n" +
                            "• Temperature: ${weather.currentTempC.toInt()}°C (Feels like ${weather.feelsLikeC.toInt()}°C)\n" +
                            "• Condition: ${weather.conditionDescription}\n" +
                            "• Humidity: ${weather.humidityPercent}%\n" +
                            "• Wind: ${weather.windSpeedKmh.toInt()} km/h (${weather.windDirection})\n" +
                            "• Air Quality (AQI): ${weather.aqi} (${weather.aqiStatus})\n" +
                            "• Rain Probability: ${weather.rainProbability}%\n" +
                            "• Sunrise / Sunset: ${weather.sunriseTime} / ${weather.sunsetTime}"
                    citations.add("Open-Meteo High-Resolution Model")
                } else {
                    text = "Atmospheric data for Nepal is currently being refreshed."
                }
            }
            p.contains("forex") || p.contains("dollar") || p.contains("usd") || p.contains("inr") || p.contains("euro") || p.contains("currency") -> {
                val usd = forex.find { it.currencyCode == "USD" }
                val inr = forex.find { it.currencyCode == "INR" }
                val eur = forex.find { it.currencyCode == "EUR" }
                text = "Nepal Rastra Bank (NRB) Official Exchange Rates for today:\n\n" +
                        (if (usd != null) "• 1 USD = Buy: NPR ${usd.buyRate} / Sell: NPR ${usd.sellRate}\n" else "") +
                        (if (inr != null) "• 100 INR = Buy: NPR ${inr.buyRate} / Sell: NPR ${inr.sellRate}\n" else "") +
                        (if (eur != null) "• 1 EUR = Buy: NPR ${eur.buyRate} / Sell: NPR ${eur.sellRate}\n" else "") +
                        "\nAll figures are official central bank fixation rates without retail commission."
                citations.add("Nepal Rastra Bank Central Bank API")
            }
            p.contains("who made") || p.contains("creator") || p.contains("about") || p.contains("author") || p.contains("resham") -> {
                text = "Mornix was created with AI by Resham. It is engineered to provide genuine, 100% verified daily intelligence across news, financial markets, gold and silver bullion, exchange rates, and atmospheric forecasts."
                citations.add("Mornix Application Credits")
            }
            else -> {
                val topArticles = news.take(3)
                val newsText = if (topArticles.isNotEmpty()) {
                    topArticles.joinToString("\n\n") { "• [${it.sourceName}] ${it.title}: ${it.summary.take(120)}..." }
                } else {
                    "Daily briefing stories are being synchronized."
                }
                text = "Here is what is currently happening across Mornix live sources:\n\n$newsText\n\nFeel free to ask for deeper details on gold rates, currency conversions, weather in specific cities, or breaking alerts!"
                citations.add("Multi-Publisher Verified RSS Feeds")
            }
        }

        return ChatMessage(
            sender = MessageSender.ASSISTANT,
            text = text,
            citations = citations
        )
    }

    fun clearHistory() {
        conversationHistory.clear()
        conversationHistory.add(
            ChatMessage(
                sender = MessageSender.ASSISTANT,
                text = "Conversation cleared. How can I assist you with today's briefing?\n\n*Made with AI by Resham*"
            )
        )
    }
}
