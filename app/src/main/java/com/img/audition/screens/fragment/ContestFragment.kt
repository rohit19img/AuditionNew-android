package com.img.audition.screens.fragment

import android.content.Context
import android.os.Bundle
import android.util.DumpableContainer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.media3.common.util.UnstableApi
import com.google.android.material.tabs.TabLayout
import com.img.audition.R
import com.img.audition.adapters.ContestLiveAdapter
import com.img.audition.databinding.FragmentContestBinding
import com.img.audition.databinding.FragmentVideoBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager


@UnstableApi class ContestFragment(val contextFromHome : Context) : Fragment() {
    val TAG = "ContestFragment"
    private val sessionManager by lazy {
        SessionManager(requireContext())
    }
    private val myApplication by lazy {
        MyApplication(contextFromHome)
    }

    lateinit var tabLayout: TabLayout
    lateinit var viewContainer: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view= FragmentContestBinding.inflate(inflater,container,false)
        tabLayout = view.tabLayout
        viewContainer = view.viewContainer


        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFragment(LiveContestFragment())

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab!!.position){
                    0 ->{
                        loadFragment(LiveContestFragment())
                    }else ->{
                        loadFragment(JoinedContestFragment())
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(viewContainer.id,fragment)
        transaction.commit()
    }
}