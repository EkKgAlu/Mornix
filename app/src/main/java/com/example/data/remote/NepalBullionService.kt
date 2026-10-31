package com.example.data.remote

import com.example.data.model.FreshnessStatus
import com.example.data.model.GoldSilverRates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NepalBullionService {

    suspend fun fetchOfficialBullionRates(): Result<GoldSilverRates> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.fenegosida.org/api/website/v1/Dashboard/today")
                .build()

            val response = ApiClient.okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code} from FENEGOSIDA"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body from FENEGOSIDA"))
            val jsonArray = JSONArray(body)

            var fineGold10g = 0.0
            var fineGoldTola = 0.0
            var silver10g = 0.0
            var silverTola = 0.0
            var yesterdayFineGoldTola = 0.0
            var yesterdaySilverTola = 0.0
            var todayDateRaw = ""

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val rateType = item.optString("rateType", "")
                val todayRate = item.optDouble("todayBaseRatePerGram", 0.0)
                val yesterdayRate = item.optDouble("yestardayBaseRatePerGram", 0.0)
                val dateStr = item.optString("todayDate", "")
                if (dateStr.isNotBlank() && todayDateRaw.isBlank()) {
                    todayDateRaw = dateStr
                }

                when {
                    rateType.contains("सुन") && rateType.contains("१० ग्राम") -> {
                        fineGold10g = todayRate
                    }
                    rateType.contains("सुन") && (rateType.contains("१ तोला") || rateType.contains("तोला")) -> {
                        fineGoldTola = todayRate
                        yesterdayFineGoldTola = yesterdayRate
                    }
                    rateType.contains("चाँदी") && rateType.contains("१० ग्राम") -> {
                        silver10g = todayRate
                    }
                    rateType.contains("चाँदी") && (rateType.contains("१ तोला") || rateType.contains("तोला")) -> {
                        silverTola = todayRate
                        yesterdaySilverTola = yesterdayRate
                    }
                }
            }

            if (fineGoldTola <= 0.0 && fineGold10g > 0.0) {
                // 1 tola = 11.6638 grams
                fineGoldTola = (fineGold10g / 10.0) * 11.6638
            }

            // Tejabi gold is officially slightly below Chhapawal (typically 99.5% or 500 NPR less per tola in Nepal market)
            val tejabiGoldTola = if (fineGoldTola > 0.0) fineGoldTola * (22.0 / 24.0) else 0.0
            val tejabiGold10g = if (fineGold10g > 0.0) fineGold10g * (22.0 / 24.0) else 0.0

            val changeGoldTola = if (yesterdayFineGoldTola > 0.0) fineGoldTola - yesterdayFineGoldTola else 0.0
            val changeGoldTolaPercent = if (yesterdayFineGoldTola > 0.0) (changeGoldTola / yesterdayFineGoldTola) * 100.0 else 0.0

            val changeSilverTola = if (yesterdaySilverTola > 0.0) silverTola - yesterdaySilverTola else 0.0
            val changeSilverTolaPercent = if (yesterdaySilverTola > 0.0) (changeSilverTola / yesterdaySilverTola) * 100.0 else 0.0

            val (pubDate, pubTime) = formatIsoDate(todayDateRaw)

            val rates = GoldSilverRates(
                fineGold10g = fineGold10g,
                fineGoldTola = fineGoldTola,
                tejabiGold10g = tejabiGold10g,
                tejabiGoldTola = tejabiGoldTola,
                silver10g = silver10g,
                silverTola = silverTola,
                publishedDate = pubDate,
                publishedTime = pubTime,
                yesterdayFineGoldTola = yesterdayFineGoldTola,
                yesterdaySilverTola = yesterdaySilverTola,
                changeGoldTola = changeGoldTola,
                changeGoldTolaPercent = changeGoldTolaPercent,
                changeSilverTola = changeSilverTola,
                changeSilverTolaPercent = changeSilverTolaPercent,
                sourceName = "Federation of Nepal Gold & Silver Dealers' Association (FENEGOSIDA)",
                sourceUrl = "https://fenegosida.org",
                freshnessStatus = FreshnessStatus.LIVE,
                retrievedAtMillis = System.currentTimeMillis()
            )

            Result.success(rates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatIsoDate(isoString: String): Pair<String, String> {
        return try {
            if (isoString.isBlank()) {
                val now = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
                Pair(now, "Published Today")
            } else {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(isoString.substringBefore(".")) ?: Date()
                val outDate = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(date)
                val outTime = SimpleDateFormat("h:mm a (Nepal Time)", Locale.ENGLISH).format(date)
                Pair(outDate, outTime)
            }
        } catch (e: Exception) {
            Pair("Today's Rate", "FENEGOSIDA Morning Bulletin")
        }
    }
}
