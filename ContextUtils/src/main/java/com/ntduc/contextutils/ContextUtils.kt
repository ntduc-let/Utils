package com.ntduc.contextutils

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.IntRange
import android.provider.Settings

val Context.isLocationEnabled: Boolean
    get() = (getSystemService(Context.LOCATION_SERVICE) as LocationManager?)?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ?: false

val Context.deviceID
    @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)

/**
 * 0 = no connection info available
 * 1 = mobile data
 * 2 = wifi
 * 3 = vpn
 * @receiver Context
 * @return Int
 */
@IntRange(from = 0, to = 3)
fun Context.getConnectionType(): Int {
    var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi; 3: vpn
    val cm = connectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        cm?.run {
            getNetworkCapabilities(cm.activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        result = 2
                    }
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        result = 1
                    }
                    hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                        result = 3
                    }
                }
            }
        }
    } else {
        cm?.run {
            activeNetworkInfo?.run {
                when (type) {
                    ConnectivityManager.TYPE_WIFI -> {
                        result = 2
                    }
                    ConnectivityManager.TYPE_MOBILE -> {
                        result = 1
                    }
                    ConnectivityManager.TYPE_VPN -> {
                        result = 3
                    }
                }
            }
        }
    }
    return result
}
