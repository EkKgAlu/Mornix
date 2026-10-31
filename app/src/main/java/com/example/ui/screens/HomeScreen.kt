package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.text.font.FontStyle
import com.example.data.model.BibleVerse
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DisasterAlert
import com.example.data.model.ForexRateItem
import com.example.data.model.FreshnessStatus
import com.example.data.model.GoldSilverRates
import com.example.data.model.MarketQuote
import com.example.data.model.MorningBriefing
import com.example.data.model.NewsArticle
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherInfo
import com.example.ui.components.FreshnessBadge
import com.example.ui.components.MiniSparkline
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberGoldLight
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.EmeraldLive
import com.example.ui.viewmodel.MornixViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MornixViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val briefing by viewModel.briefing.collectAsState()
    val bullion by viewModel.bullionRates.collectAsState()
    val forexList by viewModel.forexRates.collectAsState()
    val weather by viewModel.weather.collectAsState()
    val newsList by viewModel.allNews.collectAsState()
    val markets by viewModel.markets.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val userPrefs by viewModel.userPreferences.collectAsState()

    val isPlayingTts by viewModel.isPlayingTts.collectAsState()
    val isPausedTts by viewModel.isPausedTts.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()

    var isDisasterDismissed by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 700.dp

        if (isTablet) {
            // Adaptive Tablet 2-Column Dashboard Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_screen_tablet_row")
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Column: Disaster Banner + Morning Briefing + News Feed
                LazyColumn(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                        .testTag("home_tablet_left_column"),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    // Top Natural Disaster & Hazard Notification Banner
                    if (alerts.isNotEmpty() && !isDisasterDismissed) {
                        item {
                            HomeDisasterTopBanner(
                                alerts = alerts,
                                onViewAlertDetails = { viewModel.selectTab(1) },
                                onDismiss = { isDisasterDismissed = true }
                            )
                        }
                    }

                    if (userPrefs.enabledWidgets.contains("briefing")) {
                        item {
                            MorningBriefingCard(
                                briefing = briefing,
                                isPlayingTts = isPlayingTts,
                                isPausedTts = isPausedTts,
                                speechRate = speechRate,
                                onPlay = { viewModel.playBriefing() },
                                onPause = { viewModel.pauseBriefing() },
                                onResume = { viewModel.resumeBriefing() },
                                onStop = { viewModel.stopBriefing() },
                                onSpeedChange = { viewModel.setTtsSpeed(it) },
                                onOpenChat = { viewModel.selectTab(4) }
                            )
                        }
                    }

                    if (userPrefs.dailyBibleVerseEnabled) {
                        item {
                            val verse = briefing?.bibleVerse ?: viewModel.getTodayBibleVerse()
                            DailyBibleVerseCard(
                                verse = verse,
                                onShare = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Daily Scripture: ${verse.reference}")
                                        putExtra(Intent.EXTRA_TEXT, "\"${verse.text}\"\n— ${verse.reference} (${verse.translation})\nShared via Mornix")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Scripture"))
                                }
                            )
                        }
                    }

                    if (userPrefs.enabledWidgets.contains("breaking_news")) {
                        item {
                            SectionHeader(
                                title = "Today's Verified News",
                                subtitle = "Sourced directly from official news publishers",
                                icon = Icons.Default.Newspaper,
                                actionLabel = "View All",
                                onActionClick = { viewModel.selectTab(1) }
                            )
                        }

                        val topArticles = newsList.take(6)
                        items(topArticles, key = { it.id }) { article ->
                            NewsArticleCard(
                                article = article,
                                onToggleBookmark = { viewModel.toggleBookmark(article.id) },
                                onOpenUrl = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.sourceUrl))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }

                // Right Column: Weather, Bullion, Forex, Disaster Alerts, Markets
                LazyColumn(
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxHeight()
                        .testTag("home_tablet_right_column"),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    if (userPrefs.enabledWidgets.contains("weather")) {
                        item {
                            WeatherCard(
                                weather = weather,
                                tempUnit = userPrefs.temperatureUnit,
                                onViewFullForecast = { viewModel.selectTab(3) }
                            )
                        }
                    }

                    if (userPrefs.enabledWidgets.contains("gold_silver")) {
                        item {
                            NepalBullionCard(
                                bullion = bullion,
                                onNavigateToCalculator = {
                                    viewModel.selectTab(2)
                                    viewModel.selectMarketsSubTab(3)
                                }
                            )
                        }
                    }

                    if (userPrefs.enabledWidgets.contains("forex")) {
                        item {
                            ForexOverviewCard(
                                forexList = forexList,
                                favoriteCodes = userPrefs.favoriteCurrencies,
                                onToggleFavorite = { viewModel.toggleFavoriteCurrency(it) },
                                onNavigateToConverter = {
                                    viewModel.selectTab(2)
                                    viewModel.selectMarketsSubTab(2)
                                }
                            )
                        }
                    }

                    if (userPrefs.enabledWidgets.contains("alerts") && alerts.isNotEmpty()) {
                        item {
                            DisasterAlertsCard(
                                alerts = alerts,
                                onViewAllAlerts = { viewModel.selectTab(1) }
                            )
                        }
                    }

                    if (userPrefs.enabledWidgets.contains("markets")) {
                        item {
                            MarketsOverviewCard(
                                markets = markets,
                                onToggleFavorite = { viewModel.toggleFavoriteMarket(it) },
                                onViewAllMarkets = {
                                    viewModel.selectTab(2)
                                    viewModel.selectMarketsSubTab(4)
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        } else {
            // Standard Phone Single Column Layout
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_screen_lazy_column")
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // 0. Top Natural Disaster Alert Notification Banner
                if (alerts.isNotEmpty() && !isDisasterDismissed) {
                    item {
                        HomeDisasterTopBanner(
                            alerts = alerts,
                            onViewAlertDetails = { viewModel.selectTab(1) },
                            onDismiss = { isDisasterDismissed = true }
                        )
                    }
                }

                // 1. Briefing Card
                if (userPrefs.enabledWidgets.contains("briefing")) {
                    item {
                        MorningBriefingCard(
                            briefing = briefing,
                            isPlayingTts = isPlayingTts,
                            isPausedTts = isPausedTts,
                            speechRate = speechRate,
                            onPlay = { viewModel.playBriefing() },
                            onPause = { viewModel.pauseBriefing() },
                            onResume = { viewModel.resumeBriefing() },
                            onStop = { viewModel.stopBriefing() },
                            onSpeedChange = { viewModel.setTtsSpeed(it) },
                            onOpenChat = { viewModel.selectTab(4) }
                        )
                    }
                }

                // Daily Bible Verse Card (when enabled by user, default is off)
                if (userPrefs.dailyBibleVerseEnabled) {
                    item {
                        val verse = briefing?.bibleVerse ?: viewModel.getTodayBibleVerse()
                        DailyBibleVerseCard(
                            verse = verse,
                            onShare = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Daily Scripture: ${verse.reference}")
                                    putExtra(Intent.EXTRA_TEXT, "\"${verse.text}\"\n— ${verse.reference} (${verse.translation})\nShared via Mornix")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Scripture"))
                            }
                        )
                    }
                }

                // 2. Nepal Bullion Rates Card
                if (userPrefs.enabledWidgets.contains("gold_silver")) {
                    item {
                        NepalBullionCard(
                            bullion = bullion,
                            onNavigateToCalculator = {
                                viewModel.selectTab(2) // Markets tab
                                viewModel.selectMarketsSubTab(3) // Calculator
                            }
                        )
                    }
                }

                // 3. Foreign Exchange (NRB) Quick Overview
                if (userPrefs.enabledWidgets.contains("forex")) {
                    item {
                        ForexOverviewCard(
                            forexList = forexList,
                            favoriteCodes = userPrefs.favoriteCurrencies,
                            onToggleFavorite = { viewModel.toggleFavoriteCurrency(it) },
                            onNavigateToConverter = {
                                viewModel.selectTab(2) // Markets tab
                                viewModel.selectMarketsSubTab(2) // Converter
                            }
                        )
                    }
                }

                // 4. Weather Snapshot
                if (userPrefs.enabledWidgets.contains("weather")) {
                    item {
                        WeatherCard(
                            weather = weather,
                            tempUnit = userPrefs.temperatureUnit,
                            onViewFullForecast = {
                                viewModel.selectTab(3) // Weather tab
                            }
                        )
                    }
                }

                // 5. Important Alerts / Earthquakes
                if (userPrefs.enabledWidgets.contains("alerts") && alerts.isNotEmpty()) {
                    item {
                        DisasterAlertsCard(
                            alerts = alerts,
                            onViewAllAlerts = { viewModel.selectTab(1) }
                        )
                    }
                }

                // 6. International Markets & Commodities Ticker
                if (userPrefs.enabledWidgets.contains("markets")) {
                    item {
                        MarketsOverviewCard(
                            markets = markets,
                            onToggleFavorite = { viewModel.toggleFavoriteMarket(it) },
                            onViewAllMarkets = {
                                viewModel.selectTab(2)
                                viewModel.selectMarketsSubTab(4) // International
                            }
                        )
                    }
                }

                // 7. Top Breaking News
                if (userPrefs.enabledWidgets.contains("breaking_news")) {
                    item {
                        SectionHeader(
                            title = "Today's Verified News",
                            subtitle = "Sourced directly from official news publishers",
                            icon = Icons.Default.Newspaper,
                            actionLabel = "View All",
                            onActionClick = { viewModel.selectTab(1) } // News tab
                        )
                    }

                    val topArticles = newsList.take(4)
                    items(topArticles, key = { it.id }) { article ->
                        NewsArticleCard(
                            article = article,
                            onToggleBookmark = { viewModel.toggleBookmark(article.id) },
                            onOpenUrl = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.sourceUrl))
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun MorningBriefingCard(
    briefing: MorningBriefing?,
    isPlayingTts: Boolean,
    isPausedTts: Boolean,
    speechRate: Float,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onOpenChat: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("morning_briefing_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.mornix_logo),
                        contentDescription = "Mornix",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = briefing?.greeting ?: "Good Morning",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Made with AI by Resham",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                FreshnessBadge(
                    status = FreshnessStatus.LIVE,
                    sourceName = "Synthesized"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Executive Summary
            Text(
                text = briefing?.executiveSummary ?: "Synthesizing today's morning briefing from live data sources...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Audio Player Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                when {
                                    isPlayingTts -> onPause()
                                    isPausedTts -> onResume()
                                    else -> onPlay()
                                }
                            },
                            modifier = Modifier
                                .testTag("tts_main_play_button")
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = if (isPlayingTts) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlayingTts) "Pause" else "Play Briefing",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        if (isPlayingTts || isPausedTts) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onStop,
                                modifier = Modifier
                                    .testTag("tts_stop_button")
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop Briefing",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isPlayingTts) "Reading Briefing..." else if (isPausedTts) "Paused" else "Listen to Briefing",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Natural Voice TTS",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Speed Pill Selector
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                            val isSelected = (speechRate == speed)
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onSpeedChange(speed) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${speed}x",
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Highlights
            if (briefing != null && briefing.newsHighlights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    briefing.newsHighlights.take(3).forEach { highlight ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "• ",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = highlight,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Daily Bible Verse (shown in Morning Brief if user enabled it)
            if (briefing?.bibleVerse != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("morning_brief_bible_verse_card"),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Daily Scripture",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = briefing.bibleVerse.reference,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"${briefing.bibleVerse.text}\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                        if (briefing.bibleVerse.theme.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Theme: ${briefing.bibleVerse.theme} • ${briefing.bibleVerse.translation}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenChat() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ask Mornix AI Assistant (Gemini)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun NepalBullionCard(
    bullion: GoldSilverRates?,
    onNavigateToCalculator: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nepal_bullion_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = AmberGoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Nepal Gold & Silver Rates",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Official FENEGOSIDA Bullion Rates",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FreshnessBadge(
                    status = bullion?.freshnessStatus ?: FreshnessStatus.UNAVAILABLE,
                    sourceName = "FENEGOSIDA"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (bullion != null && bullion.fineGoldTola > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Fine Gold (Chhapawal, 24K)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Fine Gold (24K)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "NPR ${String.format(Locale.ENGLISH, "%,.0f", bullion.fineGoldTola)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "per Tola",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            PriceChangeBadge(
                                change = bullion.changeGoldTola,
                                changePercent = bullion.changeGoldTolaPercent
                            )
                        }
                    }

                    // Silver
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Silver (Tejabi/Pure)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "NPR ${String.format(Locale.ENGLISH, "%,.0f", bullion.silverTola)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "per Tola",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            PriceChangeBadge(
                                change = bullion.changeSilverTola,
                                changePercent = bullion.changeSilverTolaPercent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tejabi & 10g reference
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "10 Grams Fine Gold: NPR ${String.format(Locale.ENGLISH, "%,.0f", bullion.fineGold10g)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Published: ${bullion.publishedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Awaiting official rate publication from FENEGOSIDA...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action: Calculator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCalculator() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Open Gold & Silver Value Calculator (24K, 22K, 18K)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ForexOverviewCard(
    forexList: List<ForexRateItem>,
    favoriteCodes: List<String>,
    onToggleFavorite: (String) -> Unit,
    onNavigateToConverter: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("forex_overview_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Nepal Rastra Bank Forex",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Official Central Bank Rates (Base: NPR • ⭐ Favorite to pin)",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FreshnessBadge(
                    status = if (forexList.isNotEmpty()) FreshnessStatus.LIVE else FreshnessStatus.UNAVAILABLE,
                    sourceName = "NRB"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val displayList = forexList.filter { favoriteCodes.contains(it.currencyCode) }
                .ifEmpty { forexList.take(6) }

            if (displayList.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayList, key = { it.currencyCode }) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = item.flagEmoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${item.unit} ${item.currencyCode}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(
                                        onClick = { onToggleFavorite(item.currencyCode) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "Toggle favorite",
                                            tint = if (item.isFavorite) AmberGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Buy: NPR ${item.buyRate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Sell: NPR ${item.sellRate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Awaiting official exchange rate data from Nepal Rastra Bank...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToConverter() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Open Full Currency Converter (Official Rates)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun WeatherCard(
    weather: WeatherInfo?,
    tempUnit: TemperatureUnit,
    onViewFullForecast: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weather_snapshot_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = AmberGoldLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${weather?.cityName ?: "Nepal"} Weather",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = weather?.conditionDescription ?: "Live Observation",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FreshnessBadge(
                    status = weather?.freshnessStatus ?: FreshnessStatus.UNAVAILABLE,
                    sourceName = "Open-Meteo"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (weather != null) {
                val temp = if (tempUnit == TemperatureUnit.FAHRENHEIT) {
                    (weather.currentTempC * 9 / 5 + 32).toInt()
                } else {
                    weather.currentTempC.toInt()
                }
                val symbol = if (tempUnit == TemperatureUnit.FAHRENHEIT) "°F" else "°C"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$temp$symbol",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Feels like ${if (tempUnit == TemperatureUnit.FAHRENHEIT) (weather.feelsLikeC * 9 / 5 + 32).toInt() else weather.feelsLikeC.toInt()}$symbol",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Humidity: ${weather.humidityPercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Wind: ${weather.windSpeedKmh.toInt()} km/h ${weather.windDirection}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "AQI: ${weather.aqi} (${weather.aqiStatus})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (weather.aqi <= 100) EmeraldLive else AmberGoldPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hourly timeline preview
                if (weather.hourlyForecast.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(weather.hourlyForecast.take(6)) { hour ->
                            val hTemp = if (tempUnit == TemperatureUnit.FAHRENHEIT) {
                                (hour.tempC * 9 / 5 + 32).toInt()
                            } else {
                                hour.tempC.toInt()
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = hour.timeLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$hTemp$symbol",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${hour.rainProb}% rain",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewFullForecast() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View 7-Day Forecast & City Switcher",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun MarketsOverviewCard(
    markets: List<MarketQuote>,
    onToggleFavorite: (String) -> Unit,
    onViewAllMarkets: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("markets_overview_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "International Markets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Commodities, Crypto & Global Indices (⭐ Favorite to pin)",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FreshnessBadge(
                    status = if (markets.isNotEmpty()) FreshnessStatus.LIVE else FreshnessStatus.UNAVAILABLE,
                    sourceName = "Exchanges"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (markets.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(markets.take(6), key = { it.symbol }) { quote ->
                        val isPositive = quote.changePercent >= 0
                        Surface(
                            modifier = Modifier.width(148.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = quote.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onToggleFavorite(quote.symbol) },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (quote.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "Toggle Favorite",
                                            tint = if (quote.isFavorite) AmberGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${quote.currencySymbol}${String.format(Locale.ENGLISH, "%,.2f", quote.price)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${if (isPositive) "+" else ""}${String.format(Locale.ENGLISH, "%.2f", quote.changePercent)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isPositive) EmeraldLive else CoralWarning
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                MiniSparkline(
                                    points = quote.historyPoints,
                                    isPositive = isPositive,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewAllMarkets() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View Full Market Watch & Trends",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun HomeDisasterTopBanner(
    alerts: List<DisasterAlert>,
    onViewAlertDetails: () -> Unit,
    onDismiss: () -> Unit
) {
    val topAlert = alerts.firstOrNull() ?: return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_disaster_top_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoralWarning.copy(alpha = 0.16f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CoralWarning.copy(alpha = 0.65f))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Disaster Alert",
                        tint = CoralWarning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🚨 ${topAlert.severity} DISASTER ALERT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CoralWarning
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Alert",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = topAlert.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = topAlert.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Authority: ${topAlert.issuingAuthority}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "View Details →",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CoralWarning,
                    modifier = Modifier.clickable { onViewAlertDetails() }
                )
            }
        }
    }
}

@Composable
fun DisasterAlertsCard(
    alerts: List<DisasterAlert>,
    onViewAllAlerts: () -> Unit
) {
    val topAlert = alerts.firstOrNull() ?: return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("disaster_alert_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoralWarning.copy(alpha = 0.12f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CoralWarning.copy(alpha = 0.4f))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = CoralWarning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Official Hazard Notice (${topAlert.severity})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CoralWarning
                    )
                }

                Text(
                    text = topAlert.pubDateFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = topAlert.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = topAlert.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Authority: ${topAlert.issuingAuthority}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun NewsArticleCard(
    article: NewsArticle,
    onToggleBookmark: () -> Unit,
    onOpenUrl: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("news_article_card_${article.id}")
            .clickable { onOpenUrl() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Source & Freshness Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.sourceName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${article.freshnessLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (article.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            if (!article.aiSummary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Grounded AI Briefing",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = article.aiSummary,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Original Article",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PriceChangeBadge(change: Double, changePercent: Double) {
    val isPositive = change >= 0
    val color = if (isPositive) EmeraldLive else CoralWarning
    val sign = if (isPositive) "+" else ""

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$sign${String.format(Locale.ENGLISH, "%,.0f", change)} ($sign${String.format(Locale.ENGLISH, "%.2f", changePercent)}%)",
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DailyBibleVerseCard(
    verse: BibleVerse,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_bible_verse_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Bible Verse",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Daily Scripture Reflection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${verse.theme} • ${verse.dateFormatted}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Verse",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "\"${verse.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "— ${verse.reference} (${verse.translation})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

