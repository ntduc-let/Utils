package com.ntduc.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.ntduc.contextutils.*
import com.ntduc.toastutils.Toast
import com.ntduc.toastutils.showShort
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
                showShort(it.toString())
            }, cancelable = false)
        }
        binding.btnShowSinglePicker.setOnClickListener {
            showSinglePicker("Title", listOf("Một", "Hai", "Ba").toTypedArray(), onResponse = {
                showShort(it.toString())
            }, 0)
        }
        binding.btnShowMultiPicker.setOnClickListener {
            showMultiPicker("Title", listOf("Một", "Hai", "Ba").toTypedArray(), onResponse = { index, isChecked ->
                if (isChecked) showShort(index.toString())
            }, listOf(true, true, false).toBooleanArray())
        }

        binding.btnIsLocationEnabled.setOnClickListener {
            showShort("IsLocationEnabled $isLocationEnabled")
        }

        binding.btnDeviceID.setOnClickListener {
            showShort("DeviceID $deviceID")
        }

        binding.btnGetConnectionType.setOnClickListener {
            when(getConnectionType()){
                0 -> showShort("No Connection")
                1 -> showShort("Mobile Data")
                2 -> showShort("Wifi")
                3 -> showShort("VPN")
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
                    0 -> showShort("no telephony manager available")
                    1 -> showShort("unknown telephony")
                    2 -> showShort("2g internet")
                    3 -> showShort("3g internet")
                    4 -> showShort("4g internet")
                    5 -> showShort("5g internet")
                }
            }
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
            showShort("PERMISSION_GRANTED")
        } else {
            showShort("PERMISSION_DENIED")
        }
    }
}