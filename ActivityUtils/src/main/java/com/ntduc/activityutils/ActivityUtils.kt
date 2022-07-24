package com.ntduc.activityutils

import android.Manifest.permission.WRITE_SETTINGS
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.N
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

//Ẩn BottomBar
fun Activity.hideBottomBar() {
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

//Hiển thị BottomBar
fun Activity.showBottomBar() {
    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).show(WindowInsetsCompat.Type.navigationBars())
}

//Ẩn StatusBar
fun Activity.hideStatusBar() {
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

//Hiển thị StatusBar
fun Activity.showStatusBar() {
    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).show(WindowInsetsCompat.Type.statusBars())
}

//Bật chế độ full màn hình
fun Activity.enterFullScreenMode() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

//Thoát chế độ full màn hình
fun Activity.exitFullScreenMode() {
    WindowCompat.setDecorFitsSystemWindows(window, true)
    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).show(WindowInsetsCompat.Type.systemBars())
}

//Tắt chụp màn hình
fun Activity.addSecureFlag() {
    window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
}

//Mở chụp màn hình
fun Activity.clearSecureFlag() {
    window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
}

//Hiển thị bàn phím
fun Activity.showKeyboard(toFocus: View) {
    toFocus.requestFocus()
    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(toFocus, InputMethodManager.SHOW_IMPLICIT)
}

//Ẩn bàn phím
fun Activity.hideKeyboard() {
    if (currentFocus != null) {
        val inputMethodManager = getSystemService(
            Context
                .INPUT_METHOD_SERVICE
        ) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    }
}

//Check PIP
val Activity.supportsPictureInPicture: Boolean
    get() {
        return SDK_INT >= N && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }

//Thay đổi độ sáng
var Activity.brightness: Float?
    get() = this.window?.attributes?.screenBrightness
    set(value) {
        val window = this.window
        val layoutParams = window.attributes
        layoutParams?.screenBrightness = value //0 is turned off, 1 is full brightness
        window?.attributes = layoutParams
    }

// Chiều cao StatusBar (px)
val Activity.getStatusBarHeight: Int
    get() {
        val rect = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rect)
        return rect.top
    }

//Kích thước màn hình (px)
val Activity.displaySizePixels: Point
    get() {
        var display = this.windowManager.defaultDisplay
        if (SDK_INT >= Build.VERSION_CODES.R) {
            display = this.display
        }
        return DisplayMetrics()
            .apply {
                display.getRealMetrics(this)
            }.let {
                Point(it.widthPixels, it.heightPixels)
            }
    }

//Đặt màu StatusBar
fun Activity.setStatusBarColor(@ColorRes color: Int) {
    window.statusBarColor = ContextCompat.getColor(this, color)
}

//Đặt màu NavigationBar
fun Activity.setNavigationBarColor(@ColorRes color: Int) {
    window.navigationBarColor = ContextCompat.getColor(this, color)
}

//Đặt màu dải phân cách NavigationBar
@RequiresApi(api = Build.VERSION_CODES.P)
fun Activity.setNavigationBarDividerColor(@ColorRes color: Int) {
    window.navigationBarDividerColor = ContextCompat.getColor(this, color)
}

// Restart Activity
inline fun Activity.restart(intentBuilder: Intent.() -> Unit = {}) {
    val i = Intent(this, this::class.java)
    val oldExtras = intent.extras
    if (oldExtras != null)
        i.putExtras(oldExtras)
    i.intentBuilder()
    startActivity(i)
    finish()
}

//Chỉnh thời gian tắt màn hình
inline var Context.sleepDuration: Int
    @RequiresPermission(WRITE_SETTINGS)
    set(value) {
        Settings.System.putInt(
            this.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            value
        )
    }
    get() {
        return try {
            Settings.System.getInt(
                this.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT
            )
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            -123
        }
    }

//Khoá xoay màn hình
fun Activity.lockOrientation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
}

//Mở khoá xoay màn hình
fun Activity.unlockScreenOrientation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
}

//Khoá hướng màn hình hiện tại
fun Activity.lockCurrentScreenOrientation() {
    requestedOrientation = when (resources.configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }
}

// Uri --> Bitmap
fun Activity.getBitmapFromUri(uri: Uri): Bitmap? {
    return contentResolver.openInputStream(uri)?.use {
        return@use BitmapFactory.decodeStream(it)
    }
}

//Setup toolbar
fun AppCompatActivity.setupToolbar(
    toolbar: Toolbar,
    displayHomeAsUpEnabled: Boolean = true,
    displayShowHomeEnabled: Boolean = true,
    displayShowTitleEnabled: Boolean = false,
    showUpArrowAsCloseIcon: Boolean = false,
    @DrawableRes closeIconDrawableRes: Int? = null
) {
    setSupportActionBar(toolbar)
    supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled)
        setDisplayShowHomeEnabled(displayShowHomeEnabled)
        setDisplayShowTitleEnabled(displayShowTitleEnabled)

        if (showUpArrowAsCloseIcon && closeIconDrawableRes != null) {
            setHomeAsUpIndicator(
                AppCompatResources.getDrawable(
                    this@setupToolbar,
                    closeIconDrawableRes
                )
            )
        }
    }
}

//Hiển thị BackButton Toolbar
fun AppCompatActivity.showBackButton() {
    this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
}

//Ẩn BackButton Toolbar
fun AppCompatActivity.hideBackButton() {
    this.supportActionBar?.setDisplayHomeAsUpEnabled(false)
}

//Ẩn Toolbar
fun AppCompatActivity.hideToolbar() {
    this.supportActionBar?.hide()
}

//Hiển thị Toolbar
fun AppCompatActivity.showToolbar() {
    this.supportActionBar?.show()
}

//Đặt Title Toolbar
fun AppCompatActivity.setToolbarTitle(@StringRes title: Int) {
    supportActionBar?.setTitle(title)
}

//Đặt Title Toolbar
fun AppCompatActivity.setToolbarTitle(title: String) {
    supportActionBar?.title = title
}

fun AppCompatActivity.customToolbarDrawable(drawable: Drawable?) {
    supportActionBar?.setBackgroundDrawable(drawable)
}

fun AppCompatActivity.customBackButton(drawable: Drawable?) {
    supportActionBar?.setHomeAsUpIndicator(drawable)
}

fun AppCompatActivity.customBackButton(drawableResource: Int) {
    supportActionBar?.setHomeAsUpIndicator(drawableResource)
}

//Bật tính năng luôn cho màn hình sáng
fun Activity.keepScreenOn() {
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

//Tắt tính năng Luôn cho màn hình sáng
fun Activity.keepScreenOFF() {
    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}