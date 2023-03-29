package com.img.audition.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.img.audition.screens.fragment.FollowersFragment
import com.img.audition.screens.fragment.FollowingFragment

class SectionFollowFollowingListPager (fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FollowersFragment()
            else -> FollowingFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Followers"
            else -> "Following"
        }
    }
}