package com.example.data.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

object NepaliDateConverter {

    private val BASE_AD_DATE: LocalDate = LocalDate.of(
        NepaliCalendarData.BASE_AD_YEAR,
        NepaliCalendarData.BASE_AD_MONTH,
        NepaliCalendarData.BASE_AD_DAY
    )

    fun todayAd(): EnglishDate {
        return EnglishDate.fromLocalDate(LocalDate.now())
    }

    fun todayBs(): NepaliDate {
        return adToBs(todayAd())
    }

    fun isBsYearSupported(year: Int): Boolean {
        return year in NepaliCalendarData.MIN_BS_YEAR..NepaliCalendarData.MAX_BS_YEAR
    }

    fun getDaysInBsMonth(year: Int, month: Int): Int {
        if (!isBsYearSupported(year) || month !in 1..12) return 30
        val monthDays = NepaliCalendarData.MONTH_DAYS[year] ?: return 30
        return monthDays[month - 1]
    }

    fun getDaysInAdMonth(year: Int, month: Int): Int {
        if (month !in 1..12) return 30
        return LocalDate.of(year, month, 1).lengthOfMonth()
    }

    fun getDaysInBsYear(year: Int): Int {
        val monthDays = NepaliCalendarData.MONTH_DAYS[year] ?: return 365
        return monthDays.sum()
    }

    fun bsToAd(bsDate: NepaliDate): EnglishDate {
        val year = bsDate.year
        val month = bsDate.month
        val day = bsDate.day

        if (!isBsYearSupported(year)) {
            throw IllegalArgumentException("BS Year $year is outside supported range (2000-2100 BS).")
        }
        val daysInMonth = getDaysInBsMonth(year, month)
        if (day !in 1..daysInMonth) {
            throw IllegalArgumentException("Invalid day $day for BS ${NepaliDateUtils.getNepaliMonthNameEn(month)} $year (has $daysInMonth days).")
        }

        var totalDays = 0L
        for (y in NepaliCalendarData.BASE_BS_YEAR until year) {
            val yDays = NepaliCalendarData.MONTH_DAYS[y] ?: throw IllegalStateException("Missing data for $y")
            totalDays += yDays.sum()
        }
        val currentYearMonthDays = NepaliCalendarData.MONTH_DAYS[year] ?: throw IllegalStateException("Missing data for $year")
        for (m in 0 until (month - 1)) {
            totalDays += currentYearMonthDays[m]
        }
        totalDays += (day - 1)

        val targetLocalDate = BASE_AD_DATE.plusDays(totalDays)
        return EnglishDate.fromLocalDate(targetLocalDate)
    }

    fun adToBs(adDate: EnglishDate): NepaliDate {
        val target = adDate.toLocalDate()
        val totalDaysBetween = ChronoUnit.DAYS.between(BASE_AD_DATE, target)

        if (totalDaysBetween < 0) {
            throw IllegalArgumentException("Date $adDate is before the base supported date (April 14, 1943 / 2000 BS Baishakh 1).")
        }

        var daysLeft = totalDaysBetween
        var bsYear = NepaliCalendarData.BASE_BS_YEAR

        while (true) {
            val daysInYear = getDaysInBsYear(bsYear)
            if (daysLeft < daysInYear) {
                break
            }
            daysLeft -= daysInYear
            bsYear++
            if (bsYear > NepaliCalendarData.MAX_BS_YEAR) {
                throw IllegalArgumentException("Date $adDate exceeds the maximum supported Nepali year (${NepaliCalendarData.MAX_BS_YEAR} BS).")
            }
        }

        var bsMonth = 1
        while (true) {
            val daysInMonth = getDaysInBsMonth(bsYear, bsMonth)
            if (daysLeft < daysInMonth) {
                break
            }
            daysLeft -= daysInMonth
            bsMonth++
        }

        val bsDay = (daysLeft + 1).toInt()
        return NepaliDate(bsYear, bsMonth, bsDay)
    }

    fun getDayOfWeek(date: EnglishDate): DayOfWeek {
        return date.toLocalDate().dayOfWeek
    }

    fun getDayOfWeek(date: NepaliDate): DayOfWeek {
        return bsToAd(date).toLocalDate().dayOfWeek
    }

    fun calculateAge(dobAd: EnglishDate, targetAd: EnglishDate = todayAd()): AgeResult {
        val dob = dobAd.toLocalDate()
        val target = targetAd.toLocalDate()

        if (dob.isAfter(target)) {
            throw IllegalArgumentException("Date of birth cannot be in the future.")
        }

        val period = Period.between(dob, target)
        val totalDays = ChronoUnit.DAYS.between(dob, target)
        val totalMonths = period.years * 12L + period.months
        val totalWeeks = totalDays / 7L

        // Next Birthday Calculation
        var nextBdayYear = target.year
        var nextBdayLocalDate: LocalDate
        try {
            nextBdayLocalDate = LocalDate.of(nextBdayYear, dob.monthValue, dob.dayOfMonth)
        } catch (_: Exception) {
            // Handle Feb 29 on non-leap year
            nextBdayLocalDate = LocalDate.of(nextBdayYear, dob.monthValue, 28)
        }

        if (nextBdayLocalDate.isBefore(target) || nextBdayLocalDate.isEqual(target)) {
            nextBdayYear++
            try {
                nextBdayLocalDate = LocalDate.of(nextBdayYear, dob.monthValue, dob.dayOfMonth)
            } catch (_: Exception) {
                nextBdayLocalDate = LocalDate.of(nextBdayYear, dob.monthValue, 28)
            }
        }

        val nextBirthdayDays = ChronoUnit.DAYS.between(target, nextBdayLocalDate)
        val nextBirthdayAd = EnglishDate.fromLocalDate(nextBdayLocalDate)
        val nextBirthdayBs = adToBs(nextBirthdayAd)

        return AgeResult(
            years = period.years,
            months = period.months,
            days = period.days,
            totalDays = totalDays,
            totalMonths = totalMonths,
            totalWeeks = totalWeeks,
            nextBirthdayDays = nextBirthdayDays,
            nextBirthdayAd = nextBirthdayAd,
            nextBirthdayBs = nextBirthdayBs
        )
    }

    fun generateCalendarMonthBs(year: Int, month: Int): List<CalendarDayItem> {
        val daysInCurrentMonth = getDaysInBsMonth(year, month)
        val firstDayBs = NepaliDate(year, month, 1)
        val firstDayAd = bsToAd(firstDayBs)
        val today = todayBs()

        // In Nepal, Sunday is day 0, Monday 1, ..., Saturday 6
        val firstDayDOW = firstDayAd.toLocalDate().dayOfWeek.value % 7 // Sunday = 0, Monday = 1 ... Saturday = 6

        val items = mutableListOf<CalendarDayItem>()

        // 1. Previous month trailing days
        if (firstDayDOW > 0) {
            val prevMonth = if (month == 1) 12 else month - 1
            val prevYear = if (month == 1) year - 1 else year
            val daysInPrevMonth = getDaysInBsMonth(prevYear, prevMonth)
            for (i in (firstDayDOW - 1) downTo 0) {
                val prevDay = daysInPrevMonth - i
                val prevBs = NepaliDate(prevYear, prevMonth, prevDay)
                val prevAd = bsToAd(prevBs)
                val isSat = (prevAd.toLocalDate().dayOfWeek == DayOfWeek.SATURDAY)
                items.add(
                    CalendarDayItem(
                        dayNumber = prevDay,
                        isCurrentMonth = false,
                        isToday = (prevBs == today),
                        isWeekend = isSat,
                        bsDate = prevBs,
                        adDate = prevAd
                    )
                )
            }
        }

        // 2. Current month days
        for (day in 1..daysInCurrentMonth) {
            val currBs = NepaliDate(year, month, day)
            val currAd = bsToAd(currBs)
            val isSat = (currAd.toLocalDate().dayOfWeek == DayOfWeek.SATURDAY)
            items.add(
                CalendarDayItem(
                    dayNumber = day,
                    isCurrentMonth = true,
                    isToday = (currBs == today),
                    isWeekend = isSat,
                    bsDate = currBs,
                    adDate = currAd
                )
            )
        }

        // 3. Next month leading days to complete grid (up to multiple of 7, min 35 or 42)
        val remaining = (7 - (items.size % 7)) % 7
        val targetSize = if (items.size + remaining <= 35) 35 else 42
        val needed = targetSize - items.size
        if (needed > 0) {
            val nextMonth = if (month == 12) 1 else month + 1
            val nextYear = if (month == 12) year + 1 else year
            for (d in 1..needed) {
                val nextBs = NepaliDate(nextYear, nextMonth, d)
                val nextAd = bsToAd(nextBs)
                val isSat = (nextAd.toLocalDate().dayOfWeek == DayOfWeek.SATURDAY)
                items.add(
                    CalendarDayItem(
                        dayNumber = d,
                        isCurrentMonth = false,
                        isToday = (nextBs == today),
                        isWeekend = isSat,
                        bsDate = nextBs,
                        adDate = nextAd
                    )
                )
            }
        }

        return items
    }

    fun generateCalendarMonthAd(year: Int, month: Int): List<CalendarDayItem> {
        val daysInCurrentMonth = getDaysInAdMonth(year, month)
        val firstDayAd = EnglishDate(year, month, 1)
        val today = todayAd()

        // Sunday = 0, Monday = 1, ..., Saturday = 6
        val firstDayDOW = firstDayAd.toLocalDate().dayOfWeek.value % 7

        val items = mutableListOf<CalendarDayItem>()

        // 1. Previous month trailing days
        if (firstDayDOW > 0) {
            val prevMonth = if (month == 1) 12 else month - 1
            val prevYear = if (month == 1) year - 1 else year
            val daysInPrevMonth = getDaysInAdMonth(prevYear, prevMonth)
            for (i in (firstDayDOW - 1) downTo 0) {
                val prevDay = daysInPrevMonth - i
                val prevAd = EnglishDate(prevYear, prevMonth, prevDay)
                val prevBs = try { adToBs(prevAd) } catch (_: Exception) { NepaliDate(2000, 1, 1) }
                val isSat = (prevAd.toLocalDate().dayOfWeek == DayOfWeek.SATURDAY)
                items.add(
                    CalendarDayItem(
                        dayNumber = prevDay,
                        isCurrentMonth = false,
                        isToday = (prevAd == today),
                        isWeekend = isSat,
                        bsDate = prevBs,
                        adDate = prevAd
                    )
                )
            }
        }

        // 2. Current month days
        for (day in 1..daysInCurrentMonth) {
            val currAd = EnglishDate(year, month, day)
            val currBs = try { adToBs(currAd) } catch (_: Exception) { NepaliDate(2000, 1, 1) }
            val isSat = (currAd.toLocalDate().dayOfWeek == DayOfWeek.SATURDAY)
            items.add(
                CalendarDayItem(
                    dayNumber = day,
                    isCurrentMonth = true,
                    isToday = (currAd == today),
                    isWeekend = isSat,
                    bsDate = currBs,
                    adDate = currAd
                )
            )
        }

        // 3. Next month leading days to complete grid
        val remaining = (7 - (items.size % 7)) % 7
        val targetSize = if (items.size + remaining <= 35) 35 else 42
        val needed = targetSize - items.size
        if (needed > 0) {
            val nextMonth = if (month == 12) 1 else month + 1
            val nextYear = if (month == 12) year + 1 else year
            for (d in 1..needed) {
                val nextAd = EnglishDate(nextYear, nextMonth, d)
                val nextBs = try { adToBs(nextAd) } catch (_: Exception) { NepaliDate(2000, 1, 1) }
                val isSat = (nextAd.toLocalDate().dayOfWeek == DayOfWeek.SATURDAY)
                items.add(
                    CalendarDayItem(
                        dayNumber = d,
                        isCurrentMonth = false,
                        isToday = (nextAd == today),
                        isWeekend = isSat,
                        bsDate = nextBs,
                        adDate = nextAd
                    )
                )
            }
        }

        return items
    }
}
