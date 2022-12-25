package com.ntduc.utils.sp_utils

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ntduc.contextutils.inflater
import com.ntduc.sharedpreferenceutils.*
import com.ntduc.toastutils.shortToast
import com.ntduc.utils.R
import com.ntduc.utils.databinding.ActivitySpBinding

class SpActivity : AppCompatActivity(), (SharedPreferences, String?) -> Unit {
    companion object {
        private const val KEY_STRING = "KEY_STRING"
    }

    private lateinit var binding: ActivitySpBinding
    private lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpBinding.inflate(inflater)
        setContentView(binding.root)

        sp = getSharedPreferences("ntduc", MODE_PRIVATE)
        sp.registerSharedPreferenceChangeListener(this, this)

        init()
    }

    private fun init() {
        initView()
        initEvent()
    }

    private fun initView() {
        binding.txt.text = sp.getString(KEY_STRING, null)
    }

    private fun initEvent() {
        binding.btnPutString.setOnClickListener {
            sp.putString(KEY_STRING, binding.edt.text.toString())
            binding.txt.text = sp.getString(KEY_STRING, null)
        }
        binding.btnCommitString.setOnClickListener {
            sp.commitString(KEY_STRING, binding.edt.text.toString())
            binding.txt.text = sp.getString(KEY_STRING, null)
        }
        binding.btnClear.setOnClickListener {
            sp.clear()
            binding.txt.text = sp.getString(KEY_STRING, null)
        }
        binding.btnCommitClear.setOnClickListener {
            sp.commitClear()
            binding.txt.text = sp.getString(KEY_STRING, null)
        }
        binding.btnRemove.setOnClickListener {
            sp.remove(KEY_STRING)
            binding.txt.text = sp.getString(KEY_STRING, null)
        }
        binding.btnCommitRemove.setOnClickListener {
            sp.commitRemove(KEY_STRING)
            binding.txt.text = sp.getString(KEY_STRING, null)
        }
    }

    override fun invoke(sharedPreferences: SharedPreferences, key: String?) {
        shortToast(key ?: "null")
    }
}