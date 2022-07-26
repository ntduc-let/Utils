package com.ntduc.utils.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ntduc.utils.fragment.OneFragment
import com.ntduc.utils.fragment.SecondFragment
import com.ntduc.utils.fragment.ThreeFragment

class FragmentAdapter(
    fa: FragmentActivity
) : FragmentStateAdapter(fa) {

    override fun createFragment(position: Int): Fragment {
        when(position){
            0 -> return OneFragment()
            1 -> return SecondFragment()
            2 -> return ThreeFragment()
            else -> return OneFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}