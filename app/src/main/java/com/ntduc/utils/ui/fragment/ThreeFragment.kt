package com.ntduc.utils.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ntduc.utils.databinding.FragmentThreeBinding

class ThreeFragment : Fragment() {
    private lateinit var binding: FragmentThreeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentThreeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}