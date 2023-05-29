package com.img.audition.screens.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import com.img.audition.adapters.LeaderboardAdapter
import com.img.audition.dataModel.LeaderboardDataResponse
import com.img.audition.databinding.FragmentLeaderboardBinding
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LeaderboardFragment(val contestID: String) : Fragment() {

    private val TAG = "LeaderboardFragment"
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
        leaderboardapi(view.context)
    }

    private fun leaderboardapi(context: Context) {
        val leaderboardapiReq = apiInterface.getLeaderboardDetails(sessionManager.getToken(),contestID)

        leaderboardapiReq.enqueue(@UnstableApi object : Callback<LeaderboardDataResponse>{
            override fun onResponse(call: Call<LeaderboardDataResponse>, response: Response<LeaderboardDataResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    val data = response.body()!!.data
                    val adapter = LeaderboardAdapter(context,data,contestID)
                    view.leaderboard.adapter = adapter
                }else{
                    Log.e(TAG, "onResponse33: $response")
                }
            }

            override fun onFailure(call: Call<LeaderboardDataResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: $t")
            }

        })
    }

    override fun onDestroyView() {
        Log.d("check 400", "onDestroyView: $TAG")
        getView()?.destroyDrawingCache()
        super.onDestroyView()
    }
}