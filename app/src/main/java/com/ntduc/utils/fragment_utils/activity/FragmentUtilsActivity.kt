package com.ntduc.utils.fragment_utils.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.ntduc.contextutils.inflater
import com.ntduc.datetimeutils.currentMillis
import com.ntduc.fragmentutils.*
import com.ntduc.toastutils.shortToast
import com.ntduc.utils.databinding.ActivityFragmentUtilsBinding
import com.ntduc.utils.fragment_utils.fragment.DefaultFragment


class FragmentUtilsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFragmentUtilsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFragmentUtilsBinding.inflate(inflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        initView()
        initEvent()
    }

    private fun initView() {
        binding.note.text = backStackCount.toString()
    }

    private fun initEvent() {
        supportFragmentManager.addOnBackStackChangedListener {
            binding.note.text = backStackCount.toString()
        }

        binding.addFragment.setOnClickListener {
            val currentTime = currentMillis
            val fragment = DefaultFragment().newInstance(currentTime.toString())
            addFragment(
                fragment = fragment,
                tag = currentTime.toString(),
                layoutId = binding.container.id,
                isAddStack = true
            )
        }

        binding.replaceFragment.setOnClickListener {
            val currentTime = currentMillis
            val fragment = DefaultFragment().newInstance(currentTime.toString())
            replaceFragment(
                fragment = fragment,
                tag = currentTime.toString(),
                layoutId = binding.container.id,
                isAddStack = true
            )
        }

        binding.hideFragment.setOnClickListener {
            val currentFragment = currentFragment(binding.container.id)
            if (currentFragment != null) {
                hideFragment(currentFragment)
            } else {
                shortToast("currentFragment null")
            }
        }

        binding.showFragment.setOnClickListener {
            val currentFragment = currentFragment(binding.container.id)
            if (currentFragment != null) {
                showFragment(currentFragment)
            } else {
                shortToast("currentFragment null")
            }
        }

        binding.removeFragment.setOnClickListener {
            val currentFragment = currentFragment(binding.container.id)
            if (currentFragment != null) {
                removeFragment(currentFragment)
            } else {
                shortToast("currentFragment null")
            }
        }

        binding.popFragment.setOnClickListener {
            popFragment()
        }

        binding.popFragmentName.setOnClickListener {
            val nameFragment = binding.nameFragment.text.toString()
            if (nameFragment.isNotEmpty()) {
                popFragment(nameFragment, 0)
            } else {
                shortToast("nameFragment empty")
            }
        }

        binding.clearAllFragments.setOnClickListener {
            clearAllFragments()
        }

        binding.currentFragment.setOnClickListener {
            val currentFragment = currentFragment(binding.container.id)
            shortToast("currentFragment: ${currentFragment?.tag}")
        }

        binding.printBackStack.setOnClickListener {
            printBackStack()
        }
    }
}