package com.example.data.remote

import com.example.data.model.FreshnessStatus
import com.example.data.model.MarketQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

class MarketsService {

    data class YahooSymbolConfig(
        val symbol: String,
        val displayName: String,
        val category: String,
        val currencySymbol: String
    )

    private val yahooSymbols = listOf(
        YahooSymbolConfig("GC=F", "Gold Spot (COMEX)", "Commodity", "$"),
        YahooSymbolConfig("SI=F", "Silver (COMEX)", "Commodity", "$"),
        YahooSymbolConfig("PL=F", "Platinum", "Commodity", "$"),
        YahooSymbolConfig("CL=F", "Crude Oil (WTI)", "Commodity", "$"),
        YahooSymbolConfig("^GSPC", "S&P 500 Index", "Index", "pts "),
        YahooSymbolConfig("EURUSD=X", "EUR / USD", "Forex", "$"),
        YahooSymbolConfig("USDJPY=X", "USD / JPY", "Forex", "¥")
    )

    suspend fun fetchAllMarkets(): List<MarketQuote> = withContext(Dispatchers.IO) {
        val quotes = mutableListOf<MarketQuote>()

        // 1. Fetch Crypto from CoinGecko
        val cryptoDeferred = async { fetchCryptoQuotes() }

        // 2. Fetch Yahoo Finance symbols in parallel
        val yahooDeferredList = yahooSymbols.map { cfg ->
            async { fetchYahooQuote(cfg) }
        }

        quotes.addAll(cryptoDeferred.await())
        yahooDeferredList.awaitAll().filterNotNull().forEach { quotes.add(it) }

        quotes
    }

    private suspend fun fetchCryptoQuotes(): List<MarketQuote> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,solana&vs_currencies=usd&include_24hr_change=true"
            val request = Request.Builder().url(url).build()
            val response = ApiClient.okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            val list = mutableListOf<MarketQuote>()

            val configs = listOf(
                Triple("bitcoin", "Bitcoin (BTC)", "Crypto"),
                Triple("ethereum", "Ethereum (ETH)", "Crypto"),
                Triple("solana", "Solana (SOL)", "Crypto")
            )

            for ((id, name, cat) in configs) {
                val obj = json.optJSONObject(id)
                if (obj != null) {
                    val price = obj.optDouble("usd", 0.0)
                    val changePercent = obj.optDouble("usd_24h_change", 0.0)
                    val change24h = price * (changePercent / 100.0)

                    list.add(
                        MarketQuote(
                            symbol = id.uppercase(),
                            name = name,
                            category = cat,
                            price = price,
                            change24h = change24h,
                            changePercent = changePercent,
                            currencySymbol = "$",
                            historyPoints = listOf(
                                price - (change24h * 0.9),
                                price - (change24h * 0.7),
                                price - (change24h * 0.4),
                                price - (change24h * 0.2),
                                price
                            ),
                            marketStatus = "24/7 Live",
                            source = "CoinGecko Real-Time",
                            sourceUrl = "https://www.coingecko.com",
                            freshnessStatus = FreshnessStatus.LIVE,
                            lastUpdatedMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchYahooQuote(cfg: YahooSymbolConfig): MarketQuote? = withContext(Dispatchers.IO) {
        try {
            val url = "https://query1.finance.yahoo.com/v8/finance/chart/${cfg.symbol}?interval=1d&range=5d"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()

            val response = ApiClient.okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val chart = json.getJSONObject("chart")
            val resultArr = chart.getJSONArray("result")
            if (resultArr.length() == 0) return@withContext null

            val result = resultArr.getJSONObject(0)
            val meta = result.getJSONObject("meta")
            val price = meta.optDouble("regularMarketPrice", 0.0)
            val prevClose = meta.optDouble("chartPreviousClose", price)
            val change = price - prevClose
            val changePercent = if (prevClose > 0) (change / prevClose) * 100.0 else 0.0

            // Historical closes
            val historyList = mutableListOf<Double>()
            val indicators = result.optJSONObject("indicators")
            val quotes = indicators?.optJSONArray("quote")
            if (quotes != null && quotes.length() > 0) {
                val qObj = quotes.getJSONObject(0)
                val closes = qObj.optJSONArray("close")
                if (closes != null) {
                    for (i in 0 until closes.length()) {
                        val c = closes.optDouble(i, Double.NaN)
                        if (!c.isNaN()) {
                            historyList.add(c)
                        }
                    }
                }
            }

            MarketQuote(
                symbol = cfg.symbol,
                name = cfg.displayName,
                category = cfg.category,
                price = price,
                change24h = change,
                changePercent = changePercent,
                currencySymbol = cfg.currencySymbol,
                historyPoints = if (historyList.isNotEmpty()) historyList else listOf(prevClose, price),
                marketStatus = "Market Session",
                source = "Global Exchange Data",
                sourceUrl = "https://finance.yahoo.com",
                freshnessStatus = FreshnessStatus.LIVE,
                lastUpdatedMillis = System.currentTimeMillis()
            )
        } catch (_: Exception) {
            null
        }
    }
}
