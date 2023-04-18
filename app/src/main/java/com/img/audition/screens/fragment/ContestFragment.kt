package com.img.audition.screens.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
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


class ContestFragment(val contextFromHome : Context) : Fragment() {
    val TAG = "ContestFragment"
    private val sessionManager by lazy {
        SessionManager(requireContext())
    }
    private val myApplication by lazy {
        MyApplication(contextFromHome)
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



    override fun onPause() {
        Log.d("check 400", "onPause: $TAG")
        LiveContestFragment().onPause()
        super.onPause()
    }

    override fun onStop() {
        LiveContestFragment().onStop()
        super.onStop()
    }
    override fun onResume() {
        Log.d("check 400", "onResume: $TAG")
        super.onResume()
    }

    override fun onDetach() {
        Log.d("check 400", "onResume: $TAG")
        LiveContestFragment().onDetach()
        super.onDetach()
    }

    override fun onDestroyView() {
        Log.d("check 400", "onDestroyView: $TAG")
        LiveContestFragment().onDestroyView()
        super.onDestroyView()
    }

}