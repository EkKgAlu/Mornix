package com.example.data.remote

import com.example.data.model.ForexRateItem
import com.example.data.model.FreshnessStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray

class NepalForexService {

    suspend fun fetchOfficialForexRates(): Result<List<ForexRateItem>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://www.nrb.org.np/api/forex/v1/app-rate")
                .build()

            val response = ApiClient.okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code} from Nepal Rastra Bank"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body from NRB"))
            val jsonArray = JSONArray(body)
            val list = mutableListOf<ForexRateItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val code = obj.optString("iso3", "").uppercase().trim()
                val name = obj.optString("name", "")
                val unit = obj.optInt("unit", 1)
                val buy = obj.optDouble("buy", 0.0)
                val sell = obj.optDouble("sell", 0.0)
                val date = obj.optString("date", "")
                val publishedOn = obj.optString("published_on", "")

                if (code.isNotBlank() && (buy > 0.0 || sell > 0.0)) {
                    val flag = getCurrencyFlag(code)
                    list.add(
                        ForexRateItem(
                            currencyCode = code,
                            currencyName = name,
                            flagEmoji = flag,
                            unit = unit,
                            buyRate = buy,
                            sellRate = sell,
                            date = date,
                            publishedOn = publishedOn,
                            freshnessStatus = FreshnessStatus.LIVE
                        )
                    )
                }
            }

            if (list.isEmpty()) {
                Result.failure(Exception("No forex rate records found in NRB response"))
            } else {
                Result.success(list)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getCurrencyFlag(code: String): String {
        return when (code) {
            "USD" -> "🇺🇸"
            "INR" -> "🇮🇳"
            "EUR" -> "🇪🇺"
            "GBP" -> "🇬🇧"
            "AUD" -> "🇦🇺"
            "CAD" -> "🇨🇦"
            "JPY" -> "🇯🇵"
            "CHF" -> "🇨🇭"
            "CNY" -> "🇨🇳"
            "AED" -> "🇦🇪"
            "SAR" -> "🇸🇦"
            "QAR" -> "🇶🇦"
            "SGD" -> "🇸🇬"
            "MYR" -> "🇲🇾"
            "KRW" -> "🇰🇷"
            "THB" -> "🇹🇭"
            "KWD" -> "🇰🇼"
            "BHD" -> "🇧🇭"
            "OMR" -> "🇴🇲"
            "SEK" -> "🇸🇪"
            "DKK" -> "🇩🇰"
            "HKD" -> "🇭🇰"
            "NZD" -> "🇳🇿"
            "NPR" -> "🇳🇵"
            else -> "🌐"
        }
    }
}
