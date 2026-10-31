package com.example.ui.screens

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calendar.AgeResult
import com.example.data.calendar.CalendarDayItem
import com.example.data.calendar.EnglishDate
import com.example.data.calendar.NepaliDate
import com.example.data.calendar.NepaliDateConverter
import com.example.data.calendar.NepaliDateUtils
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.EmeraldLive
import com.example.ui.viewmodel.MornixViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MornixViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSubTab by viewModel.selectedCalendarSubTab.collectAsState()
    val context = LocalContext.current

    // Live today's dates
    val todayAd = remember { NepaliDateConverter.todayAd() }
    val todayBs = remember { NepaliDateConverter.todayBs() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen")
    ) {
        // Today Banner Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("today_calendar_banner"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${todayBs.formattedNe} (बि.सं.)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${todayAd.formatted} • ${NepaliDateUtils.WEEKDAYS_FULL_EN[NepaliDateConverter.getDayOfWeek(todayAd).value % 7]}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "TODAY",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3 Sub-tabs: Calendar View, Date Converter, Age Calculator
        PrimaryTabRow(
            selectedTabIndex = selectedSubTab,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = (selectedSubTab == 0),
                onClick = { viewModel.selectCalendarSubTab(0) },
                text = { Text("Calendar", fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar View", modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_calendar_view")
            )
            Tab(
                selected = (selectedSubTab == 1),
                onClick = { viewModel.selectCalendarSubTab(1) },
                text = { Text("Converter", fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Date Converter", modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_date_converter")
            )
            Tab(
                selected = (selectedSubTab == 2),
                onClick = { viewModel.selectCalendarSubTab(2) },
                text = { Text("Age & Days", fontWeight = if (selectedSubTab == 2) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.Cake, contentDescription = "Age Calculator", modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_age_calculator")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTablet = maxWidth >= 600.dp

            when (selectedSubTab) {
                0 -> MonthlyCalendarView(
                    todayBs = todayBs,
                    todayAd = todayAd,
                    isTablet = isTablet,
                    context = context,
                    onNavigateToAgeCalc = { bsDate ->
                        viewModel.saveBirthDate("BS", bsDate.year, bsDate.month, bsDate.day)
                        viewModel.selectCalendarSubTab(2)
                    }
                )
                1 -> DateConverterView(
                    todayBs = todayBs,
                    todayAd = todayAd,
                    isTablet = isTablet,
                    context = context
                )
                2 -> AgeCalculatorView(
                    viewModel = viewModel,
                    todayAd = todayAd,
                    todayBs = todayBs,
                    isTablet = isTablet,
                    context = context
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 1. MONTHLY CALENDAR VIEW (BS / AD)
// -------------------------------------------------------------
@Composable
fun MonthlyCalendarView(
    todayBs: NepaliDate,
    todayAd: EnglishDate,
    isTablet: Boolean,
    context: Context,
    onNavigateToAgeCalc: (NepaliDate) -> Unit
) {
    var isBsView by remember { mutableStateOf(true) }

    // BS Month navigation state
    var currentBsYear by remember { mutableIntStateOf(todayBs.year) }
    var currentBsMonth by remember { mutableIntStateOf(todayBs.month) }

    // AD Month navigation state
    var currentAdYear by remember { mutableIntStateOf(todayAd.year) }
    var currentAdMonth by remember { mutableIntStateOf(todayAd.month) }

    // Selected Day in Calendar
    var selectedDayItem by remember {
        mutableStateOf<CalendarDayItem?>(null)
    }

    val daysList by remember(isBsView, currentBsYear, currentBsMonth, currentAdYear, currentAdMonth) {
        derivedStateOf {
            if (isBsView) {
                NepaliDateConverter.generateCalendarMonthBs(currentBsYear, currentBsMonth)
            } else {
                NepaliDateConverter.generateCalendarMonthAd(currentAdYear, currentAdMonth)
            }
        }
    }

    val clipboardManager = LocalClipboardManager.current

    if (isTablet) {
        // Tablet Side-by-Side: Left = Calendar Grid, Right = Day Details & Overview
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CalendarNavigationHeader(
                        isBsView = isBsView,
                        bsYear = currentBsYear,
                        bsMonth = currentBsMonth,
                        adYear = currentAdYear,
                        adMonth = currentAdMonth,
                        onPrevYear = {
                            if (isBsView) {
                                if (currentBsYear > 2000) currentBsYear--
                            } else {
                                if (currentAdYear > 1944) currentAdYear--
                            }
                        },
                        onNextYear = {
                            if (isBsView) {
                                if (currentBsYear < 2100) currentBsYear++
                            } else {
                                if (currentAdYear < 2043) currentAdYear++
                            }
                        },
                        onPrevMonth = {
                            if (isBsView) {
                                if (currentBsMonth == 1) {
                                    if (currentBsYear > 2000) {
                                        currentBsYear--
                                        currentBsMonth = 12
                                    }
                                } else {
                                    currentBsMonth--
                                }
                            } else {
                                if (currentAdMonth == 1) {
                                    currentAdYear--
                                    currentAdMonth = 12
                                } else {
                                    currentAdMonth--
                                }
                            }
                        },
                        onNextMonth = {
                            if (isBsView) {
                                if (currentBsMonth == 12) {
                                    if (currentBsYear < 2100) {
                                        currentBsYear++
                                        currentBsMonth = 1
                                    }
                                } else {
                                    currentBsMonth++
                                }
                            } else {
                                if (currentAdMonth == 12) {
                                    currentAdYear++
                                    currentAdMonth = 1
                                } else {
                                    currentAdMonth++
                                }
                            }
                        },
                        onToday = {
                            currentBsYear = todayBs.year
                            currentBsMonth = todayBs.month
                            currentAdYear = todayAd.year
                            currentAdMonth = todayAd.month
                        },
                        onToggleCalendar = { isBsView = !isBsView }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CalendarGrid(
                        days = daysList,
                        isBsView = isBsView,
                        selectedDay = selectedDayItem,
                        onSelectDay = { selectedDayItem = it }
                    )
                }
            }

            // Right Panel: Day Detail Inspector
            Card(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    val activeItem = selectedDayItem ?: daysList.firstOrNull { it.isToday } ?: daysList.firstOrNull()
                    if (activeItem != null) {
                        DayDetailsInspector(
                            item = activeItem,
                            context = context,
                            onCopy = { text ->
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
                            },
                            onCalculateAge = { onNavigateToAgeCalc(activeItem.bsDate) }
                        )
                    }
                }
            }
        }
    } else {
        // Phone Layout: Scrollable Vertical Column
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        CalendarNavigationHeader(
                            isBsView = isBsView,
                            bsYear = currentBsYear,
                            bsMonth = currentBsMonth,
                            adYear = currentAdYear,
                            adMonth = currentAdMonth,
                            onPrevYear = {
                                if (isBsView) {
                                    if (currentBsYear > 2000) currentBsYear--
                                } else {
                                    if (currentAdYear > 1944) currentAdYear--
                                }
                            },
                            onNextYear = {
                                if (isBsView) {
                                    if (currentBsYear < 2100) currentBsYear++
                                } else {
                                    if (currentAdYear < 2043) currentAdYear++
                                }
                            },
                            onPrevMonth = {
                                if (isBsView) {
                                    if (currentBsMonth == 1) {
                                        if (currentBsYear > 2000) {
                                            currentBsYear--
                                            currentBsMonth = 12
                                        }
                                    } else {
                                        currentBsMonth--
                                    }
                                } else {
                                    if (currentAdMonth == 1) {
                                        currentAdYear--
                                        currentAdMonth = 12
                                    } else {
                                        currentAdMonth--
                                    }
                                }
                            },
                            onNextMonth = {
                                if (isBsView) {
                                    if (currentBsMonth == 12) {
                                        if (currentBsYear < 2100) {
                                            currentBsYear++
                                            currentBsMonth = 1
                                        }
                                    } else {
                                        currentBsMonth++
                                    }
                                } else {
                                    if (currentAdMonth == 12) {
                                        currentAdYear++
                                        currentAdMonth = 1
                                    } else {
                                        currentAdMonth++
                                    }
                                }
                            },
                            onToday = {
                                currentBsYear = todayBs.year
                                currentBsMonth = todayBs.month
                                currentAdYear = todayAd.year
                                currentAdMonth = todayAd.month
                            },
                            onToggleCalendar = { isBsView = !isBsView }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        CalendarGrid(
                            days = daysList,
                            isBsView = isBsView,
                            selectedDay = selectedDayItem,
                            onSelectDay = { selectedDayItem = it }
                        )
                    }
                }
            }

            // Day Detail Card
            item {
                val activeItem = selectedDayItem ?: daysList.firstOrNull { it.isToday } ?: daysList.firstOrNull()
                if (activeItem != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("day_detail_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DayDetailsInspector(
                                item = activeItem,
                                context = context,
                                onCopy = { text ->
                                    clipboardManager.setText(AnnotatedString(text))
                                    Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
                                },
                                onCalculateAge = { onNavigateToAgeCalc(activeItem.bsDate) }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun CalendarNavigationHeader(
    isBsView: Boolean,
    bsYear: Int,
    bsMonth: Int,
    adYear: Int,
    adMonth: Int,
    onPrevYear: () -> Unit,
    onNextYear: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onToggleCalendar: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Top Switch Row: BS / AD toggle + Today Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = isBsView,
                    onClick = onToggleCalendar,
                    label = { Text("Bikram Sambat (BS)", fontWeight = if (isBsView) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("toggle_bs_chip")
                )
                FilterChip(
                    selected = !isBsView,
                    onClick = onToggleCalendar,
                    label = { Text("Gregorian (AD)", fontWeight = if (!isBsView) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("toggle_ad_chip")
                )
            }

            OutlinedButton(
                onClick = onToday,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("calendar_today_btn")
            ) {
                Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Today", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Month & Year Selector with Navigation Arrows
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevYear, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.KeyboardDoubleArrowLeft, contentDescription = "Prev Year", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onPrevMonth, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month", modifier = Modifier.size(18.dp))
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isBsView) {
                    Text(
                        text = "${NepaliDateUtils.getNepaliMonthNameNe(bsMonth)} $bsYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${NepaliDateUtils.getNepaliMonthNameEn(bsMonth)} $bsYear BS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "${NepaliDateUtils.getEnglishMonthName(adMonth)} $adYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$adYear AD",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNextMonth, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onNextYear, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.KeyboardDoubleArrowRight, contentDescription = "Next Year", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    days: List<CalendarDayItem>,
    isBsView: Boolean,
    selectedDay: CalendarDayItem?,
    onSelectDay: (CalendarDayItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Weekday Header Row (Sunday=0 ... Saturday=6)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NepaliDateUtils.WEEKDAYS_EN.forEachIndexed { index, name ->
                val isSat = (index == 6)
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSat) CoralWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Day Cells (7 columns)
        val rows = days.chunked(7)
        rows.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEach { item ->
                    val isSelected = (selectedDay != null && (
                            if (isBsView) item.bsDate == selectedDay.bsDate else item.adDate == selectedDay.adDate
                            ))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.95f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    item.isToday -> MaterialTheme.colorScheme.primary
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                    item.isWeekend && item.isCurrentMonth -> CoralWarning.copy(alpha = 0.08f)
                                    else -> Color.Transparent
                                }
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectDay(item) }
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${item.dayNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (item.isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    item.isToday -> MaterialTheme.colorScheme.onPrimary
                                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                    !item.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                    item.isWeekend -> CoralWarning
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            // Secondary calendar date below
                            val subDateText = if (isBsView) "${item.adDate.day}" else "${item.bsDate.day}"
                            Text(
                                text = subDateText,
                                fontSize = 9.sp,
                                color = when {
                                    item.isToday -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    !item.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayDetailsInspector(
    item: CalendarDayItem,
    context: Context,
    onCopy: (String) -> Unit,
    onCalculateAge: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Selected Date Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (item.isToday) "Today (आज)" else if (item.isWeekend) "Saturday • Weekly Holiday" else "Normal Working Day",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.isWeekend) CoralWarning else MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = {
                    val copyStr = "${item.bsDate.formattedNe} (${item.bsDate.formattedEn}) = ${item.adDate.formatted}"
                    onCopy(copyStr)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy date", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bikram Sambat Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                .padding(12.dp)
        ) {
            Column {
                Text(text = "Nepali Date (Bikram Sambat – BS)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = item.bsDate.formattedNe, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "${item.bsDate.formattedEn} • ${NepaliDateUtils.WEEKDAYS_FULL_NE[NepaliDateConverter.getDayOfWeek(item.adDate).value % 7]}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Gregorian AD Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(12.dp)
        ) {
            Column {
                Text(text = "English Date (Gregorian – AD)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = item.adDate.formatted, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "${NepaliDateUtils.WEEKDAYS_FULL_EN[NepaliDateConverter.getDayOfWeek(item.adDate).value % 7]} • ISO: ${item.adDate.isoFormatted}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onCalculateAge,
            modifier = Modifier.fillMaxWidth().testTag("calc_age_from_selected_date_btn")
        ) {
            Icon(Icons.Default.Cake, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Calculate Age from this Date")
        }
    }
}

// -------------------------------------------------------------
// 2. DATE CONVERTER VIEW (BS ⇄ AD)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateConverterView(
    todayBs: NepaliDate,
    todayAd: EnglishDate,
    isTablet: Boolean,
    context: Context
) {
    var convertFromBs by remember { mutableStateOf(true) } // true: BS -> AD, false: AD -> BS

    // BS Input selectors
    var inputBsYear by remember { mutableIntStateOf(todayBs.year) }
    var inputBsMonth by remember { mutableIntStateOf(todayBs.month) }
    var inputBsDay by remember { mutableIntStateOf(todayBs.day) }

    // AD Input selectors
    var inputAdYear by remember { mutableIntStateOf(todayAd.year) }
    var inputAdMonth by remember { mutableIntStateOf(todayAd.month) }
    var inputAdDay by remember { mutableIntStateOf(todayAd.day) }

    // Clamp days if month changes
    val maxBsDays = remember(inputBsYear, inputBsMonth) {
        NepaliDateConverter.getDaysInBsMonth(inputBsYear, inputBsMonth)
    }
    LaunchedEffect(maxBsDays) {
        if (inputBsDay > maxBsDays) inputBsDay = maxBsDays
    }

    val maxAdDays = remember(inputAdYear, inputAdMonth) {
        NepaliDateConverter.getDaysInAdMonth(inputAdYear, inputAdMonth)
    }
    LaunchedEffect(maxAdDays) {
        if (inputAdDay > maxAdDays) inputAdDay = maxAdDays
    }

    // Instant Converted Result
    val conversionResult by remember(convertFromBs, inputBsYear, inputBsMonth, inputBsDay, inputAdYear, inputAdMonth, inputAdDay) {
        derivedStateOf {
            try {
                if (convertFromBs) {
                    val bs = NepaliDate(inputBsYear, inputBsMonth, inputBsDay)
                    val ad = NepaliDateConverter.bsToAd(bs)
                    Pair(bs, ad)
                } else {
                    val ad = EnglishDate(inputAdYear, inputAdMonth, inputAdDay)
                    val bs = NepaliDateConverter.adToBs(ad)
                    Pair(bs, ad)
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Conversion Direction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = convertFromBs,
                            onClick = { convertFromBs = true },
                            label = { Text("Bikram Sambat (BS) ➔ English (AD)", fontWeight = if (convertFromBs) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f).testTag("conv_mode_bs_to_ad")
                        )
                        FilterChip(
                            selected = !convertFromBs,
                            onClick = { convertFromBs = false },
                            label = { Text("English (AD) ➔ Bikram Sambat (BS)", fontWeight = if (!convertFromBs) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f).testTag("conv_mode_ad_to_bs")
                        )
                    }
                }
            }
        }

        // Input Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (convertFromBs) "Enter Nepali Date (BS)" else "Enter English Date (AD)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (convertFromBs) {
                        // BS Date Selector Pickers (Year, Month, Day)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // BS Year Dropdown
                            NumberDropdown(
                                label = "Year (वर्ष)",
                                selected = inputBsYear,
                                options = (2000..2100).toList(),
                                onSelect = { inputBsYear = it },
                                modifier = Modifier.weight(1.1f)
                            )
                            // BS Month Dropdown
                            MonthDropdown(
                                label = "Month (महिना)",
                                selected = inputBsMonth,
                                monthNames = NepaliDateUtils.NEPALI_MONTHS_EN,
                                onSelect = { inputBsMonth = it },
                                modifier = Modifier.weight(1.4f)
                            )
                            // BS Day Dropdown
                            NumberDropdown(
                                label = "Day (गते)",
                                selected = inputBsDay,
                                options = (1..maxBsDays).toList(),
                                onSelect = { inputBsDay = it },
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                    } else {
                        // AD Date Selector Pickers (Year, Month, Day)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NumberDropdown(
                                label = "Year",
                                selected = inputAdYear,
                                options = (1944..2043).toList(),
                                onSelect = { inputAdYear = it },
                                modifier = Modifier.weight(1.1f)
                            )
                            MonthDropdown(
                                label = "Month",
                                selected = inputAdMonth,
                                monthNames = NepaliDateUtils.ENGLISH_MONTHS,
                                onSelect = { inputAdMonth = it },
                                modifier = Modifier.weight(1.4f)
                            )
                            NumberDropdown(
                                label = "Day",
                                selected = inputAdDay,
                                options = (1..maxAdDays).toList(),
                                onSelect = { inputAdDay = it },
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (convertFromBs) {
                                    inputBsYear = todayBs.year
                                    inputBsMonth = todayBs.month
                                    inputBsDay = todayBs.day
                                } else {
                                    inputAdYear = todayAd.year
                                    inputAdMonth = todayAd.month
                                    inputAdDay = todayAd.day
                                }
                            }
                        ) {
                            Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Set to Today", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Converted Result Hero Card
        item {
            val result = conversionResult
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("conversion_result_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Conversion Result",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (result != null) {
                            IconButton(
                                onClick = {
                                    val copyText = "${result.first.formattedNe} (${result.first.formattedEn}) = ${result.second.formatted}"
                                    clipboardManager.setText(AnnotatedString(copyText))
                                    Toast.makeText(context, "Copied converted date!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("copy_converted_date_btn")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Converted Date", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (result != null) {
                        val (bsDate, adDate) = result
                        val dayOfWeek = NepaliDateConverter.getDayOfWeek(adDate)
                        val weekdayName = NepaliDateUtils.WEEKDAYS_FULL_EN[dayOfWeek.value % 7]
                        val weekdayNe = NepaliDateUtils.WEEKDAYS_FULL_NE[dayOfWeek.value % 7]

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (convertFromBs) "Converted English Date (AD):" else "Converted Nepali Date (BS):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (convertFromBs) adDate.formatted else bsDate.formattedNe,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (convertFromBs) "$weekdayName • ISO: ${adDate.isoFormatted}" else "${bsDate.formattedEn} • $weekdayNe",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full Equation Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${bsDate.formattedEn}  ⇄  ${adDate.formatted}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "Selected date is outside the valid conversion range (2000-2100 BS / 1943-2043 AD).",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. AGE CALCULATOR & DAYS SINCE BIRTH VIEW
// -------------------------------------------------------------
@Composable
fun AgeCalculatorView(
    viewModel: MornixViewModel,
    todayAd: EnglishDate,
    todayBs: NepaliDate,
    isTablet: Boolean,
    context: Context
) {
    val savedBirthDate by viewModel.savedBirthDate.collectAsState()

    var inputType by remember { mutableStateOf(savedBirthDate?.calendarType ?: "BS") }
    var year by remember { mutableIntStateOf(savedBirthDate?.year ?: 2055) }
    var month by remember { mutableIntStateOf(savedBirthDate?.month ?: 5) }
    var day by remember { mutableIntStateOf(savedBirthDate?.day ?: 15) }

    // If saved birth date changes, restore it
    LaunchedEffect(savedBirthDate) {
        savedBirthDate?.let {
            inputType = it.calendarType
            year = it.year
            month = it.month
            day = it.day
        }
    }

    // Dynamic max days
    val maxDays = remember(inputType, year, month) {
        if (inputType == "BS") {
            NepaliDateConverter.getDaysInBsMonth(year, month)
        } else {
            NepaliDateConverter.getDaysInAdMonth(year, month)
        }
    }
    LaunchedEffect(maxDays) {
        if (day > maxDays) day = maxDays
    }

    // Auto-save whenever user adjusts birth date
    LaunchedEffect(inputType, year, month, day) {
        viewModel.saveBirthDate(inputType, year, month, day)
    }

    // Calculate Age & Days
    val ageResult by remember(inputType, year, month, day, todayAd) {
        derivedStateOf {
            try {
                val dobAd = if (inputType == "BS") {
                    NepaliDateConverter.bsToAd(NepaliDate(year, month, day))
                } else {
                    EnglishDate(year, month, day)
                }
                NepaliDateConverter.calculateAge(dobAd, todayAd)
            } catch (_: Exception) {
                null
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date of Birth Entry Card with Remember/Clear controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
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
                                text = "Your Date of Birth (जन्म मिति)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Saved locally on this device • Automatically remembered",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (savedBirthDate != null) {
                            OutlinedButton(
                                onClick = { viewModel.clearBirthDate() },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("clear_saved_birthdate_btn")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calendar Toggle: BS or AD
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = (inputType == "BS"),
                            onClick = {
                                if (inputType != "BS") {
                                    // Convert current AD to BS
                                    try {
                                        val bs = NepaliDateConverter.adToBs(EnglishDate(year, month, day))
                                        year = bs.year
                                        month = bs.month
                                        day = bs.day
                                    } catch (_: Exception) {
                                        year = 2055
                                        month = 5
                                        day = 15
                                    }
                                    inputType = "BS"
                                }
                            },
                            label = { Text("Bikram Sambat (BS)", fontWeight = if (inputType == "BS") FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f).testTag("dob_bs_chip")
                        )
                        FilterChip(
                            selected = (inputType == "AD"),
                            onClick = {
                                if (inputType != "AD") {
                                    // Convert current BS to AD
                                    try {
                                        val ad = NepaliDateConverter.bsToAd(NepaliDate(year, month, day))
                                        year = ad.year
                                        month = ad.month
                                        day = ad.day
                                    } catch (_: Exception) {
                                        year = 1998
                                        month = 8
                                        day = 31
                                    }
                                    inputType = "AD"
                                }
                            },
                            label = { Text("Gregorian (AD)", fontWeight = if (inputType == "AD") FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f).testTag("dob_ad_chip")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Year, Month, Day Dropdowns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val years = if (inputType == "BS") (2000..todayBs.year).toList().reversed() else (1944..todayAd.year).toList().reversed()
                        val monthNames = if (inputType == "BS") NepaliDateUtils.NEPALI_MONTHS_EN else NepaliDateUtils.ENGLISH_MONTHS

                        NumberDropdown(
                            label = "Year",
                            selected = year,
                            options = years,
                            onSelect = { year = it },
                            modifier = Modifier.weight(1.1f)
                        )
                        MonthDropdown(
                            label = "Month",
                            selected = month,
                            monthNames = monthNames,
                            onSelect = { month = it },
                            modifier = Modifier.weight(1.4f)
                        )
                        NumberDropdown(
                            label = "Day",
                            selected = day,
                            options = (1..maxDays).toList(),
                            onSelect = { day = it },
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                }
            }
        }

        // Prominent Days Since Birth Hero Card
        item {
            val res = ageResult
            if (res != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("days_since_birth_hero_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TOTAL DAYS LIVED",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = String.format(Locale.ENGLISH, "%,d", res.totalDays),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "days on Earth since birth",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Age In Years, Months, Days breakdown
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "${res.years}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(text = "Years", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(text = "•", color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.align(Alignment.CenterVertically))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "${res.months}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(text = "Months", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(text = "•", color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.align(Alignment.CenterVertically))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "${res.days}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(text = "Days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Please enter a valid past birth date.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Secondary Metrics & Next Birthday Card
        item {
            val res = ageResult
            if (res != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Next Birthday & Lifetime Stats",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Next Birthday Countdown
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(EmeraldLive.copy(alpha = 0.12f))
                                .border(1.dp, EmeraldLive.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldLive),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Cake, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (res.nextBirthdayDays == 0L) "🎉 Happy Birthday Today! 🎉" else "In ${res.nextBirthdayDays} days",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldLive
                                    )
                                    Text(
                                        text = "Next Birthday: ${res.nextBirthdayAd.formatted} (${res.nextBirthdayBs.formattedEn})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Lifetime Milestones Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricBox(label = "Total Months", value = String.format(Locale.ENGLISH, "%,d", res.totalMonths), modifier = Modifier.weight(1f))
                            MetricBox(label = "Total Weeks", value = String.format(Locale.ENGLISH, "%,d", res.totalWeeks), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun MetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(12.dp)
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// -------------------------------------------------------------
// HELPERS: DROPDOWN COMPONENTS
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberDropdown(
    label: String,
    selected: Int,
    options: List<Int>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = "$selected",
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 11.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text("$item") },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDropdown(
    label: String,
    selected: Int,
    monthNames: List<String>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val displayName = if (selected in 1..monthNames.size) monthNames[selected - 1] else "$selected"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 11.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            monthNames.forEachIndexed { index, name ->
                val monthIndex = index + 1
                DropdownMenuItem(
                    text = { Text("$monthIndex. $name") },
                    onClick = {
                        onSelect(monthIndex)
                        expanded = false
                    }
                )
            }
        }
    }
}
