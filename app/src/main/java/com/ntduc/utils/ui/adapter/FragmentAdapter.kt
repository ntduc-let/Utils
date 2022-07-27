package com.ntduc.utils.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ntduc.utils.ui.fragment.OneFragment
import com.ntduc.utils.ui.fragment.SecondFragment
import com.ntduc.utils.ui.fragment.ThreeFragment

class FragmentAdapter(
    fa: FragmentActivity
) : FragmentStateAdapter(fa) {

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> OneFragment()
            1 -> SecondFragment()
            2 -> ThreeFragment()
            3 -> OneFragment()
            4 -> SecondFragment()
            5 -> ThreeFragment()
            else -> OneFragment()
        }
    }

    override fun getItemCount(): Int {
        return 6
    }
}