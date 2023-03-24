package com.img.audition.screens.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.img.audition.databinding.FragmentTrendingSearchBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TrendingSearchFragment : Fragment() {
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
        val view = FragmentTrendingSearchBinding.inflate(inflater,container,false)

        return view.root
    }

}