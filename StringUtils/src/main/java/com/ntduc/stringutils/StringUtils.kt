package com.ntduc.stringutils

import android.graphics.Color
import android.graphics.Typeface
import android.text.*
import android.text.style.*
import android.util.Patterns
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import java.util.*
import java.util.regex.Pattern

val String.isAlphanumeric get() = matches("^[a-zA-Z0-9]*$".toRegex())

val String.isAlphabetic get() = matches("^[a-zA-Z]*$".toRegex())

val String.isNumeric get() = matches("^[0-9]*$".toRegex())

val String.isIP: Boolean
    get() {
        val p = Pattern.compile(
            "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}"
        )
        val m = p.matcher(this)
        return m.find()
    }


fun String.isHttp() = this.matches(Regex("(http|https)://[^\\s]*"))

fun CharSequence.isEmail() = isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.asFile() = File(this)

fun String.saveToFile(file: File) = FileOutputStream(file).bufferedWriter().use {
    it.write(this)
    it.flush()
    it.close()
}

fun String.convertToCamelCase(): String {
    var titleText = ""
    if (this.isNotEmpty()) {
        val words = this.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        words.filterNot { it.isEmpty() }
            .map { it.substring(0, 1).uppercase() + it.substring(1).lowercase() }
            .forEach { titleText += "$it " }
    }
    return titleText.trim { it <= ' ' }
}

fun String.ellipsize(at: Int): String {
    if (this.length > at) {
        return this.substring(0, at) + "..."
    }
    return this
}

val String?.asDouble: Double
    get() = if (TextUtils.isEmpty(this)) 0.0 else try {
        this!!.toDouble()
    } catch (e: Exception) {
        0.0
    }

val String?.asInt: Int
    get() = if (TextUtils.isEmpty(this)) 0 else try {
        this!!.toInt()
    } catch (e: Exception) {
        0
    }

val String?.asFloat: Float
    get() = if (TextUtils.isEmpty(this)) 0f else try {
        this!!.toFloat()
    } catch (e: Exception) {
        0f
    }

val CharSequence?.asInt: Int
    get() = toString().asInt

val CharSequence?.asFloat: Float
    get() = toString().asFloat

val CharSequence?.asDouble: Double
    get() = toString().asDouble

fun charToByte(c: Char): Byte {
    return "0123456789ABCDEF".indexOf(c).toByte()
}

fun String.convertToBytes(): ByteArray {
    if (this == "") {
        return ByteArray(0)
    }
    val newHexString = this.trim().uppercase()
    val length = newHexString.length / 2
    val hexChars = newHexString.toCharArray()
    val d = ByteArray(length)
    for (i in 0 until length) {
        val pos = i * 2
        d[i] =
            (charToByte(hexChars[pos]).toInt() shl 4 or charToByte(hexChars[pos + 1]).toInt()).toByte()
    }
    return d
}

fun CharSequence.setBackgroundColor(color: Int): CharSequence {
    val ss = SpannableString(this)
    ss.setSpan(BackgroundColorSpan(color), 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return ss
}

fun CharSequence.setForegroundColor(color: Int): CharSequence {
    val ss = SpannableString(this)
    ss.setSpan(ForegroundColorSpan(color), 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return ss
}

fun String.highlight(
    key: String = this,
    underline: Boolean = false,
    strikeLine: Boolean = false,
    bold: Boolean = false,
    italic: Boolean = false,
    color: Int? = null
): SpannableString {
    val ss = SpannableString(this)

    var mKey = key
    var startIndex = ss.toString().indexOf(key)
    if (startIndex == -1){
        mKey = this
        startIndex = 0
    }
    ss.setSpan(object : ClickableSpan() {
        override fun updateDrawState(ds: TextPaint) {
            if (color != null) {
                ds.color = color
                ds.bgColor = Color.TRANSPARENT
            }
        }

        override fun onClick(widget: View) {}
    }, startIndex, startIndex + mKey.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    if (underline) {
        ss.setSpan(UnderlineSpan(), startIndex, startIndex + mKey.length, 0)
    }
    if (strikeLine) {
        ss.setSpan(StrikethroughSpan(), startIndex, startIndex + mKey.length, 0)
    }
    if (bold) {
        ss.setSpan(StyleSpan(Typeface.BOLD), startIndex, startIndex + mKey.length, 0)
    }
    if (italic) {
        ss.setSpan(StyleSpan(Typeface.ITALIC), startIndex, startIndex + mKey.length, 0)
    }
    return ss
}

fun String.remove(value: String, ignoreCase: Boolean = false): String = replace(value, "", ignoreCase)

private const val CHARACTERS: String = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
private val random: Random = SecureRandom()
fun randomString(length: Int): String {
    if (length <= 0) return ""

    return buildString {
        repeat((1..length).count()) {
            val selection = random.nextInt(CHARACTERS.length)
            val character = CHARACTERS[selection]
            append(character)
        }
    }
}
