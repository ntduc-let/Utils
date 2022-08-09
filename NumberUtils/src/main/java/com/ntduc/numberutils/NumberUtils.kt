package com.ntduc.numberutils

import androidx.annotation.IntRange
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.random.Random

val Int.length get() = toString().length

//UCLN
fun Int.gcd(n: Int): Int {
    return if (n == 0) this else n.gcd(this % n)
}

//1 -> "01", 10 -> "10"
fun Int.twoDigitTime() = if (this < 10) "0" + toString() else toString()

//C -> F
fun Double.celToFah(): Double = (this * 1.8) + 32

//F -> C
fun Double.fahToCel(): Double = (this - 32) * 5 / 9

fun Number.round(@IntRange(from = 1L) decimalCount: Int): String {
    val expression = StringBuilder().append("#.")
    repeat((1..decimalCount).count()) { expression.append("#") }
    val formatter = DecimalFormat(expression.toString())
    formatter.roundingMode = RoundingMode.HALF_UP
    return formatter.format(this)
}

fun Int.forEach(callback: (i: Int) -> Unit) {
    for (i in 0 until this) {
        callback(i)
    }
}

const val BYTES_TO_KB: Long = 1024
const val BYTES_TO_MB = BYTES_TO_KB * 1024
const val BYTES_TO_GB = BYTES_TO_MB * 1024
const val BYTES_TO_TB = BYTES_TO_GB * 1024

fun Long.formatBytes(): String {
    if (this <= 0)
        return "0 bytes"

    return when {
        this / BYTES_TO_TB > 0 -> String.format("%.2f TB", this / BYTES_TO_TB.toFloat())
        this / BYTES_TO_GB > 0 -> String.format("%.2f GB", this / BYTES_TO_GB.toFloat())
        this / BYTES_TO_MB > 0 -> String.format("%.2f MB", this / BYTES_TO_MB.toFloat())
        this / BYTES_TO_KB > 0 -> String.format("%.2f KB", this / BYTES_TO_KB.toFloat())
        else -> "$this bytes"
    }
}

fun randomNumber(fromNo: Int = 0, toNo: Int = 1000, noToBeGenerated: Int = 20) =
    List(noToBeGenerated) { Random.nextInt(fromNo, toNo) }

fun randomNumber(fromNo: Double = 0.0, toNo: Double = 1000.0, noToBeGenerated: Int = 20) =
    List(noToBeGenerated) { Random.nextDouble(fromNo, toNo) }