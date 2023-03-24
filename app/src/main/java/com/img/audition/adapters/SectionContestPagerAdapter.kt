package com.img.audition.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.img.audition.screens.fragment.JoinedContestFragment
import com.img.audition.screens.fragment.LiveContestFragment

class SectionContestPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getCount(): Int {
       return 2
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> LiveContestFragment()
            else -> JoinedContestFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Live Contest"
            else -> "Joined Contest"
        }
    }
}