package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ForexRateItem
import com.example.data.model.FreshnessStatus
import com.example.data.model.GoldSilverRates
import com.example.data.model.GoldWeightUnit
import com.example.data.model.MarketQuote
import com.example.ui.components.FreshnessBadge
import com.example.ui.components.MiniSparkline
import com.example.ui.theme.AmberGoldLight
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.EmeraldLive
import com.example.ui.viewmodel.MornixViewModel
import java.util.Locale

@Composable
fun MarketsScreen(
    viewModel: MornixViewModel,
    modifier: Modifier = Modifier
) {
    val subTab by viewModel.selectedMarketsSubTab.collectAsState()
    val bullion by viewModel.bullionRates.collectAsState()
    val forexList by viewModel.forexRates.collectAsState()
    val markets by viewModel.markets.collectAsState()

    val subTabTitles = listOf("Nepal Bullion", "Forex Rates", "Currency Converter", "Gold Calculator", "Global Markets")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("markets_screen")
    ) {
        ScrollableTabRow(
            selectedTabIndex = subTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            subTabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = (subTab == index),
                    onClick = { viewModel.selectMarketsSubTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (subTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("markets_subtab_$index")
                )
            }
        }

        when (subTab) {
            0 -> NepalBullionDetailView(bullion)
            1 -> ForexListView(forexList, onToggleFavorite = { viewModel.toggleFavoriteCurrency(it) })
            2 -> CurrencyConverterView(viewModel, forexList)
            3 -> GoldCalculatorView(viewModel, bullion)
            4 -> InternationalMarketsView(markets, onToggleFavorite = { viewModel.toggleFavoriteMarket(it) })
        }
    }
}

@Composable
fun NepalBullionDetailView(bullion: GoldSilverRates?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
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
                        Column {
                            Text(
                                text = "Nepal Bullion Rates",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Federation of Nepal Gold & Silver Dealers' Association",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FreshnessBadge(
                            status = bullion?.freshnessStatus ?: FreshnessStatus.UNAVAILABLE,
                            sourceName = "FENEGOSIDA"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Official rates published for: ${bullion?.publishedDate ?: "Today"}, ${bullion?.publishedTime ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (bullion != null && bullion.fineGoldTola > 0) {
            item {
                Text(
                    text = "Gold Rates by Purity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 24K Fine Gold (Chhapawal)
            item {
                BullionPurityCard(
                    purityLabel = "24 Karat (Fine / Chhapawal Gold)",
                    puritySub = "99.9% Pure • Official FENEGOSIDA Benchmark",
                    pricePerTola = bullion.fineGoldTola,
                    pricePer10g = bullion.fineGold10g,
                    pricePerGram = bullion.fineGold10g / 10.0,
                    change = bullion.changeGoldTola,
                    changePercent = bullion.changeGoldTolaPercent,
                    accentColor = AmberGoldPrimary
                )
            }

            // 22K Tejabi Gold
            item {
                BullionPurityCard(
                    purityLabel = "22 Karat (Tejabi Gold)",
                    puritySub = "91.6% Pure • Standard Jewelry Benchmark",
                    pricePerTola = bullion.tejabiGoldTola,
                    pricePer10g = bullion.tejabiGold10g,
                    pricePerGram = bullion.tejabiGold10g / 10.0,
                    change = bullion.changeGoldTola * (22.0 / 24.0),
                    changePercent = bullion.changeGoldTolaPercent,
                    accentColor = AmberGoldLight
                )
            }

            // 18K Gold
            item {
                BullionPurityCard(
                    purityLabel = "18 Karat Gold",
                    puritySub = "75.0% Pure • Modern Diamond Jewelry",
                    pricePerTola = bullion.fineGoldTola * (18.0 / 24.0),
                    pricePer10g = bullion.fineGold10g * (18.0 / 24.0),
                    pricePerGram = (bullion.fineGold10g / 10.0) * (18.0 / 24.0),
                    change = bullion.changeGoldTola * (18.0 / 24.0),
                    changePercent = bullion.changeGoldTolaPercent,
                    accentColor = MaterialTheme.colorScheme.secondary
                )
            }

            // 14K Gold
            item {
                BullionPurityCard(
                    purityLabel = "14 Karat Gold",
                    puritySub = "58.3% Pure • Export & Daily Wear",
                    pricePerTola = bullion.fineGoldTola * (14.0 / 24.0),
                    pricePer10g = bullion.fineGold10g * (14.0 / 24.0),
                    pricePerGram = (bullion.fineGold10g / 10.0) * (14.0 / 24.0),
                    change = bullion.changeGoldTola * (14.0 / 24.0),
                    changePercent = bullion.changeGoldTolaPercent,
                    accentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = "Silver Rates",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Silver
            item {
                BullionPurityCard(
                    purityLabel = "Pure Silver (चाँदी दर)",
                    puritySub = "Fine Silver • Official FENEGOSIDA Benchmark",
                    pricePerTola = bullion.silverTola,
                    pricePer10g = bullion.silver10g,
                    pricePerGram = bullion.silver10g / 10.0,
                    change = bullion.changeSilverTola,
                    changePercent = bullion.changeSilverTolaPercent,
                    accentColor = MaterialTheme.colorScheme.secondary
                )
            }

            item {
                Text(
                    text = "Note: 1 Tola = 11.6638 Grams. All rates in Nepalese Rupees (NPR). Data sourced from official FENEGOSIDA publications.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Loading official Nepal bullion rates from FENEGOSIDA...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BullionPurityCard(
    purityLabel: String,
    puritySub: String,
    pricePerTola: Double,
    pricePer10g: Double,
    pricePerGram: Double,
    change: Double,
    changePercent: Double,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = purityLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = puritySub,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                PriceChangeBadge(change = change, changePercent = changePercent)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Per Tola", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "NPR ${String.format(Locale.ENGLISH, "%,.0f", pricePerTola)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(text = "Per 10 Grams", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "NPR ${String.format(Locale.ENGLISH, "%,.0f", pricePer10g)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(text = "Per Gram", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "NPR ${String.format(Locale.ENGLISH, "%,.1f", pricePerGram)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ForexListView(
    forexList: List<ForexRateItem>,
    onToggleFavorite: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Nepal Rastra Bank Official Forex",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Central Bank Foreign Exchange Fixation (⭐ Star to pin)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FreshnessBadge(
                            status = if (forexList.isNotEmpty()) FreshnessStatus.LIVE else FreshnessStatus.UNAVAILABLE,
                            sourceName = "NRB"
                        )
                    }
                    if (forexList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Date: ${forexList.first().date} • Published: ${forexList.first().publishedOn}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(forexList, key = { it.currencyCode }) { item ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onToggleFavorite(item.currencyCode) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Toggle Favorite",
                                tint = if (item.isFavorite) AmberGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = item.flagEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${item.unit} ${item.currencyCode}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.currencyName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Buying", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "NPR ${item.buyRate}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLive
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Selling", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "NPR ${item.sellRate}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CoralWarning
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyConverterView(
    viewModel: MornixViewModel,
    forexList: List<ForexRateItem>
) {
    val src by viewModel.convSourceCurrency.collectAsState()
    val tgt by viewModel.convTargetCurrency.collectAsState()
    val amountStr by viewModel.convAmount.collectAsState()

    val amount = amountStr.toDoubleOrNull() ?: 0.0

    // Compute conversion using NRB rates:
    // If src != NPR and tgt == NPR: amount * (rate / unit)
    // If src == NPR and tgt != NPR: amount / (rate / unit)
    // If src != NPR and tgt != NPR: cross rate
    val srcItem = forexList.find { it.currencyCode == src }
    val tgtItem = forexList.find { it.currencyCode == tgt }

    val srcToNpr = if (src == "NPR") 1.0 else if (srcItem != null && srcItem.unit > 0) srcItem.buyRate / srcItem.unit else 1.0
    val tgtToNpr = if (tgt == "NPR") 1.0 else if (tgtItem != null && tgtItem.unit > 0) tgtItem.sellRate / tgtItem.unit else 1.0

    val converted = if (tgtToNpr > 0) (amount * srcToNpr) / tgtToNpr else 0.0
    val singleRate = if (tgtToNpr > 0) srcToNpr / tgtToNpr else 0.0

    val supportedCurrencies = listOf("USD", "INR", "EUR", "GBP", "AUD", "CAD", "JPY", "AED", "SAR", "QAR", "SGD", "MYR", "CNY", "NPR")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Official Currency Converter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calculated using verified Nepal Rastra Bank exchange rates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { viewModel.setConvAmount(it) },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("converter_amount_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Currency Selectors & Swap
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "From", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            CurrencyDropdown(
                                selected = src,
                                onSelect = { viewModel.setConvSource(it) },
                                options = supportedCurrencies
                            )
                        }

                        IconButton(
                            onClick = { viewModel.swapCurrencies() },
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Default.SwapVert, contentDescription = "Swap", tint = MaterialTheme.colorScheme.primary)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "To", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            CurrencyDropdown(
                                selected = tgt,
                                onSelect = { viewModel.setConvTarget(it) },
                                options = supportedCurrencies
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Result Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                text = "Converted Amount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${String.format(Locale.ENGLISH, "%,.2f", converted)} $tgt",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1 $src = ${String.format(Locale.ENGLISH, "%.4f", singleRate)} $tgt",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Methodology: Official central bank buying/selling rates from Nepal Rastra Bank. No hidden margins applied.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CurrencyDropdown(
    selected: String,
    onSelect: (String) -> Unit,
    options: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Text(
                text = selected,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(12.dp)
            )
        }

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(opt, fontWeight = if (opt == selected) FontWeight.Bold else FontWeight.Normal) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun GoldCalculatorView(
    viewModel: MornixViewModel,
    bullion: GoldSilverRates?
) {
    val weightStr by viewModel.calcWeight.collectAsState()
    val unit by viewModel.calcUnit.collectAsState()
    val purity by viewModel.calcPurity.collectAsState()
    val metal by viewModel.calcMetal.collectAsState()

    val weight = weightStr.toDoubleOrNull() ?: 0.0

    // Baseline 24K per gram = fineGold10g / 10
    val fineGoldPerGram = (bullion?.fineGold10g ?: 24031.0) / 10.0
    val silverPerGram = (bullion?.silver10g ?: 328.0) / 10.0

    // Purity factor:
    val purityFactor = when (purity) {
        "24K" -> 1.0
        "22K" -> 22.0 / 24.0
        "18K" -> 18.0 / 24.0
        "14K" -> 14.0 / 24.0
        else -> 1.0
    }

    // Weight in grams:
    // 1 tola = 11.6638 grams
    // 10 grams = 10 grams
    // 1 troy ounce = 31.1035 grams
    val weightInGrams = when (unit) {
        GoldWeightUnit.TOLA -> weight * 11.6638
        GoldWeightUnit.GRAM -> weight
        GoldWeightUnit.TEN_GRAM -> weight * 10.0
        GoldWeightUnit.TROY_OUNCE -> weight * 31.1035
    }

    val baseRatePerGram = if (metal == "Gold") (fineGoldPerGram * purityFactor) else silverPerGram
    val totalEstimatedValue = weightInGrams * baseRatePerGram

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Nepal Gold & Silver Value Calculator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Based on official FENEGOSIDA published bullion benchmark",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metal Choice (Gold vs Silver)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = (metal == "Gold"),
                            onClick = { viewModel.setCalcMetal("Gold") },
                            label = { Text("Gold (सुन)") }
                        )
                        FilterChip(
                            selected = (metal == "Silver"),
                            onClick = { viewModel.setCalcMetal("Silver") },
                            label = { Text("Silver (चाँदी)") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Purity Choice (if Gold)
                    if (metal == "Gold") {
                        Text(text = "Gold Purity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("24K", "22K", "18K", "14K").forEach { p ->
                                FilterChip(
                                    selected = (purity == p),
                                    onClick = { viewModel.setCalcPurity(p) },
                                    label = { Text(p) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Weight Unit
                    Text(text = "Weight Unit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Pair("Tola", GoldWeightUnit.TOLA),
                            Pair("Gram", GoldWeightUnit.GRAM),
                            Pair("10 Grams", GoldWeightUnit.TEN_GRAM),
                            Pair("Troy Oz", GoldWeightUnit.TROY_OUNCE)
                        ).forEach { (label, u) ->
                            FilterChip(
                                selected = (unit == u),
                                onClick = { viewModel.setCalcUnit(u) },
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { viewModel.setCalcWeight(it) },
                        label = { Text("Weight ($unit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("gold_calc_weight_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calculation Results
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                text = "Estimated Bullion Value",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "NPR ${String.format(Locale.ENGLISH, "%,.0f", totalEstimatedValue)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = AmberGoldPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Equivalent Weight: ${String.format(Locale.ENGLISH, "%.3f", weightInGrams)} Grams",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Rate applied: NPR ${String.format(Locale.ENGLISH, "%,.1f", baseRatePerGram)} / gram",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Methodology: Based on FENEGOSIDA official rate (${bullion?.publishedDate ?: "Today"}). Formula: Weight (g) × (Official 24K Rate / 10g / 10) × Purity Ratio. Excludes jewelry making charges and taxes.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun InternationalMarketsView(
    markets: List<MarketQuote>,
    onToggleFavorite: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Global Market Watch",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Commodities, Crypto, Global Indices (⭐ Star to pin)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FreshnessBadge(
                            status = if (markets.isNotEmpty()) FreshnessStatus.LIVE else FreshnessStatus.UNAVAILABLE,
                            sourceName = "Exchanges"
                        )
                    }
                }
            }
        }

        items(markets, key = { it.symbol }) { quote ->
            val isPositive = quote.changePercent >= 0
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = { onToggleFavorite(quote.symbol) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (quote.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Toggle Favorite",
                                    tint = if (quote.isFavorite) AmberGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = quote.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${quote.category} • ${quote.source}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${quote.currencySymbol}${String.format(Locale.ENGLISH, "%,.2f", quote.price)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${if (isPositive) "+" else ""}${String.format(Locale.ENGLISH, "%.2f", quote.changePercent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) EmeraldLive else CoralWarning
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    MiniSparkline(
                        points = quote.historyPoints,
                        isPositive = isPositive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Status: ${quote.marketStatus}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Daily Change: ${if (isPositive) "+" else ""}${String.format(Locale.ENGLISH, "%,.2f", quote.change24h)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = if (isPositive) EmeraldLive else CoralWarning
                        )
                    }
                }
            }
        }
    }
}
