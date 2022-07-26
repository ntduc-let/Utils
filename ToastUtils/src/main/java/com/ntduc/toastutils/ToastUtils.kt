package com.ntduc.toastutils

import android.content.Context

fun Context.shortToast(resId: Int) {
    Toast(this).showShort(resId)
}

fun Context.shortToast(text: String) {
    Toast(this).showShort(text)
}

fun Context.longToast(resId: Int) {
    Toast(this).showLong(resId)
}

fun Context.longToast(text: String) {
    Toast(this).showLong(text)
}