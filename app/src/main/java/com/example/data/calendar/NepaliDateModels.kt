package com.example.data.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

data class NepaliDate(
    val year: Int,
    val month: Int, // 1 to 12 (1 = Baishakh, 12 = Chaitra)
    val day: Int
) : Comparable<NepaliDate> {
    val monthNameEn: String get() = NepaliDateUtils.getNepaliMonthNameEn(month)
    val monthNameNe: String get() = NepaliDateUtils.getNepaliMonthNameNe(month)
    val formattedEn: String get() = "$day $monthNameEn $year BS"
    val formattedNe: String get() = "$day $monthNameNe $year बि.सं."
    val isoFormatted: String get() = String.format("%04d-%02d-%02d", year, month, day)

    override fun compareTo(other: NepaliDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }
}

data class EnglishDate(
    val year: Int,
    val month: Int, // 1 to 12 (1 = January, 12 = December)
    val day: Int
) : Comparable<EnglishDate> {
    val monthName: String get() = NepaliDateUtils.getEnglishMonthName(month)
    val formatted: String get() = "$monthName $day, $year AD"
    val isoFormatted: String get() = String.format("%04d-%02d-%02d", year, month, day)

    fun toLocalDate(): LocalDate = LocalDate.of(year, month, day)

    companion object {
        fun fromLocalDate(date: LocalDate): EnglishDate =
            EnglishDate(date.year, date.monthValue, date.dayOfMonth)
    }

    override fun compareTo(other: EnglishDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }
}

data class CalendarDayItem(
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isWeekend: Boolean, // Saturday in Nepal
    val bsDate: NepaliDate,
    val adDate: EnglishDate
)

data class AgeResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val totalMonths: Long,
    val totalWeeks: Long,
    val nextBirthdayDays: Long,
    val nextBirthdayAd: EnglishDate,
    val nextBirthdayBs: NepaliDate
)

data class SavedBirthDate(
    val calendarType: String, // "BS" or "AD"
    val year: Int,
    val month: Int,
    val day: Int
)

object NepaliDateUtils {
    val NEPALI_MONTHS_EN = listOf(
        "Baishakh", "Jestha", "Ashadh", "Shrawan",
        "Bhadra", "Ashwin", "Kartik", "Mangshir",
        "Poush", "Magh", "Falgun", "Chaitra"
    )

    val NEPALI_MONTHS_NE = listOf(
        "बैशाख", "जेठ", "असार", "साउन",
        "भदौ", "असोज", "कात्तिक", "मंसिर",
        "पुस", "माघ", "फागुन", "चैत"
    )

    val ENGLISH_MONTHS = listOf(
        "January", "February", "March", "April",
        "May", "June", "July", "August",
        "September", "October", "November", "December"
    )

    val WEEKDAYS_EN = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val WEEKDAYS_FULL_EN = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val WEEKDAYS_NE = listOf("आइत", "सोम", "मंगल", "बुध", "बिहि", "शुक्र", "शनि")
    val WEEKDAYS_FULL_NE = listOf("आइतबार", "सोमबार", "मंगलबार", "बुधबार", "बिहिबार", "शुक्रबार", "शनिबार")

    fun getNepaliMonthNameEn(month: Int): String {
        return if (month in 1..12) NEPALI_MONTHS_EN[month - 1] else "Month $month"
    }

    fun getNepaliMonthNameNe(month: Int): String {
        return if (month in 1..12) NEPALI_MONTHS_NE[month - 1] else "महिना $month"
    }

    fun getEnglishMonthName(month: Int): String {
        return if (month in 1..12) ENGLISH_MONTHS[month - 1] else "Month $month"
    }
}
