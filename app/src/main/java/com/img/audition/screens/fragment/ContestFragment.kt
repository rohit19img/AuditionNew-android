package com.img.audition.screens.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.img.audition.R
import com.img.audition.adapters.SectionContestPagerAdapter
import com.img.audition.databinding.FragmentContestBinding
import com.img.audition.databinding.FragmentVideoBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager


class ContestFragment : Fragment() {
    private val sessionManager by lazy {
        SessionManager(requireContext())
    }
    private val myApplication by lazy {
        MyApplication(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view= FragmentContestBinding.inflate(inflater,container,false)
        val viewPager = view.viewPager
        val tabLayout = view.tabLayout

        tabLayout.setupWithViewPager(viewPager)
        viewPager.adapter = SectionContestPagerAdapter(parentFragmentManager)

        return view.root
    }
}