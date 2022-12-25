package com.ntduc.utils.navigation_utils.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.ntduc.navigationutils.navigateToActionListener
import com.ntduc.utils.R
import com.ntduc.utils.databinding.FragmentFlowStepBinding

class FlowStepFragment : Fragment() {
    private lateinit var binding: FragmentFlowStepBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFlowStepBinding.inflate(inflater)

        val safeArgs: FlowStepFragmentArgs by navArgs()
        val flowStepNumber = safeArgs.flowStepNumber

        binding.text.text = "Step $flowStepNumber"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener(navigateToActionListener(R.id.next_action))
    }
}