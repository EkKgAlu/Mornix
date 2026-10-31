package com.example.data.remote

import android.util.Xml
import com.example.data.model.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NewsRssService {

    data class FeedSource(
        val publisherName: String,
        val url: String,
        val defaultCategory: String
    )

    private val feedSources = listOf(
        // Nepal Local Sources
        FeedSource("OnlineKhabar English", "https://english.onlinekhabar.com/feed", "Nepal News"),
        FeedSource("Nepali Times", "https://nepalitimes.com/feed", "Nepal News"),
        FeedSource("The Kathmandu Post", "https://kathmandupost.com/rss", "Nepal News"),
        FeedSource("The Himalayan Times", "https://thehimalayantimes.com/rss", "Nepal News"),
        FeedSource("Setopati English", "https://en.setopati.com/feed", "Nepal News"),
        FeedSource("Ratopati English", "https://english.ratopati.com/feed", "Nepal News"),

        // International Top & World Sources
        FeedSource("BBC World News", "https://feeds.bbci.co.uk/news/world/rss.xml", "World News"),
        FeedSource("Al Jazeera English", "https://www.aljazeera.com/xml/rss/all.xml", "World News"),
        FeedSource("AP News World", "https://feedx.net/rss/apnews.xml", "World News"),

        // Business, Finance & Economy
        FeedSource("BBC Business", "https://feeds.bbci.co.uk/news/business/rss.xml", "Business and Economy"),
        FeedSource("CNBC Global Markets", "https://search.cnbc.com/rs/search/view.html?partnerId=2000&keywords=cnbcnews&sort=date&type=rss", "Business and Economy"),

        // Technology, AI & Science
        FeedSource("TechCrunch", "https://techcrunch.com/feed/", "Technology and AI"),
        FeedSource("BBC Technology", "https://feeds.bbci.co.uk/news/technology/rss.xml", "Technology and AI"),
        FeedSource("NASA Science", "https://www.nasa.gov/rss/dyn/breaking_news.rss", "Science"),

        // Crypto & Commodities
        FeedSource("CoinDesk", "https://www.coindesk.com/arc/outboundfeeds/rss/", "Cryptocurrency"),
        FeedSource("CoinTelegraph", "https://cointelegraph.com/rss", "Cryptocurrency")
    )

    suspend fun fetchAllFeeds(): List<NewsArticle> = withContext(Dispatchers.IO) {
        val deferredList = feedSources.map { source ->
            async {
                fetchFeed(source)
            }
        }
        val results = deferredList.awaitAll().flatten()

        // Deduplicate by normalized title
        val seenTitles = mutableSetOf<String>()
        val deduplicated = mutableListOf<NewsArticle>()

        for (item in results) {
            val key = item.title.lowercase().trim().replace(Regex("[^a-z0-9]"), "")
            if (key.length > 5 && !seenTitles.contains(key)) {
                seenTitles.add(key)
                deduplicated.add(item)
            }
        }

        // Sort by publication time descending (newest first)
        deduplicated.sortedByDescending { it.pubDateMillis }
    }

    private suspend fun fetchFeed(source: FeedSource): List<NewsArticle> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(source.url).build()
            val response = ApiClient.okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val xml = response.body?.string() ?: return@withContext emptyList()
            parseRssXml(xml, source)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseRssXml(xml: String, source: FeedSource): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(xml))

        var eventType = parser.eventType
        var inItem = false

        var title = ""
        var link = ""
        var description = ""
        var pubDateRaw = ""
        var category = source.defaultCategory
        var imageUrl: String? = null

        val currentTime = System.currentTimeMillis()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName.equals("item", ignoreCase = true) || tagName.equals("entry", ignoreCase = true)) {
                        inItem = true
                        title = ""
                        link = ""
                        description = ""
                        pubDateRaw = ""
                        category = source.defaultCategory
                        imageUrl = null
                    } else if (inItem) {
                        when (tagName.lowercase()) {
                            "title" -> title = parser.nextText().trim()
                            "link" -> {
                                val href = parser.getAttributeValue(null, "href")
                                link = if (!href.isNullOrBlank()) href else parser.nextText().trim()
                            }
                            "description", "summary" -> description = parser.nextText().trim()
                            "pubdate", "published", "updated", "dc:date" -> pubDateRaw = parser.nextText().trim()
                            "category" -> {
                                val catText = parser.nextText().trim()
                                if (catText.isNotBlank()) {
                                    category = mapToAppCategory(catText, source.defaultCategory)
                                }
                            }
                            "enclosure", "media:content", "media:thumbnail" -> {
                                val url = parser.getAttributeValue(null, "url")
                                if (!url.isNullOrBlank() && imageUrl == null) {
                                    imageUrl = url
                                }
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagName.equals("item", ignoreCase = true) || tagName.equals("entry", ignoreCase = true)) {
                        inItem = false
                        if (title.isNotBlank() && link.isNotBlank()) {
                            val cleanDesc = cleanHtml(description)
                            val pubDateMillis = parsePubDate(pubDateRaw)
                            val (formattedDate, freshnessLabel) = calculateFreshness(pubDateMillis, currentTime)
                            val isBreaking = (currentTime - pubDateMillis) < (90 * 60 * 1000L) // Under 90 minutes old

                            // Derive dynamic category if default
                            val finalCategory = if (category == source.defaultCategory) {
                                inferCategoryFromContent(title, cleanDesc, source.defaultCategory)
                            } else category

                            val aiSummary = generateEditorialSummary(title, cleanDesc)

                            articles.add(
                                NewsArticle(
                                    id = link.hashCode().toString(),
                                    title = cleanHtml(title),
                                    summary = if (cleanDesc.isNotBlank()) cleanDesc else title,
                                    sourceName = source.publisherName,
                                    sourceUrl = link,
                                    imageUrl = imageUrl,
                                    pubDateMillis = pubDateMillis,
                                    pubDateFormatted = formattedDate,
                                    freshnessLabel = freshnessLabel,
                                    category = finalCategory,
                                    isBreaking = isBreaking,
                                    isBookmarked = false,
                                    aiSummary = aiSummary
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return articles
    }

    private fun cleanHtml(html: String): String {
        return html
            .replace(Regex("<[^>]*>"), " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun parsePubDate(dateStr: String): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis() - 3600_000L

        val formats = listOf(
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH),
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        )

        for (format in formats) {
            try {
                format.timeZone = TimeZone.getTimeZone("UTC")
                val parsed = format.parse(dateStr)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }

        return System.currentTimeMillis() - 1800_000L
    }

    private fun calculateFreshness(pubMillis: Long, currentMillis: Long): Pair<String, String> {
        val diff = currentMillis - pubMillis
        val date = Date(pubMillis)
        val formatted = SimpleDateFormat("MMM d, h:mm a", Locale.ENGLISH).format(date)

        val label = when {
            diff < 3600_000L -> "Just published"
            diff < 4 * 3600_000L -> "Recently published"
            diff < 24 * 3600_000L -> "Today"
            diff < 48 * 3600_000L -> "Yesterday"
            else -> "Older"
        }
        return Pair(formatted, label)
    }

    private fun mapToAppCategory(raw: String, fallback: String): String {
        val lower = raw.lowercase()
        return when {
            lower.contains("nepal") || lower.contains("kathmandu") -> "Nepal News"
            lower.contains("tech") || lower.contains("ai") || lower.contains("software") -> "Technology and AI"
            lower.contains("crypto") || lower.contains("bitcoin") || lower.contains("blockchain") -> "Cryptocurrency"
            lower.contains("gold") || lower.contains("silver") || lower.contains("metal") || lower.contains("bullion") -> "Gold and Precious Metals"
            lower.contains("politic") || lower.contains("government") || lower.contains("parliament") -> "Politics"
            lower.contains("business") || lower.contains("econom") || lower.contains("market") || lower.contains("finance") -> "Business and Economy"
            lower.contains("sport") || lower.contains("cricket") || lower.contains("football") -> "Sports"
            lower.contains("weather") || lower.contains("climate") || lower.contains("flood") -> "Weather and Climate"
            lower.contains("health") || lower.contains("medical") -> "Health and Science"
            lower.contains("science") -> "Science"
            lower.contains("entertain") || lower.contains("movie") || lower.contains("music") -> "Entertainment"
            else -> fallback
        }
    }

    private fun inferCategoryFromContent(title: String, desc: String, defaultCat: String): String {
        val combined = "$title $desc".lowercase()
        return when {
            combined.contains("gold") || combined.contains("bullion") || combined.contains("silver price") -> "Gold and Precious Metals"
            combined.contains("bitcoin") || combined.contains("crypto") || combined.contains("ethereum") -> "Cryptocurrency"
            combined.contains("artificial intelligence") || combined.contains("ai") || combined.contains("semiconductor") || combined.contains("google") || combined.contains("apple") || combined.contains("microsoft") -> "Technology and AI"
            combined.contains("election") || combined.contains("minister") || combined.contains("parliament") || combined.contains("president") -> "Politics"
            combined.contains("stock") || combined.contains("inflation") || combined.contains("gdp") || combined.contains("economy") || combined.contains("bank") -> "Business and Economy"
            combined.contains("weather") || combined.contains("rainfall") || combined.contains("temperature") || combined.contains("monsoon") -> "Weather and Climate"
            else -> defaultCat
        }
    }

    private fun generateEditorialSummary(title: String, desc: String): String {
        val clean = if (desc.length > 30) desc else title
        val sentences = clean.split(". ")
        val firstTwo = sentences.take(2).joinToString(". ").trim()
        return if (firstTwo.endsWith(".")) firstTwo else "$firstTwo."
    }
}
