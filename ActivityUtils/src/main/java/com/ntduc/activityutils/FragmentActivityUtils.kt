package com.ntduc.activityutils

import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment


//Đặt màu StatusBar
fun Fragment.setStatusBarColor(@ColorRes color: Int) {
    requireActivity().setStatusBarColor(color)
}

// Chiều cao StatusBar (px)
val Fragment.getStatusBarHeight: Int
    get() {
        return requireActivity().getStatusBarHeight
    }

//Ẩn StatusBar
fun Fragment.hideStatusBar() {
    requireActivity().hideStatusBar()
}

//Hiển thị StatusBar
fun Fragment.showStatusBar() {
    requireActivity().showStatusBar()
}

//Ẩn NavigationBar
fun Fragment.hideNavigationBar() {
    requireActivity().hideNavigationBar()
}

//Show NavigationBar
fun Fragment.showNavigationBar() {
    requireActivity().showNavigationBar()
}

// Chiều cao NavigationBar (px)
val Fragment.getNavigationBarHeight: Int
    get() {
        return requireActivity().getNavigationBarHeight
    }

//Đặt màu NavigationBar
fun Fragment.setNavigationBarColor(@ColorRes color: Int) {
    requireActivity().setNavigationBarColor(color)
}

//Đặt màu dải phân cách NavigationBar
@RequiresApi(api = Build.VERSION_CODES.P)
fun Fragment.setNavigationBarDividerColor(@ColorRes color: Int) {
    requireActivity().setNavigationBarDividerColor(color)
}


//Bật chế độ full màn hình
fun Fragment.enterFullScreenMode() {
    requireActivity().enterFullScreenMode()
}

//Thoát chế độ full màn hình
fun Fragment.exitFullScreenMode() {
    requireActivity().exitFullScreenMode()
}


//Tắt chụp màn hình
fun Fragment.addSecureFlag() {
    requireActivity().addSecureFlag()
}

//Mở chụp màn hình
fun Fragment.clearSecureFlag() {
    requireActivity().clearSecureFlag()
}


//Hiển thị bàn phím
fun Fragment.showKeyboard(toFocus: View) {
    requireActivity().showKeyboard(toFocus)
}

//Ẩn bàn phím
fun Fragment.hideKeyboard() {
    requireActivity().hideKeyboard()
}


//Thay đổi độ sáng
var Fragment.brightness: Float?
    get() = requireActivity().brightness
    set(value) {
        requireActivity().brightness = value
    }

//Bật tính năng luôn cho màn hình sáng
fun Fragment.keepScreenOn() {
    requireActivity().keepScreenOn()
}

//Tắt tính năng Luôn cho màn hình sáng
fun Fragment.keepScreenOFF() {
    requireActivity().keepScreenOFF()
}


//Khoá xoay màn hình
fun Fragment.lockOrientation() {
    requireActivity().lockOrientation()
}

//Mở khoá xoay màn hình
fun Fragment.unlockScreenOrientation() {
    requireActivity().unlockScreenOrientation()
}

//Khoá hướng màn hình hiện tại
fun Fragment.lockCurrentScreenOrientation() {
    requireActivity().lockCurrentScreenOrientation()
}


//Kích thước Activity (px)
val Fragment.displaySizePixels: Point
    get() {
        return requireActivity().displaySizePixels
    }

// Restart Activity
inline fun Fragment.restart(intentBuilder: Intent.() -> Unit = {}) {
    requireActivity().restart(intentBuilder)
}