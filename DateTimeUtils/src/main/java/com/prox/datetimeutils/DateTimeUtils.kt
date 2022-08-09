package com.prox.datetimeutils

import android.annotation.SuppressLint
import android.text.format.DateUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

val currentDate get() = Date(System.currentTimeMillis())

val Date.calendar: Calendar
    get() {
        val calendar = currentCalendar
        calendar.time = this
        return calendar
    }

val Calendar.year: Int
    get() = get(Calendar.YEAR)

val Calendar.dayOfMonth: Int
    get() = get(Calendar.DAY_OF_MONTH)

val Calendar.month: Int
    get() = get(Calendar.MONTH)

val Calendar.hour: Int
    get() = get(Calendar.HOUR)

val Calendar.hourOfDay: Int
    get() = get(Calendar.HOUR_OF_DAY)

val Calendar.minute: Int
    get() = get(Calendar.MINUTE)

val Calendar.second: Int
    get() = get(Calendar.SECOND)

fun Date.isFuture(): Boolean {
    return !Date().before(this)
}

fun Date.isPast(): Boolean {
    return Date().before(this)
}

fun Date.isToday(): Boolean {
    return DateUtils.isToday(this.time)
}

fun Date.isYesterday(): Boolean {
    return DateUtils.isToday(this.time + DateUtils.DAY_IN_MILLIS)
}

fun Date.isTomorrow(): Boolean {
    return DateUtils.isToday(this.time - DateUtils.DAY_IN_MILLIS)
}

fun Date.reset(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

fun Date.getAge(): Int {
    val birthday = Calendar.getInstance()
    birthday.time = this
    val today = Calendar.getInstance()

    var age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < birthday.get(Calendar.DAY_OF_YEAR)) {
        age -= 1
    }
    return age
}

private val DATEFORMAT = "dd-MM-yyyy HH:mm:ss"

@SuppressLint("SimpleDateFormat")
fun stringDateToDate(StrDate: String): Date? {
    var dateToReturn: Date? = null
    val dateFormat = SimpleDateFormat(DATEFORMAT)
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    try {
        dateToReturn = dateFormat.parse(StrDate)
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    return dateToReturn
}

fun timeAsMillis(hours: Int, minutes: Int, seconds: Int): Long {
    return TimeUnit.HOURS.toMillis(hours.toLong()) +
            TimeUnit.MINUTES.toMillis(minutes.toLong()) +
            TimeUnit.SECONDS.toMillis(seconds.toLong())
}

fun extractHours(millis: Long): Long {
    return millis / (1000 * 60 * 60)
}

fun extractMinutes(millis: Long): Long {
    return millis / (1000 * 60) % 60
}

fun extractSeconds(millis: Long): Long {
    return millis / 1000 % 60
}

val currentMillis: Long
    get() = System.currentTimeMillis()

val currentCalendar: Calendar
    get() = Calendar.getInstance()

fun convertDate(date: String, defaultFormat: String, formatWanted: String): String {
    val format1 = SimpleDateFormat(defaultFormat, Locale.getDefault())
    val format2 = SimpleDateFormat(formatWanted, Locale.getDefault())
    return try {
        format2.format(format1.parse(date) ?: date)
    } catch (e: ParseException) {
        date
    }
}

fun getDateTimeFromMillis(millis: Long, dateFormat: String): String = SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date(millis))

const val TIME_12HOUR = "hh:mm:ss a"
const val TIME_24HOUR = "HH:mm:ss"

fun get24HourCurTime(): String {
    return getDateTimeFromMillis(
        currentMillis,
        TIME_24HOUR
    )
}

fun get12HourCurTime(): String {
    return getDateTimeFromMillis(
        currentMillis,
        TIME_12HOUR
    )
}

fun getCurDate(format: String): String {
    return getDateTimeFromMillis(
        currentMillis,
        format
    )
}

fun convert24HoursTimeTo12HoursTime(date: String): String = convertDate(date, TIME_24HOUR, TIME_12HOUR)

fun convert12HoursTimeTo24HoursTime(date: String): String = convertDate(date, TIME_12HOUR, TIME_24HOUR)