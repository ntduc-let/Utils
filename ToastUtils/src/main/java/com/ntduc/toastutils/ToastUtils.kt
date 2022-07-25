package com.ntduc.toastutils

import android.content.Context

fun Context.showShort(resId: Int) {
    Toast(this).showShort(resId)
}

fun Context.showShort(text: String) {
    Toast(this).showShort(text)
}

fun Context.showLong(resId: Int) {
    Toast(this).showLong(resId)
}

fun Context.showLong(text: String) {
    Toast(this).showLong(text)
}