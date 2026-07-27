package com.parsaplanner.app.util

import java.time.LocalDate

/**
 * Lightweight Gregorian <-> Jalali (Shamsi) converter.
 * No external dependency needed for the core math — this keeps the app
 * working fully offline and avoids library-version issues.
 */
object JalaliCalendar {

    private val persianMonthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    private val englishMonthNames = listOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"
    )

    private fun isPersianLocale(): Boolean =
        java.util.Locale.getDefault().language == "fa"

    private val weekDayNames = listOf(
        "شنبه", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
    )

    private val weekDayNamesEn = listOf(
        "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
    )

    data class JalaliDate(val year: Int, val month: Int, val day: Int) {
        fun formatted(): String {
            val names = if (JalaliCalendar.isPersianLocale()) JalaliCalendar.persianMonthNames else JalaliCalendar.englishMonthNames
            return "${day} ${names[month - 1]} ${year}"
        }
    }

    fun gregorianToJalali(date: LocalDate): JalaliDate {
        val gy = date.year
        val gm = date.monthValue
        val gd = date.dayOfMonth

        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
                ((gy2 + 399) / 400) + gd + gDaysInMonth[gm - 1]
        if (isGregorianLeap(gy) && gm > 2) days += 1

        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }

        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return JalaliDate(jy, jm, jd)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): LocalDate {
        var gy = if (jy > 979) 1600 else 621
        var jy2 = if (jy > 979) jy - 979 else jy

        var days = 365 * jy2 + (jy2 / 33) * 8 + ((jy2 % 33 + 3) / 4) +
                78 + jd + if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30 + 186)

        gy += 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            gy += 100 * (--days / 36524)
            days %= 36524
            if (days >= 365) days += 1
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }

        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gd = days + 1
        var gm = 0
        val monthDays = gDaysInMonth.copyOf()
        if (isGregorianLeap(gy)) monthDays[1] = 29
        for (i in monthDays.indices) {
            if (gd <= monthDays[i]) {
                gm = i + 1
                break
            }
            gd -= monthDays[i]
        }
        return LocalDate.of(gy, gm, gd.toInt())
    }

    private fun isGregorianLeap(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    fun weekDayName(date: LocalDate): String {
        // LocalDate.dayOfWeek: MONDAY=1..SUNDAY=7; Iranian week starts Saturday
        val isoIndex = date.dayOfWeek.value % 7 // SUNDAY(7)->0 ... SATURDAY(6)->6
        val mapToIranianOrder = (isoIndex + 1) % 7
        val names = if (isPersianLocale()) weekDayNames else weekDayNamesEn
        return names[mapToIranianOrder]
    }
}
