package com.ntduc.utils.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ntduc.utils.databinding.FragmentSecondBinding

class SecondFragment : Fragment() {
    private lateinit var binding: FragmentSecondBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSecondBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}