package com.ntduc.utils.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.ntduc.contextutils.*
import com.ntduc.toastutils.shortToast
import com.ntduc.utils.databinding.ActivityContextUtilsBinding

class ContextUtilsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityContextUtilsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnShowDialog.setOnClickListener {
            showDialog("Title", "showDialog")
        }
        binding.btnShowConfirmationDialog.setOnClickListener {
            showConfirmationDialog("", "showDialog", onResponse = {
                shortToast(it.toString())
            }, cancelable = false)
        }
        binding.btnShowSinglePicker.setOnClickListener {
            showSinglePicker("Title", listOf("Một", "Hai", "Ba").toTypedArray(), onResponse = {
                shortToast(it.toString())
            }, 0)
        }
        binding.btnShowMultiPicker.setOnClickListener {
            showMultiPicker("Title", listOf("Một", "Hai", "Ba").toTypedArray(), onResponse = { index, isChecked ->
                if (isChecked) shortToast(index.toString())
            }, listOf(true, true, false).toBooleanArray())
        }

        binding.btnIsLocationEnabled.setOnClickListener {
            shortToast("IsLocationEnabled $isLocationEnabled")
        }

        binding.btnDeviceID.setOnClickListener {
            shortToast("DeviceID $deviceID")
        }

        binding.btnGetConnectionType.setOnClickListener {
            when(getConnectionType()){
                0 -> shortToast("No Connection")
                1 -> shortToast("Mobile Data")
                2 -> shortToast("Wifi")
                3 -> shortToast("VPN")
            }
        }

        binding.btnDeviceNetworkType.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this,
                    listOf(Manifest.permission.READ_PHONE_STATE).toTypedArray(), 100);
                return@setOnClickListener
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when(deviceNetworkType()){
                    0 -> shortToast("no telephony manager available")
                    1 -> shortToast("unknown telephony")
                    2 -> shortToast("2g internet")
                    3 -> shortToast("3g internet")
                    4 -> shortToast("4g internet")
                    5 -> shortToast("5g internet")
                }
            }
        }

        binding.btnOpenEmail.setOnClickListener {
            openEmail {
                shortToast("openEmail")
            }
        }

        binding.btnSendEmail.setOnClickListener {
            sendEmail(listOf("savatar2204@gmail.com", "savatar2205@gmail.com").toTypedArray(), "subject", "text", onCantHandleAction = {
                shortToast("sendEmail")
            })
        }

        binding.btnSendSMS.setOnClickListener {
            sendSMS("0813615988")
        }

        binding.btnWatchYoutube.setOnClickListener {
            watchYoutube("LCAHVao0kkE")
        }

        binding.btnOpenGoogleMaps.setOnClickListener {
            openGoogleMaps("Hà Nội")
        }

        binding.btnGetTextFromClipboard.setOnClickListener {
            shortToast(getTextFromClipboard().toString())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            shortToast("PERMISSION_GRANTED")
        } else {
            shortToast("PERMISSION_DENIED")
        }
    }
}