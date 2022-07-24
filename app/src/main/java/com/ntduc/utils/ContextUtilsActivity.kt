package com.ntduc.utils

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ntduc.contextutils.showConfirmationDialog
import com.ntduc.contextutils.showDialog
import com.ntduc.contextutils.showMultiPicker
import com.ntduc.contextutils.showSinglePicker
import com.ntduc.toastutils.ToastUtils
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
                ToastUtils(this).showShort(it.toString())
            }, cancelable = false)
        }
        binding.btnShowSinglePicker.setOnClickListener {
            showSinglePicker("Title", listOf("Một", "Hai", "Ba").toTypedArray(), onResponse = {
                ToastUtils(this).showShort(it.toString())
            }, 0)
        }
        binding.btnShowMultiPicker.setOnClickListener {
            showMultiPicker("Title", listOf("Một", "Hai", "Ba").toTypedArray(), onResponse = { index, isChecked ->
                if (isChecked) ToastUtils(this).showShort(index.toString())
            }, listOf(true, true, false).toBooleanArray())
        }
    }
}