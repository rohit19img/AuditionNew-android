package com.img.audition.screens.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.img.audition.R
import com.img.audition.adapters.DetailsPrizecardAdapter
import com.img.audition.adapters.LeaderboardAdapter
import com.img.audition.dataModel.LeaderboardDataResponse
import com.img.audition.databinding.FragmentDetailsPrizecardBinding
import com.img.audition.databinding.FragmentLeaderboardBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LeaderboardFragment(val contestID: String) : Fragment() {

    val TAG = "CompletedContestFragment"
    private lateinit var _viewBinding : FragmentLeaderboardBinding
    private val view get() = _viewBinding
    private val sessionManager by lazy {
        SessionManager(requireContext())
    }


    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentLeaderboardBinding.inflate(inflater,container,false)

        return _viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        leaderboardapi()
    }

    private fun leaderboardapi() {
        val leaderboardapiReq = apiInterface.getLeaderboardDetails(sessionManager.getToken(),contestID)

        leaderboardapiReq.enqueue(object : Callback<LeaderboardDataResponse>{
            override fun onResponse(call: Call<LeaderboardDataResponse>, response: Response<LeaderboardDataResponse>) {
                if (response.isSuccessful && response.body()?.success!!){
                    try {
                        val data = response.body()!!.data
                        val adapter = LeaderboardAdapter(requireContext(),data)
                        view.leaderboard.adapter = adapter
                    }catch (e:java.lang.Exception){
                        Log.e(TAG, "onResponse: $e")
                    }


                }else{
                    Log.e(TAG, "onResponse: $response")
                }
            }

            override fun onFailure(call: Call<LeaderboardDataResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: $t")
            }

        })
    }
}